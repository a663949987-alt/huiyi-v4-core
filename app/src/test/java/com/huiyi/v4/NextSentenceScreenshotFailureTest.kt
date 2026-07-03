package com.huiyi.v4

import com.huiyi.v4.domain.pipeline.NextSentenceCaptureSource
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceFailureReportGenerator
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.mapScreenshotException
import com.huiyi.v4.domain.pipeline.toNextSentenceException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NextSentenceScreenshotFailureTest {
    @Test
    fun screenshotSecurityExceptionMapsToScreenshotCapabilityMissingTest() {
        val error = SecurityException("Services don't have the capability of taking the screenshot.")

        assertEquals(NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING, mapScreenshotException(error))
    }

    @Test
    fun unknownExceptionNotUsedForKnownSecurityExceptionTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "s1",
            startedAt = 1L,
            primaryCapturePath = "NODE_TREE",
            nodeTreeAttempted = true,
            nodeTreeSuccess = true
        )
        val exception = SecurityException("Services don't have the capability of taking the screenshot.")
            .toNextSentenceException(trace, NextSentenceStage.OPTIONAL_SCREENSHOT_DIAGNOSTIC_STARTED)

        assertEquals(NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING, exception.code)
        assertEquals(NextSentenceStage.OPTIONAL_SCREENSHOT_DIAGNOSTIC_FAILED, exception.failedStage)
        assertFalse(exception.code == NextSentenceErrorCode.UNKNOWN_EXCEPTION)
    }

    @Test
    fun screenshotFailureDoesNotAbortNodeTreeAnalysisTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "success-with-secondary-screenshot-error",
            startedAt = 1L,
            stage = NextSentenceStage.ROUTES_GENERATED,
            errorCode = null,
            secondaryErrorCode = NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING,
            primaryCapturePath = "NODE_TREE",
            nodeTreeAttempted = true,
            nodeTreeSuccess = true,
            screenshotAttempted = true,
            screenshotSuccess = false,
            screenshotAvailable = false,
            screenshotExceptionClass = SecurityException::class.java.name,
            screenshotExceptionMessageRedacted = "Services don't have the capability of taking the screenshot.",
            captureSource = NextSentenceCaptureSource.CURRENT_ROOT,
            parsedMessageCount = 3,
            effectiveMessageCount = 3
        )

        assertNull(trace.errorCode)
        assertEquals(NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING, trace.secondaryErrorCode)
        assertTrue(trace.nodeTreeSuccess)
        assertFalse(trace.screenshotSuccess)
    }

    @Test
    fun latestFailureReportSeparatesNodeTreeAndScreenshotFieldsTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "failure-screenshot",
            startedAt = 1L,
            failedStage = NextSentenceStage.OPTIONAL_SCREENSHOT_DIAGNOSTIC_FAILED,
            errorCode = NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING,
            primaryCapturePath = "NODE_TREE",
            nodeTreeAttempted = true,
            nodeTreeSuccess = true,
            screenshotAttempted = true,
            screenshotSuccess = false,
            screenshotErrorCode = NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING,
            pipelineExceptionClass = SecurityException::class.java.name,
            pipelineExceptionMessageRedacted = "Services don't have the capability of taking the screenshot.",
            permissionMissingMessageShown = false,
            bubbleVisibleAfterFailure = true
        )
        val json = NextSentenceFailureReportGenerator().buildJson(trace)

        assertTrue(json.contains("\"primaryCapturePath\": \"NODE_TREE\""))
        assertTrue(json.contains("\"nodeTreeAttempted\": true"))
        assertTrue(json.contains("\"screenshotErrorCode\": \"SCREENSHOT_CAPABILITY_MISSING\""))
        assertTrue(json.contains("\"pipelineExceptionClass\": \"java.lang.SecurityException\""))
    }

    @Test
    fun wrongNoPermissionNotShownForScreenshotCapabilityMissingTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "no-permission-confusion",
            startedAt = 1L,
            errorCode = NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING,
            systemAccessibilityEnabled = true,
            serviceConnected = true,
            rootAvailableAfterRetry = true,
            permissionMissingMessageShown = false
        )

        assertFalse(trace.permissionMissingMessageShown)
    }

    @Test
    fun failurePanelStateDoesNotContaminateCaptureStateTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "sample-order",
            startedAt = 1L,
            rootPackageAtCaptureStart = "com.bajiao.im.liaoqi",
            rootPackageBeforeFailureUi = "com.bajiao.im.liaoqi",
            currentPackageAfterFailurePanel = "com.android.systemui",
            failurePanelAlreadyShownWhenSampled = true
        )

        assertEquals("com.bajiao.im.liaoqi", trace.rootPackageAtCaptureStart)
        assertEquals("com.android.systemui", trace.currentPackageAfterFailurePanel)
    }
}
