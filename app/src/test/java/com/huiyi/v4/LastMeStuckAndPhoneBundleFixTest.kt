package com.huiyi.v4

import com.huiyi.v4.accessibility.AccessibilityRuntimeState
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.LastSpeakerAcceptanceReportGenerator
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.pipeline.RealDeviceScenarioValidator
import com.huiyi.v4.domain.pipeline.RealDeviceTestIntent
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.runtime.PhoneGptReviewBundleBuilder
import com.huiyi.v4.runtime.PhoneGptReviewBundleInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class LastMeStuckAndPhoneBundleFixTest {
    @Test
    fun lastMeAnalyzingTimesOutAndWritesReport() {
        val trace = NextSentenceSessionTrace("s1", 100L, endedAt = 8_200L, stage = NextSentenceStage.NODE_TREE_CAPTURE_STARTED)
            .failed(NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK, NextSentenceStage.NODE_TREE_CAPTURE_STARTED, now = 8_200L)
        val report = LastSpeakerAcceptanceReportGenerator().build(
            result = null,
            trace = trace,
            accessibilityState = runtimeState(),
            scenario = RealDeviceScenario.LAST_ME,
            testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
            generatedAt = 9_000L,
            versionName = "4.1.12",
            versionCode = 430
        )

        assertEquals("STUCK_ANALYZING", report.result)
        assertEquals("CONTROLLED_FAIL_WITH_LAST_ME_STUCK_EVIDENCE", report.currentOverallResult)
        assertTrue(report.json.contains("\"timeoutErrorCode\": \"LAST_ME_ANALYSIS_STUCK\""))
    }

    @Test
    fun lastMeWaitDecisionRendersWaitPanel() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("o1", Speaker.OTHER, "ok", 1), textNode("m2", Speaker.ME, "I replied", 2)),
            includeRoutes = false
        ).copy(
            waitPanelShown = true,
            routePanelShown = false,
            sessionTerminalState = "PANEL_RENDERED_WAIT",
            decisionTypeFamily = "WAIT"
        )

        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertTrue(result.waitPanelShown)
        assertFalse(result.routePanelShown)
        assertEquals(0, result.routes.size)
        assertEquals("PANEL_RENDERED_WAIT", result.sessionTerminalState)
    }

    @Test
    fun lastOtherEmpathyFirstWithFiveRoutesIsFunctionalPass() {
        val result = evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("m1", Speaker.ME, "hi", 1), textNode("o2", Speaker.OTHER, "I am sad today", 2)),
            includeRoutes = true
        ).copy(
            tacticalDecision = evidenceResult(
                appPackage = "com.bajiao.im.liaoqi",
                source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
                messages = listOf(textNode("m1", Speaker.ME, "hi", 1), textNode("o2", Speaker.OTHER, "I am sad today", 2)),
                includeRoutes = false
            ).tacticalDecision.copy(decisionType = TacticalDecisionType.EMPATHY_FIRST),
            overlayShownInTargetApp = true,
            resultShownAsOverlay = true,
            userStayedInChatApp = true,
            routePanelShown = true
        )

        val validation = RealDeviceScenarioValidator.validate(result, RealDeviceScenario.LAST_OTHER)

        assertEquals("PASS", validation.realDeviceFunctionalSmoke)
        assertEquals("PASS", validation.currentOverallResult)
    }

    @Test
    fun phoneBundleDoesNotOverwriteLastMeReportWithPlaceholder() {
        val zip = tempZip()
        val summary = PhoneGptReviewBundleBuilder().build(
            input(lastMeMarkdown = "- lastMeResult: STUCK_ANALYZING\n- failureReason: analysis_timeout_without_terminal_state"),
            zip
        )

        ZipFile(zip).use { opened ->
            val lastMe = opened.read("last-me/last-me-real-device-report-for-gpt.md")
            assertTrue(lastMe.contains("STUCK_ANALYZING"))
            assertFalse(lastMe.contains("NOT_GENERATED_ON_PHONE"))
        }
        assertEquals("STUCK_ANALYZING", summary.lastMeRealDeviceResult)
    }

    @Test
    fun oldFailureReportMovedToStale() {
        val zip = tempZip()
        val summary = PhoneGptReviewBundleBuilder().build(
            input(
                latestFailureMarkdown = "- versionName: 4.1.8b\n- versionCode: 418\n- exceptionClass: java.lang.SecurityException",
                latestFailureJson = """{"versionName":"4.1.8b","versionCode":418,"exceptionClass":"java.lang.SecurityException"}"""
            ),
            zip
        )

        ZipFile(zip).use { opened ->
            assertNotNull(opened.getEntry("stale/old-latest-next-sentence-failure.md"))
            assertTrue(opened.read("failure/latest-next-sentence-failure.json").contains("NOT_TESTED"))
        }
        assertEquals("STALE_OLD_VERSION", summary.latestFailureFreshness)
        assertFalse(summary.latestFailureUsedForCurrentOverallResult)
    }

    private fun input(
        lastMeMarkdown: String? = "- lastMeResult: NOT_TESTED\n- actualLastSpeaker: NOT_TESTED",
        latestFailureMarkdown: String? = null,
        latestFailureJson: String? = null
    ) = PhoneGptReviewBundleInput(
        appVersionName = "4.1.12",
        appVersionCode = 430,
        packageName = "com.huiyi.v4",
        buildType = "debug",
        targetChatAppPackage = "com.bajiao.im.liaoqi",
        latestSessionId = "s1",
        generatedAt = 1L,
        currentReviewMarkdown = "- currentOverallResult: PASS\n- realDeviceFunctionalSmoke: PASS",
        currentScreenMarkdown = "- currentOverallResult: PASS\n- realDeviceFunctionalSmoke: PASS\n- scenarioName: real_device_last_other",
        currentScreenJson = """{"currentOverallResult":"PASS"}""",
        lastMeMarkdown = lastMeMarkdown,
        lastMeJson = lastMeMarkdown?.let { """{"lastMeResult":"STUCK_ANALYZING"}""" },
        lastOtherMarkdown = "- lastOtherRealDeviceResult: PASS\n- actualLastSpeaker: OTHER",
        lastOtherJson = """{"lastOtherRealDeviceResult":"PASS"}""",
        latestFailureMarkdown = latestFailureMarkdown,
        latestFailureJson = latestFailureJson
    )

    private fun runtimeState() = AccessibilityRuntimeState(
        systemAccessibilityEnabled = true,
        serviceConnected = true,
        rootAvailable = true,
        currentPackage = "com.bajiao.im.liaoqi",
        currentWindowTitle = "chat",
        lastServiceConnectedAt = 1L,
        lastAccessibilityEventAt = 1L,
        lastRootAvailableAt = 1L,
        lastDisconnectAt = null,
        lastInterruptAt = null,
        lastDestroyAt = null,
        lastError = null,
        activeServiceInstanceId = "test",
        overlayVisible = true,
        floatingServiceRunning = true
    )

    private fun tempZip(): File = File.createTempFile("huiyi-phone-gpt-review", ".zip").also { it.deleteOnExit() }

    private fun ZipFile.read(path: String): String = getInputStream(getEntry(path)).bufferedReader().use { it.readText() }
}
