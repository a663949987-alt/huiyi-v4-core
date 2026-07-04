package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.pipeline.NextSentenceCaptureSource
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceFailureReportGenerator
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.userFacingMessageFor
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextSentenceFailureDiagnosticsTest {
    @Test
    fun genericAnalysisFailedOnlyForUnknownExceptionTest() {
        assertFalse(userFacingMessageFor(NextSentenceErrorCode.ROOT_UNAVAILABLE).contains("分析失败"))
        assertFalse(userFacingMessageFor(NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY).contains("分析失败"))
        assertFalse(userFacingMessageFor(NextSentenceErrorCode.API_DISABLED).contains("分析失败"))
        assertFalse(userFacingMessageFor(NextSentenceErrorCode.UNKNOWN_EXCEPTION).contains("分析失败"))
        assertTrue(userFacingMessageFor(NextSentenceErrorCode.UNKNOWN_EXCEPTION).contains("没有跑完"))
    }

    @Test
    fun nextSentenceFailureWritesFailureReportTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "session-test",
            startedAt = 1L,
            stage = NextSentenceStage.FAILED,
            failedStage = NextSentenceStage.CHAT_MESSAGES_PARSED,
            errorCode = NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY,
            captureSource = NextSentenceCaptureSource.CURRENT_ROOT,
            rawNodeCount = 12,
            visibleTextCount = 8,
            parsedMessageCount = 0,
            effectiveMessageCount = 0,
            bubbleVisibleAfterFailure = true,
            bubbleAttachedAfterClick = true,
            permissionMissingMessageShown = false,
            userFacingMessage = userFacingMessageFor(NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY)
        )
        val report = NextSentenceFailureReportGenerator()

        assertTrue(report.buildMarkdown(trace).contains("errorCode: CHAT_MESSAGE_PARSE_EMPTY"))
        assertTrue(report.buildJson(trace).contains("\"failedStage\": \"CHAT_MESSAGES_PARSED\""))
        assertTrue(report.buildJson(trace).contains("\"bubbleVisibleAfterFailure\": true"))
    }

    @Test
    fun rootUnavailableDoesNotShowPermissionMissingTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "root-null",
            startedAt = 1L,
            failedStage = NextSentenceStage.ROOT_CAPTURE_RETRYING,
            errorCode = NextSentenceErrorCode.ROOT_UNAVAILABLE,
            systemAccessibilityEnabled = true,
            serviceConnected = true,
            rootAvailableAfterRetry = false,
            permissionMissingMessageShown = false,
            userFacingMessage = userFacingMessageFor(NextSentenceErrorCode.ROOT_UNAVAILABLE)
        )

        assertFalse(trace.permissionMissingMessageShown)
        assertEquals("当前窗口暂时不可读取，请回到聊天页再试一次。", trace.userFacingMessage)
    }

    @Test
    fun rootOwnOverlayUsesLastStableChatSnapshotTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "overlay",
            startedAt = 1L,
            rootPackageName = "com.huiyi.v4",
            rootIsOwnOverlay = true,
            captureSource = NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT,
            usedFallbackSnapshot = true,
            lastStableSnapshotAgeMs = 900L,
            lastStableSnapshotPackage = "com.bajiao.im.liaoqi",
            parsedMessageCount = 4
        )

        assertTrue(trace.usedFallbackSnapshot)
        assertEquals(NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT, trace.captureSource)
        assertEquals("com.bajiao.im.liaoqi", trace.lastStableSnapshotPackage)
    }

    @Test
    fun staleSnapshotIsRejectedTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "stale",
            startedAt = 1L,
            failedStage = NextSentenceStage.ROOT_CAPTURE_RETRYING,
            errorCode = NextSentenceErrorCode.ROOT_UNAVAILABLE,
            captureSource = NextSentenceCaptureSource.NONE,
            usedFallbackSnapshot = false,
            lastStableSnapshotAgeMs = 6001L
        )

        assertFalse(trace.usedFallbackSnapshot)
        assertEquals(NextSentenceCaptureSource.NONE, trace.captureSource)
    }

    @Test
    fun lastSpeakerMeReturnsWaitNotFailureTest() {
        val messages = listOf(
            textNode("1", Speaker.OTHER, "今天有点累", 1),
            textNode("2", Speaker.ME, "那你先休息，我晚点再找你", 2)
        )
        val context = ContextAssembler().assemble(messages)
        val lastSpeaker = LastSpeakerDecisionUseCase().decide(messages)
        val decision = TacticalDecisionEngine().decide(context)
        val routes = if (decision.decisionType == TacticalDecisionType.WAIT) {
            emptyList()
        } else {
            ReplyRouteGenerator().generate(context, decision)
        }

        assertEquals(Speaker.ME, lastSpeaker.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, decision.decisionType)
        assertTrue(routes.isEmpty())
    }
}
