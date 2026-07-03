package com.huiyi.v4

import com.huiyi.v4.BuildConfig
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import com.huiyi.v4.runtime.OneTapGithubUploadConfig
import com.huiyi.v4.runtime.OneTapGithubUploadErrorCode
import com.huiyi.v4.runtime.OneTapGithubUploadGateway
import com.huiyi.v4.runtime.OneTapGithubUploadReportGenerator
import com.huiyi.v4.runtime.OneTapGithubUploadRequest
import com.huiyi.v4.runtime.OneTapGithubUploadResponse
import com.huiyi.v4.runtime.OneTapGithubUploadStage
import com.huiyi.v4.runtime.OneTapGithubUploader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class OneTapGithubUploadTest {
    @Test
    fun oneTapFeedbackUploadsToGithubWhenPrivacySafe() = runBlocking {
        val uploader = OneTapGithubUploader(
            config = OneTapGithubUploadConfig(endpoint = "https://gateway.example/upload"),
            gateway = FakeGateway(Result.success(successResponse()))
        )

        val report = uploader.upload(zip(safe = true), record())

        assertTrue(report.uploadAttempted)
        assertTrue(report.uploadSuccess)
        assertEquals("abc123", report.githubCommitHash)
        assertEquals("outputs/gpt_review_inbox/phone/latest/", report.githubReviewPath)
        assertEquals(OneTapGithubUploadStage.UPLOAD_SUCCESS, report.state.stage)
    }

    @Test
    fun oneTapFeedbackBlocksGithubUploadWhenPrivacyUnsafe() = runBlocking {
        val uploader = OneTapGithubUploader(
            config = OneTapGithubUploadConfig(endpoint = "https://gateway.example/upload"),
            gateway = FakeGateway(Result.success(successResponse()))
        )

        val report = uploader.upload(zip(safe = false), record())

        assertFalse(report.uploadAttempted)
        assertFalse(report.uploadSuccess)
        assertEquals(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_PRIVACY_BLOCKED, report.errorCode)
        assertTrue(report.fallbackLocalZipPath!!.endsWith(".zip"))
    }

    @Test
    fun githubUploadFailureFallsBackToLocalZip() = runBlocking {
        val uploader = OneTapGithubUploader(
            config = OneTapGithubUploadConfig(endpoint = "https://gateway.example/upload"),
            gateway = FakeGateway(Result.failure(IllegalStateException("HTTP 500")))
        )

        val report = uploader.upload(zip(safe = true), record())

        assertTrue(report.uploadAttempted)
        assertFalse(report.uploadSuccess)
        assertEquals(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_SERVER_REJECTED, report.errorCode)
        assertTrue(report.fallbackLocalZipPath!!.endsWith(".zip"))
    }

    @Test
    fun githubUploadDisabledKeepsLocalZipFallback() = runBlocking {
        val uploader = OneTapGithubUploader(
            config = OneTapGithubUploadConfig(endpoint = ""),
            gateway = FakeGateway(Result.success(successResponse()))
        )

        val report = uploader.upload(zip(safe = true), record())

        assertFalse(report.uploadAttempted)
        assertFalse(report.uploadSuccess)
        assertEquals(OneTapGithubUploadErrorCode.GITHUB_UPLOAD_DISABLED, report.errorCode)
        assertTrue(report.fallbackLocalZipPath!!.endsWith(".zip"))
    }

    @Test
    fun githubUploadReportGeneratedOnSuccessAndFailure() = runBlocking {
        val success = OneTapGithubUploader(
            config = OneTapGithubUploadConfig(endpoint = "https://gateway.example/upload"),
            gateway = FakeGateway(Result.success(successResponse()))
        ).upload(zip(safe = true), record())
        val failure = OneTapGithubUploader(
            config = OneTapGithubUploadConfig(endpoint = ""),
            gateway = FakeGateway(Result.success(successResponse()))
        ).upload(zip(safe = true), record())

        assertTrue(OneTapGithubUploadReportGenerator.markdown(success).contains("upload success: true"))
        assertTrue(OneTapGithubUploadReportGenerator.json(success).contains("\"githubCommitHash\": \"abc123\""))
        assertTrue(OneTapGithubUploadReportGenerator.markdown(failure).contains("GITHUB_UPLOAD_DISABLED"))
        assertTrue(OneTapGithubUploadReportGenerator.json(failure).contains("\"fallbackLocalZipPath\""))
    }

    @Test
    fun githubUploadDoesNotPutTokenInApkConfig() {
        val combined = BuildConfig.HUIYI_REVIEW_UPLOAD_ENDPOINT + "\n" + BuildConfig.HUIYI_REVIEW_UPLOAD_CLIENT_KEY

        assertFalse(combined.contains("ghp_"))
        assertFalse(combined.contains("github_pat_"))
        assertFalse(combined.contains("GITHUB_APP_PRIVATE_KEY"))
    }

    private class FakeGateway(
        private val result: Result<OneTapGithubUploadResponse>
    ) : OneTapGithubUploadGateway {
        var latestRequest: OneTapGithubUploadRequest? = null

        override suspend fun upload(request: OneTapGithubUploadRequest): Result<OneTapGithubUploadResponse> {
            latestRequest = request
            return result
        }
    }

    private fun successResponse() = OneTapGithubUploadResponse(
        githubCommitHash = "abc123",
        githubBranch = "main",
        githubReviewPath = "outputs/gpt_review_inbox/phone/latest/",
        githubReviewUrl = "https://github.com/a663949987-alt/huiyi-v4-core/tree/main/outputs/gpt_review_inbox/phone/latest",
        uploadedAt = "2026-07-03T00:00:00Z"
    )

    private fun record() = NextSentenceFlightRecordFactory.fromFailure(
        NextSentenceSessionTrace("s1", 100L, endedAt = 200L)
            .failed(NextSentenceErrorCode.CHAT_WINDOW_NOT_FOUND, NextSentenceStage.CHAT_MESSAGES_PARSED, now = 200L)
    )

    private fun zip(safe: Boolean): File {
        val file = File.createTempFile("huiyi-one-tap-feedback", ".zip").also { it.deleteOnExit() }
        ZipOutputStream(file.outputStream()).use { stream ->
            stream.putNextEntry(ZipEntry("metadata/privacy-scan.json"))
            stream.write(
                """
                    {
                      "containsRawPrivateChat": ${!safe},
                      "containsRawScreenshot": false,
                      "containsApiKey": false,
                      "containsToken": false,
                      "containsKeystore": false,
                      "safeForPublicGitHub": $safe
                    }
                """.trimIndent().toByteArray()
            )
            stream.closeEntry()
        }
        return file
    }
}
