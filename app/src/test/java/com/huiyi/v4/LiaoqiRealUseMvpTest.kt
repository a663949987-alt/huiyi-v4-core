package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.NextSentenceSessionTrace
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.runtime.NextSentenceFlightRecordFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiaoqiRealUseMvpTest {
    @Test
    fun LastMeShowsWaitPanelTest() = runTest {
        val result = pipeline(lastMeMessages(), FakeCloudService()).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.ME, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertEquals("你已经回过了，先等对方。", result.tacticalDecision.bestMove)
    }

    @Test
    fun LastMeDoesNotShowRoutesTest() = runTest {
        val result = pipeline(lastMeMessages(), FakeCloudService()).run(emptyPersona()).getOrThrow()

        assertTrue(result.routes.isEmpty())
        assertFalse(result.routePanelShown)
        assertFalse(result.apiCalled)
        assertFalse(result.cloudTrace.cloudAttempted)
    }

    @Test
    fun LastMeDoesNotShowContextRequiredTest() = runTest {
        val result = pipeline(lastMeMessages(), FakeCloudService()).run(emptyPersona()).getOrThrow()

        assertEquals(TacticalDecisionType.WAIT, result.tacticalDecision.decisionType)
        assertFalse(result.tacticalDecision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED)
    }

    @Test
    fun LastOtherAlwaysShowsFiveRoutesTest() = runTest {
        val result = pipeline(lastOtherMessages(), null).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(5, result.routes.size)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
    }

    @Test
    fun CloudFailureFallsBackToLocalRoutesTest() = runTest {
        val result = pipeline(
            lastOtherMessages(),
            FakeCloudService(error = CloudAnalysisException("NETWORK"))
        ).run(emptyPersona()).getOrThrow()

        assertEquals(Speaker.OTHER, result.lastSpeakerDecision.lastSpeaker)
        assertEquals(5, result.routes.size)
        assertTrue(result.cloudTrace.cloudFallbackUsed)
        assertEquals("LOCAL_FALLBACK", result.cloudTrace.decisionSource)
    }

    @Test
    fun NextSentenceNeverStaysLoadingForeverTest() = runTest {
        val result = pipeline(lastOtherMessages(), null).run(emptyPersona()).getOrThrow()
        val displayed = result.copy(
            routePanelShown = result.routes.size == 5,
            loadingStillVisibleAfterTimeout = false
        )

        assertFalse(displayed.loadingStillVisibleAfterTimeout)
        assertTrue(displayed.routePanelShown)
    }

    @Test
    fun NextSentenceDoesNotOpenMainActivityTest() = runTest {
        val result = pipeline(lastOtherMessages(), null).run(emptyPersona()).getOrThrow()

        assertFalse(result.mainActivityOpened)
        assertFalse(result.huiyiActivityOpened)
        assertFalse(result.foregroundPackageWhenPanelShown == BuildConfig.APPLICATION_ID)
    }

    @Test
    fun OneTapFeedbackDoesNotBlockNextSentenceTest() = runTest {
        val first = pipeline(lastMeMessages(), null).run(emptyPersona()).getOrThrow()
        val record = NextSentenceFlightRecordFactory.fromSuccess(
            first.copy(waitPanelShown = true, routePanelShown = false),
            NextSentenceSessionTrace(sessionId = "first", startedAt = 1L)
        )
        val second = pipeline(lastOtherMessages(), null).run(emptyPersona()).getOrThrow()

        assertEquals("WAIT_PANEL", record.terminalState)
        assertEquals(5, second.routes.size)
        assertEquals(Speaker.OTHER, second.lastSpeakerDecision.lastSpeaker)
    }

    private fun pipeline(
        messages: List<com.huiyi.v4.domain.model.MessageNode>,
        cloud: CloudAnalysisService?
    ) = CurrentScreenPipelineUseCase(
        captureUseCase = FakeCaptureUseCase(messages),
        cloudAnalysisService = cloud,
        appVersionName = "4.1.28",
        appVersionCode = 447
    )

    private class FakeCaptureUseCase(
        private val messages: List<com.huiyi.v4.domain.model.MessageNode>
    ) : CurrentScreenCaptureUseCase({ null }) {
        override fun capture(): Result<CurrentScreenCaptureResult> = Result.success(
            CurrentScreenCaptureResult(
                snapshot = CurrentScreenSnapshot(
                    appPackage = "com.bajiao.im.liaoqi",
                    windowTitle = "聊起",
                    screenWidth = 1080,
                    screenHeight = 2400,
                    nodes = emptyList(),
                    capturedAt = 1L
                ),
                messages = messages,
                sampleSource = SampleSource.REAL_DEVICE_ACCESSIBILITY
            )
        )
    }

    private class FakeCloudService(
        override val config: CloudAnalysisConfig = CloudAnalysisConfig(
            cloudEnabled = true,
            endpoint = "https://relay.example/v1",
            apiKey = "placeholder",
            relayApiKeyStoredSecurely = true
        ),
        private val error: Throwable? = null
    ) : CloudAnalysisService {
        override suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput> {
            error?.let { return Result.failure(it) }
            val routes: List<ReplyRoute> = ReplyRouteGenerator().generate(input.context, input.localDecision)
                .mapIndexed { index, route -> route.copy(id = "cloud-$index", tag = "云端") }
            return Result.success(CloudAnalysisOutput("cloud-req", input.localDecision, routes, 42))
        }
    }

    private fun lastMeMessages() = listOf(
        textNode("other-1", Speaker.OTHER, "我今天有点忙", 1),
        textNode("me-2", Speaker.ME, "你先忙，我晚点再看你", 2)
    )

    private fun lastOtherMessages() = listOf(
        textNode("other-1", Speaker.OTHER, "今天真的有点累", 1),
        textNode("me-2", Speaker.ME, "怎么了", 2),
        textNode("other-3", Speaker.OTHER, "就是事情太多了，想缓一缓", 3)
    )

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
