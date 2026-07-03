package com.huiyi.v4

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import com.huiyi.v4.runtime.OneTapFeedbackZipContract
import com.huiyi.v4.runtime.UserFeedbackMark
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

    @Test
    fun recentSessionRecordsDeduplicateLatestSessionBeforeWritingZipEntries() {
        val latest = NextSentenceFlightRecordFactory.fromFailure(
            NextSentenceSessionTrace("same-session", 100L, endedAt = 200L)
                .failed(NextSentenceErrorCode.CHAT_WINDOW_NOT_FOUND, NextSentenceStage.CHAT_MESSAGES_PARSED, now = 200L)
        )
        val older = latest.copy(sessionId = "older-session")

        val records = OneTapFeedbackZipContract.recentSessionRecords(
            records = listOf(older, latest),
            latest = latest
        )

        assertEquals(listOf("older-session", "same-session"), records.map { it.sessionId })
    }

    @Test
    fun OneTapFeedbackExportsBoundPanelSessionTest() {
        val panel = routeRecord("panel-session")
        val later = routeRecord("later-session")

        val selected = OneTapFeedbackZipContract.selectTargetRecord(
            panelSessionId = "panel-session",
            lastCompletedSessionId = "later-session",
            latest = later,
            records = listOf(panel, later)
        )

        assertEquals("panel-session", selected?.first?.sessionId)
        assertEquals("BOUND_PANEL_SESSION", selected?.second)
    }

    @Test
    fun OneTapFeedbackUsesLastCompletedSessionWhenPanelSessionMissingTest() {
        val last = routeRecord("last-completed")

        val selected = OneTapFeedbackZipContract.selectTargetRecord(
            panelSessionId = null,
            lastCompletedSessionId = "last-completed",
            latest = last,
            records = listOf(last)
        )

        assertEquals("last-completed", selected?.first?.sessionId)
        assertEquals("LAST_COMPLETED_NEXT_SENTENCE_SESSION", selected?.second)
    }

    @Test
    fun OneTapFeedbackDoesNotFallbackWhenPanelSessionIdIsMissingFromRecordsTest() {
        val last = routeRecord("last-completed")

        val selected = OneTapFeedbackZipContract.selectTargetRecord(
            panelSessionId = "panel-session-missing",
            lastCompletedSessionId = "last-completed",
            latest = last,
            records = listOf(last)
        )

        assertEquals(null, selected)
    }

    @Test
    fun OneTapFeedbackFailsWhenNoTargetSessionTest() {
        val selected = OneTapFeedbackZipContract.selectTargetRecord(
            panelSessionId = null,
            lastCompletedSessionId = null,
            latest = null,
            records = emptyList()
        )

        assertEquals(null, selected)
    }

    @Test
    fun OneTapFeedbackDoesNotCreateNewAnalysisSessionTest() {
        val record = routeRecord("original-session")
            .withFeedback(UserFeedbackMark(markedWrong = true))
            .withFeedbackTrace(300L, "original-session", "BOUND_PANEL_SESSION")

        assertEquals("original-session", record.sessionId)
        assertEquals("original-session", record.feedbackTargetSessionId)
        assertFalse(record.feedbackTriggeredNewAnalysis)
    }

    @Test
    fun OneTapFeedbackDoesNotRecaptureRootAfterPanelShownTest() {
        val record = routeRecord("original-session")
            .withFeedbackTrace(300L, "original-session", "BOUND_PANEL_SESSION")

        assertFalse(record.feedbackReCapturedCurrentRoot)
        assertFalse(record.postPanelSnapshotUsedForDecision)
        assertFalse(record.preAnalysisSnapshotMutableAfterPanel)
    }

    @Test
    fun PreAnalysisSnapshotFrozenBeforePanelTest() {
        val record = routeRecord("frozen-session")

        assertEquals(100L, record.preAnalysisSnapshotFrozenAt)
        assertEquals("CURRENT_ROOT_BEFORE_PANEL", record.preAnalysisSnapshotSource)
        assertFalse(record.preAnalysisSnapshotMutableAfterPanel)
    }

    @Test
    fun PreAnalysisPanelTextDetectedAsContaminationTest() {
        val record = routeRecord("contaminated")
            .copy(windowTitlePreAnalysisRedacted = "会意雷达 判断：最后一句是我。打法：你已经回过了，先等对方。")
            .withComputedConsistency()

        assertTrue(record.preAnalysisLooksLikeHuiyiPanel)
        assertFalse(record.preAnalysisSnapshotTrusted)
        assertEquals("PRE_ANALYSIS_SNAPSHOT_CONTAMINATED_BY_PANEL", record.preAnalysisSnapshotErrorCode)
    }

    @Test
    fun PreAnalysisWaitPhraseDetectedAsOverlayContaminationTest() {
        val record = routeRecord("wait-phrase")
            .copy(windowTitlePreAnalysisRedacted = "先等对方")
            .withComputedConsistency()

        assertTrue(record.preAnalysisLooksLikeHuiyiPanel)
        assertFalse(record.preAnalysisSnapshotTrusted)
        assertEquals("FAIL_CONTAMINATED_EXPORT", record.reportConsistencyResult)
    }

    @Test
    fun WindowTitleClaimsWaitButRecordClaimsRouteIsConsistencyFailTest() {
        val record = routeRecord("contradiction")
            .copy(windowTitlePreAnalysisRedacted = "会意雷达 最后一句是我，你已经回过了，先等对方。")
            .withComputedConsistency()

        assertTrue(record.preAnalysisTextClaimsLastMeWait)
        assertTrue(record.recordClaimsLastOtherRoutePanel)
        assertTrue(record.windowTitleAndDecisionContradiction)
        assertEquals("FAIL_CONTAMINATED_EXPORT", record.reportConsistencyResult)
    }

    @Test
    fun ContaminatedFeedbackExportCannotBeFunctionalPassTest() {
        val record = routeRecord("contaminated-route")
            .copy(windowTitlePreAnalysisRedacted = "会意雷达 最后一句是我。")
            .withComputedConsistency()

        assertEquals("FAIL_CONTAMINATED_EXPORT", record.reportConsistencyResult)
    }

    @Test
    fun LastMeWaitRuleStillPassesTest() {
        val record = lastMeRecord("last-me")

        assertEquals("ME", record.actualLastSpeaker)
        assertEquals("WAIT", record.decisionType)
        assertEquals("WAIT_PANEL", record.terminalState)
        assertEquals(0, record.routeCount)
        assertTrue(record.waitPanelShown)
        assertFalse(record.routePanelShown)
        assertFalse(record.contextRequiredPanelShown)
        assertFalse(record.cloudAttempted)
        assertEquals("LOCAL_WAIT", record.decisionSource)
    }

    @Test
    fun CloudUploadIsNotCloudAnalysisTest() {
        val record = routeRecord("local")

        assertTrue(record.cloudContractImplemented)
        assertFalse(record.cloudAnalysisAttempted)
        assertFalse(record.cloudAttempted)
        assertEquals("NOT_RUN", record.cloudContractValidationResult)
    }

    @Test
    fun CloudContractImplementedButNotConfiguredIsReportedTest() {
        val record = routeRecord("local")

        assertTrue(record.cloudContractImplemented)
        assertFalse(record.cloudConfigured)
        assertEquals("HuiyiTacticalContract-v1", record.cloudContractVersion)
    }

    @Test
    fun HuiyiMomentAcceptedOnlyWhenPreAnalysisCleanTest() {
        val clean = routeRecord("clean").copy(decisionType = "HUIYI_MOMENT").withComputedConsistency()
        val contaminated = clean.copy(windowTitlePreAnalysisRedacted = "会意雷达 最后一句是我。").withComputedConsistency()

        assertEquals("PASS", clean.reportConsistencyResult)
        assertEquals("FAIL_CONTAMINATED_EXPORT", contaminated.reportConsistencyResult)
    }

    private fun tempZip(): File = File.createTempFile("huiyi-one-tap-feedback", ".zip").also { it.deleteOnExit() }

    private fun routeRecord(sessionId: String) = NextSentenceFlightRecordFactory.fromSuccess(
        evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(
                textNode("m1", Speaker.ME, "hi", 1),
                textNode("o2", Speaker.OTHER, "tell me more", 2),
                textNode("m3", Speaker.ME, "I am here", 3),
                textNode("o4", Speaker.OTHER, "today was hard", 4)
            ),
            includeRoutes = true,
            windowTitle = "聊起"
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
        decisionSource = "LOCAL_FALLBACK",
        cloudConfigured = false,
        cloudAnalysisAttempted = false
    ).withComputedConsistency()

    private fun lastMeRecord(sessionId: String) = NextSentenceFlightRecordFactory.fromSuccess(
        evidenceResult(
            appPackage = "com.bajiao.im.liaoqi",
            source = SampleSource.REAL_DEVICE_ACCESSIBILITY,
            messages = listOf(textNode("o1", Speaker.OTHER, "ok", 1), textNode("m2", Speaker.ME, "I replied", 2)),
            includeRoutes = false,
            windowTitle = "聊起"
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
}
