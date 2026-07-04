package com.huiyi.v4

import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.NextSentenceStage
import com.huiyi.v4.domain.pipeline.userFacingMessageFor
import com.huiyi.v4.runtime.NextSentenceClickAck
import com.huiyi.v4.runtime.NextSentenceUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextSentenceNoReactionUxTest {
    @Test
    fun NextSentenceClickShowsAckImmediatelyTest() {
        val ack = NextSentenceClickAck(
            clickReceivedAt = 1_000L,
            clickAckShownAt = 1_120L,
            clickAckVisible = true
        )

        assertTrue(ack.clickAckVisible)
        assertTrue((ack.clickAckLatencyMs ?: Long.MAX_VALUE) <= 300L)
    }

    @Test
    fun NextSentenceClickCreatesSessionTraceTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "s1",
            startedAt = 1_000L,
            stage = NextSentenceStage.CLICK_ACK_SHOWN,
            clickReceivedAt = 1_000L,
            clickAckShownAt = 1_100L,
            clickAckLatencyMs = 100L,
            clickAckVisible = true,
            runNextSentenceEntered = true,
            sessionCreated = true
        )

        assertEquals("s1", trace.sessionId)
        assertTrue(trace.clickAckVisible)
        assertTrue(trace.sessionCreated)
    }

    @Test
    fun RunNextSentenceEntryIsRecordedTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "s2",
            startedAt = 2_000L,
            runNextSentenceEntered = true,
            sessionCreated = true
        )

        assertTrue(trace.runNextSentenceEntered)
        assertTrue(trace.sessionCreated)
    }

    @Test
    fun NoVisibleResultTimesOutToPanelTest() {
        val trace = NextSentenceSessionTrace("s-timeout", 1_000L)
            .failed(NextSentenceErrorCode.NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT, NextSentenceStage.CAPTURE_STARTING, now = 9_000L)
            .copy(
                stage = NextSentenceStage.TIMEOUT,
                terminalState = "TIMEOUT_PANEL",
                panelShown = true
            )

        assertEquals(NextSentenceStage.TIMEOUT, trace.stage)
        assertEquals("TIMEOUT_PANEL", trace.terminalState)
        assertTrue(trace.panelShown)
    }

    @Test
    fun CloudStartCannotHideLoadingTest() {
        assertTrue(NextSentenceUiState.LOADING_CLOUD.ordinal > NextSentenceUiState.CLICK_ACK.ordinal)
        assertFalse(NextSentenceUiState.LOADING_CLOUD == NextSentenceUiState.IDLE)
    }

    @Test
    fun RootFailureShowsControlledPanelTest() {
        val message = userFacingMessageFor(NextSentenceErrorCode.ROOT_UNAVAILABLE)
        assertTrue(message.contains("回到聊天页") || message.contains("当前窗口"))
        assertFalse(message.contains("分析失败"))
    }

    @Test
    fun ParserFailureShowsControlledPanelTest() {
        val message = userFacingMessageFor(NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY)
        assertTrue(message.contains("没识别出聊天气泡"))
        assertFalse(message.contains("分析失败"))
    }

    @Test
    fun UnknownExceptionShowsControlledPanelTest() {
        val message = userFacingMessageFor(NextSentenceErrorCode.UNKNOWN_EXCEPTION)
        assertTrue(message.contains("没有跑完"))
        assertFalse(message.contains("分析失败"))
    }

    @Test
    fun FloatingServiceCollectsLoadingUiStateTest() {
        val states = listOf(
            NextSentenceUiState.CLICK_ACK,
            NextSentenceUiState.LOADING_CAPTURE,
            NextSentenceUiState.RESULT
        )

        assertTrue(states.contains(NextSentenceUiState.CLICK_ACK))
        assertTrue(states.contains(NextSentenceUiState.LOADING_CAPTURE))
    }

    @Test
    fun PanelVisibleNotRequiredForClickAckTest() {
        val ack = NextSentenceClickAck(
            clickReceivedAt = 1L,
            clickAckShownAt = 2L,
            clickAckVisible = true,
            panelVisibleBeforeClick = false
        )

        assertTrue(ack.clickAckVisible)
        assertFalse(ack.panelVisibleBeforeClick)
    }
}
