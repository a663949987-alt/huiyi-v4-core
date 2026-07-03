package com.huiyi.v4

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.runtime.NextSentenceFlightRecord
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import com.huiyi.v4.runtime.OneTapFeedbackZipContract
import com.huiyi.v4.runtime.UserFeedbackMark
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalStateChainClosureTest {
    @Test
    fun NextSentenceSessionPreAnalysisFrozenBeforePanelTest() {
        val record = lastMeRecord("freeze-session")

        assertEquals(100L, record.preAnalysisSnapshotFrozenAt)
        assertEquals("CURRENT_ROOT_BEFORE_PANEL", record.preAnalysisSnapshotSource)
        assertFalse(record.preAnalysisSnapshotMutableAfterPanel)
        assertFalse(record.postPanelSnapshotUsedForDecision)
    }

    @Test
    fun NextSentenceSessionImmutableAfterTerminalStateTest() {
        val record = lastMeRecord("immutable-session")

        assertTrue(record.sessionImmutableAfterTerminalState)
        assertEquals("WAIT_PANEL", record.terminalState)
        assertEquals("ME", record.actualLastSpeaker)
    }

    @Test
    fun OneTapFeedbackDoesNotCreateNewNextSentenceSessionTest() {
        val record = lastMeRecord("original-session")
            .withFeedback(UserFeedbackMark(markedWrong = true))
            .withFeedbackTrace(300L, "original-session", "BOUND_PANEL_SESSION")

        assertEquals("original-session", record.sessionId)
        assertEquals("original-session", record.feedbackTargetSessionId)
        assertFalse(record.feedbackTriggeredNewAnalysis)
    }

    @Test
    fun OneTapFeedbackDoesNotRecaptureRootTest() {
        val record = lastMeRecord("no-recapture")
            .withFeedbackTrace(300L, "no-recapture", "BOUND_PANEL_SESSION")

        assertFalse(record.feedbackReCapturedCurrentRoot)
        assertFalse(record.preAnalysisSnapshotMutableAfterPanel)
        assertFalse(record.postPanelSnapshotUsedForDecision)
    }

    @Test
    fun OneTapFeedbackDoesNotUseOverlayStateAsPreAnalysisTest() {
        val record = lastMeRecord("clean-feedback")
            .copy(windowTitlePreAnalysisRedacted = "聊天窗口")
            .withFeedbackTrace(300L, "clean-feedback", "BOUND_PANEL_SESSION")

        assertFalse(record.feedbackUsedOverlayStateAsPreAnalysis)
        assertTrue(record.preAnalysisSnapshotTrusted)
        assertEquals("PASS", record.reportConsistencyResult)
    }

    @Test
    fun NoTargetSessionForFeedbackShowsControlledErrorTest() {
        val selected = OneTapFeedbackZipContract.selectTargetRecord(
            panelSessionId = "missing-panel-session",
            lastCompletedSessionId = "last-completed",
            latest = lastMeRecord("last-completed"),
            records = listOf(lastMeRecord("last-completed"))
        )

        assertNull(selected)
    }

    @Test
    fun LastMeCannotBecomeHuiyiMomentTest() {
        val record = lastMeRecord("last-me")

        assertEquals("ME", record.actualLastSpeaker)
        assertEquals("WAIT", record.decisionType)
        assertEquals("WAIT", record.decisionTypeFamily)
        assertEquals("WAIT_PANEL", record.terminalState)
        assertEquals(0, record.routeCount)
        assertFalse(record.routePanelShown)
        assertFalse(record.contextRequiredPanelShown)
    }

    @Test
    fun LastMeDecisionSourceIsLocalWaitTest() {
        val record = lastMeRecord("local-wait")

        assertEquals("LOCAL_WAIT", record.decisionSource)
        assertEquals("LAST_SPEAKER_ME_WAIT", record.cloudSkippedReason)
        assertFalse(record.cloudAttempted)
    }

    @Test
    fun PreAnalysisHuiyiPanelContaminationDetectedTest() {
        val record = routeRecord("contaminated")
            .copy(windowTitlePreAnalysisRedacted = "会意雷达 最后一句是我 这次不对，发给 GPT 正在上传 GitHub")
            .withComputedConsistency()

        assertTrue(record.preAnalysisLooksLikeHuiyiPanel)
        assertFalse(record.preAnalysisSnapshotTrusted)
        assertEquals("FAIL_CONTAMINATED_EXPORT", record.reportConsistencyResult)
    }

    @Test
    fun ContaminatedPhoneLatestNotUsedForCurrentResultTest() {
        val phoneLatest = PhoneLatestState(
            versionName = "4.1.23",
            currentVersionName = "4.1.23",
            contaminated = true
        )

        assertEquals("STALE_CONTAMINATED", phoneLatest.freshness)
        assertFalse(phoneLatest.usedForCurrentResult)
    }

    @Test
    fun OldPhoneLatestMarkedStaleTest() {
        val phoneLatest = PhoneLatestState(
            versionName = "4.1.20",
            currentVersionName = "4.1.23",
            contaminated = false
        )

        assertEquals("STALE_OLD_VERSION", phoneLatest.freshness)
        assertFalse(phoneLatest.usedForCurrentResult)
    }

    @Test
    fun FixturePassDoesNotImplyEmulatorUiPassTest() {
        val split = ResultSplit(fixtureReplayResult = "PASS", mockChatBuildResult = "PASS", emulatorAvailable = false)

        assertEquals("PASS", split.fixtureReplayResult)
        assertEquals("NOT_RUN", split.emulatorUiSmokeResult)
    }

    @Test
    fun MockChatBuildPassDoesNotImplyEmulatorUiPassTest() {
        val split = ResultSplit(fixtureReplayResult = "NOT_RUN", mockChatBuildResult = "PASS", emulatorAvailable = false)

        assertEquals("PASS", split.mockChatBuildResult)
        assertEquals("NOT_RUN", split.emulatorUiSmokeResult)
    }

    private fun routeRecord(sessionId: String): NextSentenceFlightRecord = NextSentenceFlightRecordFactory.fromSuccess(
        evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("m1", Speaker.ME, "hi", 1),
                textNode("o2", Speaker.OTHER, "tell me more", 2)
            ),
            includeRoutes = true,
            windowTitle = "聊天窗口"
        ).copy(
            sessionId = sessionId,
            analysisStartedAt = 100L,
            analysisEndedAt = 260L,
            analysisDurationMs = 160L,
            routePanelShown = true
        ),
        NextSentenceSessionTrace(sessionId, 100L, endedAt = 260L)
    ).copy(
        cloudEnabled = false,
        cloudAttempted = false,
        cloudSkippedReason = "CLOUD_NOT_CONFIGURED",
        decisionSource = "LOCAL_FALLBACK"
    ).withComputedConsistency()

    private fun lastMeRecord(sessionId: String): NextSentenceFlightRecord = NextSentenceFlightRecordFactory.fromSuccess(
        evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("o1", Speaker.OTHER, "ok", 1), textNode("m2", Speaker.ME, "I replied", 2)),
            includeRoutes = false,
            windowTitle = "聊天窗口"
        ).copy(
            sessionId = sessionId,
            analysisStartedAt = 100L,
            analysisEndedAt = 250L,
            analysisDurationMs = 150L,
            waitPanelShown = true,
            routePanelShown = false,
            sessionTerminalState = "WAIT_PANEL",
            decisionTypeFamily = "WAIT"
        ),
        NextSentenceSessionTrace(sessionId, 100L, endedAt = 250L)
    ).copy(
        cloudEnabled = false,
        cloudAttempted = false,
        cloudSkippedReason = "LAST_SPEAKER_ME_WAIT",
        decisionSource = "LOCAL_WAIT",
        cloudConfigured = false,
        cloudAnalysisAttempted = false
    ).withComputedConsistency()

    private data class PhoneLatestState(
        val versionName: String,
        val currentVersionName: String,
        val contaminated: Boolean
    ) {
        val freshness: String = when {
            versionName < currentVersionName -> "STALE_OLD_VERSION"
            contaminated -> "STALE_CONTAMINATED"
            else -> "CURRENT"
        }
        val usedForCurrentResult: Boolean = freshness == "CURRENT" && !contaminated
    }

    private data class ResultSplit(
        val fixtureReplayResult: String,
        val mockChatBuildResult: String,
        val emulatorAvailable: Boolean
    ) {
        val emulatorUiSmokeResult: String = if (emulatorAvailable) "PASS" else "NOT_RUN"
    }
}
