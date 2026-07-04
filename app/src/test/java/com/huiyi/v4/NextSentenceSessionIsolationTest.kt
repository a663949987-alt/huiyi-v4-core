package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.pipeline.CloudResponseBinding
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.NextSentenceErrorCode
import com.huiyi.v4.domain.pipeline.NextSentenceSessionBinding
import com.huiyi.v4.domain.pipeline.NextSentenceSessionGate
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextSentenceSessionIsolationTest {
    @Test
    fun DoubleClickNextOnlyOneActiveSessionTest() {
        val first = response("first", "snap-a")
        val secondActive = active("second", "snap-b")

        val decision = NextSentenceSessionGate.evaluate(secondActive, first)

        assertFalse(decision.allowed)
        assertEquals(NextSentenceSessionGate.STALE_SESSION, decision.discardedReason)
    }

    @Test
    fun SwitchingChatCancelsOldCloudRequestTest() {
        val active = active("new", "snap-new", chatWindowHash = "chat-b")
        val oldResponse = response("new", "snap-old", chatWindowHash = "chat-a")

        val decision = NextSentenceSessionGate.evaluate(active, oldResponse)

        assertFalse(decision.allowed)
        assertEquals(NextSentenceSessionGate.SNAPSHOT_CHANGED, decision.discardedReason)
    }

    @Test
    fun StaleCloudResponseDoesNotRenderPanelTest() {
        val active = active("current", "snap")
        val stale = response("old", "snap")

        val decision = NextSentenceSessionGate.evaluate(active, stale)

        assertFalse(decision.allowed)
        assertEquals(NextSentenceSessionGate.STALE_SESSION, decision.discardedReason)
    }

    @Test
    fun ContaminatedPreAnalysisSkipsCloudAndRoutesTest() = runTest {
        val cloud = FakeCloud()
        val result = pipeline(
            windowTitle = "没读到当前聊天 请回到聊起聊天窗口 这次不对，发给 GPT 隐藏",
            cloud = cloud
        ).run(emptyPersona(), sessionId = "contaminated").getOrThrow()

        assertEquals(TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED, result.tacticalDecision.decisionType)
        assertEquals("CONTROLLED_FAIL", result.sessionTerminalState)
        assertEquals(0, result.routes.size)
        assertFalse(result.cloudTrace.cloudAttempted)
        assertEquals(0, cloud.callCount)
    }

    @Test
    fun OneClickOneTerminalPanelTest() = runTest {
        val result = pipeline(cloud = FakeCloud()).run(emptyPersona(), sessionId = "one-click").getOrThrow()

        assertEquals("one-click", result.sessionId)
        assertEquals("one-click", result.cloudTrace.activeSessionId)
        assertEquals("one-click", result.cloudTrace.panelRenderedSessionId)
        assertTrue(result.cloudTrace.oneClickOneTerminalPanel)
        assertEquals(5, result.routes.size)
    }

    @Test
    fun TimeoutShowsControlledFailNotSilentTest() {
        val trace = NextSentenceSessionTrace(
            sessionId = "timeout",
            startedAt = 1L,
            endedAt = 9000L,
            errorCode = NextSentenceErrorCode.SESSION_TIMEOUT_NO_TERMINAL_STATE,
            userFacingMessage = "这次分析超时，请回到聊天页再试一次。"
        )
        val record = NextSentenceFlightRecordFactory.fromFailure(trace)

        assertEquals("TIMEOUT", record.terminalState)
        assertTrue(record.loadingStillVisible)
        assertEquals("SESSION_TIMEOUT_NO_TERMINAL_STATE", record.errorCode)
    }

    private fun active(
        sessionId: String,
        snapshotId: String,
        chatPackage: String = "com.huiyi.mockchat",
        chatWindowHash: String = "chat-a",
        panelSessionId: String? = null
    ) = NextSentenceSessionBinding(
        activeSessionId = sessionId,
        activeSnapshotId = snapshotId,
        activeChatPackage = chatPackage,
        activeChatWindowHash = chatWindowHash,
        panelSessionId = panelSessionId
    )

    private fun response(
        sessionId: String,
        snapshotId: String,
        chatPackage: String = "com.huiyi.mockchat",
        chatWindowHash: String = "chat-a"
    ) = CloudResponseBinding(
        sessionId = sessionId,
        preAnalysisSnapshotId = snapshotId,
        chatPackage = chatPackage,
        chatWindowHash = chatWindowHash
    )

    private fun pipeline(
        windowTitle: String = "chat",
        cloud: CloudAnalysisService
    ) = CurrentScreenPipelineUseCase(
        captureUseCase = FakeCaptureUseCase(windowTitle),
        cloudAnalysisService = cloud,
        appVersionName = "test",
        appVersionCode = 1
    )

    private class FakeCaptureUseCase(
        private val windowTitle: String
    ) : CurrentScreenCaptureUseCase({ null }) {
        override fun capture(): Result<CurrentScreenCaptureResult> {
            return Result.success(
                CurrentScreenCaptureResult(
                    snapshot = CurrentScreenSnapshot(
                        appPackage = "com.bajiao.im.liaoqi",
                        windowTitle = windowTitle,
                        screenWidth = 1080,
                        screenHeight = 2400,
                        nodes = emptyList(),
                        capturedAt = 1L
                    ),
                    messages = listOf(
                        textNode("other-1", Speaker.OTHER, "hello", 1),
                        textNode("me-2", Speaker.ME, "I am here", 2),
                        textNode("other-3", Speaker.OTHER, "today was hard", 3)
                    ),
                    sampleSource = SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
                )
            )
        }
    }

    private class FakeCloud : CloudAnalysisService {
        override val config: CloudAnalysisConfig = CloudAnalysisConfig(
            cloudEnabled = true,
            endpoint = "https://relay.example/v1",
            apiKey = "placeholder",
            relayApiKeyStoredSecurely = true
        )
        var callCount = 0

        override suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput> {
            callCount += 1
            val routes: List<ReplyRoute> = ReplyRouteGenerator().generate(input.context, input.localDecision)
            return Result.success(
                CloudAnalysisOutput(
                    sessionId = input.sessionId,
                    preAnalysisSnapshotId = input.preAnalysisSnapshotId,
                    chatPackage = input.chatPackage,
                    chatWindowHash = input.chatWindowHash,
                    cloudRequestId = "cloud",
                    decision = input.localDecision,
                    routes = routes,
                    latencyMs = 10L
                )
            )
        }
    }

    private fun emptyPersona() = UserPersonaCorpus(
        id = "test",
        name = "test",
        enabled = false,
        identityCards = emptyList(),
        storyCards = emptyList(),
        styleRules = emptyList(),
        riskRules = emptyList()
    )
}
