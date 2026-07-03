package com.huiyi.v4

import com.huiyi.v4.accessibility.AccessibilityRuntimeState
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.LastSpeakerAcceptanceReportGenerator
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import com.huiyi.v4.domain.pipeline.RealDeviceTestIntent
import com.huiyi.v4.domain.pipeline.SampleSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LastMeWaitRegressionFixTest {
    private val generator = LastSpeakerAcceptanceReportGenerator()

    @Test
    fun lastMeCurrentRootReturnsWait() {
        val result = lastMeResult()

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertTrue(result.routes.isEmpty())
        assertFalse(result.apiCalled)
    }

    @Test
    fun lastMeDoesNotGenerateRoutes() {
        val report = generator.build(
            result = lastMeResult().copy(waitPanelShown = true, routePanelShown = false),
            trace = null,
            accessibilityState = runtimeState(),
            scenario = RealDeviceScenario.LAST_ME,
            testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
            generatedAt = 1L,
            versionName = "test",
            versionCode = 1
        )

        assertEquals("PASS", report.result)
        assertTrue(report.markdown.contains("- decisionType: WAIT"))
        assertTrue(report.markdown.contains("- routeCount: 0"))
    }

    @Test
    fun userAssertedLastMeActualOtherReportsAssertionMismatch() {
        val report = generator.build(
            result = lastOtherResult().copy(
                overlayShownInTargetApp = true,
                resultShownAsOverlay = true,
                userStayedInChatApp = true,
                routePanelShown = true
            ),
            trace = null,
            accessibilityState = runtimeState(),
            scenario = RealDeviceScenario.LAST_ME,
            testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
            generatedAt = 1L,
            versionName = "test",
            versionCode = 1
        )

        assertEquals("USER_ASSERTION_MISMATCH", report.result)
        assertEquals("CONTROLLED_FAIL_WITH_LAST_ME_EVIDENCE", report.currentOverallResult)
        assertTrue(report.json.contains("\"failureCategory\": \"user_assertion_vs_accessibility_mismatch\""))
    }

    @Test
    fun userAssertedLastMeActualMeButRoutesShownIsFail() {
        val staleRoutes = lastOtherResult().routes
        val result = lastMeResult().copy(
            routes = staleRoutes,
            waitPanelShown = false,
            routePanelShown = true,
            staleRoutesReused = true
        )

        val report = generator.build(
            result = result,
            trace = null,
            accessibilityState = runtimeState(),
            scenario = RealDeviceScenario.LAST_ME,
            testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_ME,
            generatedAt = 1L,
            versionName = "test",
            versionCode = 1
        )

        assertEquals("FAIL", report.result)
        assertEquals("last_me_decision_rule_violation", report.failureCategory)
    }

    @Test
    fun lastOtherStillReturnsRoutes() {
        val result = lastOtherResult().copy(
            overlayShownInTargetApp = true,
            resultShownAsOverlay = true,
            userStayedInChatApp = true,
            routePanelShown = true
        )
        val report = generator.build(
            result = result,
            trace = null,
            accessibilityState = runtimeState(),
            scenario = RealDeviceScenario.LAST_OTHER,
            testIntent = RealDeviceTestIntent.USER_ASSERTED_LAST_OTHER,
            generatedAt = 1L,
            versionName = "test",
            versionCode = 1
        )

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(5, result.routes.size)
        assertEquals("PASS", report.result)
    }

    @Test
    fun waitDecisionClearsStaleRoutes() {
        val result = lastMeResult().copy(
            sessionId = "new-session",
            previousSessionId = "old-session",
            panelSessionId = "new-session",
            waitPanelShown = true,
            routePanelShown = false,
            staleRoutesClearedAtSessionStart = true,
            staleRoutesReused = false
        )

        assertTrue(result.routes.isEmpty())
        assertTrue(result.staleRoutesClearedAtSessionStart)
        assertFalse(result.staleRoutesReused)
        assertEquals(result.sessionId, result.panelSessionId)
    }

    private fun lastMeResult() = evidenceResult(
        appPackage = "com.bajiao.im.liaoqi",
        source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
        messages = listOf(
            textNode("other-1", Speaker.OTHER, "ok", 1),
            textNode("me-2", Speaker.ME, "I replied", 2)
        ),
        includeRoutes = false
    )

    private fun lastOtherResult() = evidenceResult(
        appPackage = "com.bajiao.im.liaoqi",
        source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
        messages = listOf(
            textNode("me-1", Speaker.ME, "hello", 1),
            textNode("other-2", Speaker.OTHER, "what are you doing", 2)
        ),
        includeRoutes = true
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
}
