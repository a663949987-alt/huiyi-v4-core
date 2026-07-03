package com.huiyi.v4

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import com.huiyi.v4.runtime.OneTapFeedbackZipContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class OneTapFeedbackExportTest {
    @Test
    fun lastMeWaitPanelTerminalState() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("o1", Speaker.OTHER, "ok", 1), textNode("m2", Speaker.ME, "I replied", 2)),
            includeRoutes = false
        ).copy(
            sessionId = "s-wait",
            analysisStartedAt = 100L,
            analysisEndedAt = 250L,
            analysisDurationMs = 150L,
            waitPanelShown = true,
            routePanelShown = false
        )

        val record = NextSentenceFlightRecordFactory.fromSuccess(result, NextSentenceSessionTrace("s-wait", 100L, endedAt = 250L))

        assertEquals("WAIT_PANEL", record.terminalState)
        assertEquals("ME", record.actualLastSpeaker)
        assertEquals("WAIT", record.decisionTypeFamily)
        assertEquals(0, record.routeCount)
        assertFalse(record.apiCalled)
    }

    @Test
    fun lastOtherRoutePanelTerminalState() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("m1", Speaker.ME, "hi", 1), textNode("o2", Speaker.OTHER, "tell me more", 2)),
            includeRoutes = true
        ).copy(
            sessionId = "s-route",
            analysisStartedAt = 100L,
            analysisEndedAt = 260L,
            analysisDurationMs = 160L,
            routePanelShown = true
        )

        val record = NextSentenceFlightRecordFactory.fromSuccess(result, NextSentenceSessionTrace("s-route", 100L, endedAt = 260L))

        assertEquals("ROUTE_PANEL", record.terminalState)
        assertEquals("OTHER", record.actualLastSpeaker)
        assertEquals("REPLY_ROUTES", record.decisionTypeFamily)
        assertEquals(5, record.routeCount)
    }

    @Test
    fun sessionTimesOutAfterEightSecondsAndLastMeCannotRemainAnalyzing() {
        val trace = NextSentenceSessionTrace("s-timeout", 100L, endedAt = 8_200L)
            .failed(NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK, NextSentenceStage.NODE_TREE_CAPTURE_STARTED, now = 8_200L)

        val record = NextSentenceFlightRecordFactory.fromFailure(trace)

        assertEquals("TIMEOUT", record.terminalState)
        assertEquals("LAST_ME_ANALYSIS_STUCK", record.errorCode)
        assertTrue(record.loadingStillVisible)
    }

    @Test
    fun unsupportedAppFriendlyExportState() {
        val trace = NextSentenceSessionTrace(
            sessionId = "s-unsupported",
            startedAt = 100L,
            endedAt = 200L,
            rootPackageName = "com.example.unknown",
            rootIsTargetChatApp = false
        ).failed(NextSentenceErrorCode.CHAT_WINDOW_NOT_FOUND, NextSentenceStage.CHAT_MESSAGES_PARSED, now = 200L)

        val record = NextSentenceFlightRecordFactory.fromFailure(trace)

        assertEquals("UNSUPPORTED_APP", record.terminalState)
        assertEquals("com.example.unknown", record.appPackage)
        assertFalse(record.targetAppSupported)
    }

    @Test
    fun oneTapFeedbackZipCanBeOpenedAndContainsRequiredContractFiles() {
        val zip = tempZip()
        ZipOutputStream(zip.outputStream()).use { stream ->
            OneTapFeedbackZipContract.requiredPaths.forEach { path ->
                stream.putNextEntry(ZipEntry(path))
                stream.write("ok".toByteArray())
                stream.closeEntry()
            }
        }

        ZipFile(zip).use { opened ->
            OneTapFeedbackZipContract.requiredPaths.forEach { path ->
                assertNotNull("missing $path", opened.getEntry(path))
            }
        }
    }

    private fun tempZip(): File = File.createTempFile("huiyi-one-tap-feedback", ".zip").also { it.deleteOnExit() }
}
