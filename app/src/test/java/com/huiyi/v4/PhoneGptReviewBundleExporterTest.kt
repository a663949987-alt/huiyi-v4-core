package com.huiyi.v4

import com.huiyi.v4.runtime.PhoneGptReviewBundleBuilder
import com.huiyi.v4.runtime.PhoneGptReviewBundleInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class PhoneGptReviewBundleExporterTest {
    @Test
    fun phoneGptReviewBundleIncludesRequiredFiles() {
        val zip = buildZip(input())

        ZipFile(zip).use { opened ->
            assertNotNull(opened.getEntry("README_FOR_GPT.md"))
            assertNotNull(opened.getEntry("phone-gpt-review-manifest.json"))
            assertNotNull(opened.getEntry("current/huiyi-v4-review-for-gpt.md"))
            assertNotNull(opened.getEntry("current/real-device-current-screen-report-for-gpt.md"))
            assertNotNull(opened.getEntry("current/real-device-current-screen-report.json"))
            assertNotNull(opened.getEntry("metadata/privacy-scan.json"))
        }
    }

    @Test
    fun createsPlaceholdersForMissingLastMeAndLastOther() {
        val zip = buildZip(input(lastMeMarkdown = null, lastOtherMarkdown = null))

        ZipFile(zip).use { opened ->
            val lastMe = opened.read("last-me/last-me-real-device-report-for-gpt.md")
            val lastOther = opened.read("last-other/last-other-real-device-report-for-gpt.md")

            assertTrue(lastMe.contains("NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO"))
            assertTrue(lastMe.contains("USER_DID_NOT_HAVE_SAFE_LAST_ME_SCENARIO"))
            assertTrue(lastOther.contains("NOT_TESTED"))
            assertTrue(opened.read("phone-gpt-review-manifest.json").contains("\"exists\": false"))
        }
    }

    @Test
    fun missingSafeLastMeScenarioHasExplicitSummaryResult() {
        val zip = tempZip()
        val summary = PhoneGptReviewBundleBuilder().build(input(lastMeMarkdown = null), zip)

        assertEquals("NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO", summary.lastMeRealDeviceResult)
        ZipFile(zip).use { opened ->
            val manifest = opened.read("phone-gpt-review-manifest.json")
            assertTrue(manifest.contains("\"result\": \"NOT_TESTED_USER_DID_NOT_HAVE_SAFE_SCENARIO\""))
        }
    }

    @Test
    fun separatesCurrentAndStaleReportsWithoutPollutingCurrentResult() {
        val zip = tempZip()
        val summary = PhoneGptReviewBundleBuilder().build(
            input(staleReports = mapOf("old-last-me-fail.md" to "- overall_result: FAIL\n- failureReason: old")),
            zip
        )

        ZipFile(zip).use { opened ->
            assertNotNull(opened.getEntry("stale/old-last-me-fail.md"))
            assertTrue(opened.read("stale/README_STALE_REPORTS.md").contains("staleReportCount: 1"))
        }
        assertEquals("PASS", summary.currentOverallResult)
    }

    @Test
    fun manifestPrivacyFlagsAreSafeForPublicByDefault() {
        val zip = buildZip(input())

        ZipFile(zip).use { opened ->
            val manifest = opened.read("phone-gpt-review-manifest.json")
            assertTrue(manifest.contains("\"containsRawPrivateChat\": false"))
            assertTrue(manifest.contains("\"containsRawScreenshot\": false"))
            assertTrue(manifest.contains("\"safeForPublicGitHub\": true"))
        }
    }

    @Test
    fun doesNotIncludeRawChatByDefault() {
        val zip = buildZip(
            input(
                currentScreenMarkdown = """
                    - currentOverallResult: PASS
                    - lastEffectiveMessageText: 这是一句不应该公开的私聊原文
                    message: 另一句不应该公开的私聊原文
                """.trimIndent(),
                currentScreenJson = """{"text":"这是一句不应该公开的私聊原文","message":"另一句不应该公开的私聊原文"}"""
            )
        )

        ZipFile(zip).use { opened ->
            val markdown = opened.read("current/real-device-current-screen-report-for-gpt.md")
            val json = opened.read("current/real-device-current-screen-report.json")
            assertFalse(markdown.contains("这是一句不应该公开的私聊原文"))
            assertFalse(json.contains("另一句不应该公开的私聊原文"))
            assertTrue(markdown.contains("[REDACTED_PRIVATE_CHAT]"))
            assertTrue(json.contains("[REDACTED_PRIVATE_CHAT]"))
        }
    }

    @Test
    fun zipCanBeOpened() {
        val zip = buildZip(input())

        ZipFile(zip).use { opened ->
            assertTrue(opened.entries().asSequence().count() > 10)
        }
    }

    private fun input(
        currentScreenMarkdown: String = """
            - currentOverallResult: PASS
            - realDeviceFunctionalSmoke: PASS
            - scenarioName: real_device_last_other
            - actualLastSpeaker: OTHER
            - actualDecisionType: NORMAL_REPLY
            - actualRouteCount: 5
            - overlayShownInTargetApp: true
            - mainActivityOpened: false
            - screenshotFailureBlocksMainPath: false
        """.trimIndent(),
        currentScreenJson: String = """{"currentOverallResult":"PASS","actualLastSpeaker":"OTHER"}""",
        lastMeMarkdown: String? = "- scenarioResult: NOT_TESTED\n- actualLastSpeaker: NOT_TESTED",
        lastOtherMarkdown: String? = "- scenarioResult: PASS\n- actualLastSpeaker: OTHER\n- actualDecisionType: NORMAL_REPLY\n- actualRouteCount: 5",
        staleReports: Map<String, String> = emptyMap()
    ) = PhoneGptReviewBundleInput(
        appVersionName = "4.1.11",
        appVersionCode = 428,
        packageName = "com.huiyi.v4",
        buildType = "debug",
        targetChatAppPackage = "com.bajiao.im.liaoqi",
        latestSessionId = "s1",
        generatedAt = 1L,
        currentReviewMarkdown = "- currentOverallResult: PASS\n- realDeviceFunctionalSmoke: PASS",
        currentScreenMarkdown = currentScreenMarkdown,
        currentScreenJson = currentScreenJson,
        lastMeMarkdown = lastMeMarkdown,
        lastMeJson = lastMeMarkdown?.let { """{"scenarioResult":"NOT_TESTED"}""" },
        lastOtherMarkdown = lastOtherMarkdown,
        lastOtherJson = lastOtherMarkdown?.let { """{"scenarioResult":"PASS"}""" },
        latestFailureMarkdown = null,
        latestFailureJson = null,
        staleReports = staleReports
    )

    private fun buildZip(input: PhoneGptReviewBundleInput): File {
        val zip = tempZip()
        PhoneGptReviewBundleBuilder().build(input, zip)
        return zip
    }

    private fun tempZip(): File {
        return File.createTempFile("huiyi-phone-gpt-review", ".zip").also { it.deleteOnExit() }
    }

    private fun ZipFile.read(path: String): String {
        return getInputStream(getEntry(path)).bufferedReader().use { it.readText() }
    }
}
