package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.LastSpeakerDecisionUseCase
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.domain.tactical.ReplyRouteGenerator
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine

fun evidenceResult(
    appPackage: String,
    source: SampleSource,
    messages: List<com.huiyi.v4.domain.model.MessageNode>,
    includeRoutes: Boolean = true,
    apiCalled: Boolean = false
): CurrentScreenPipelineResult {
    val snapshot = CurrentScreenSnapshot(
        appPackage = appPackage,
        windowTitle = "聊天窗口",
        screenWidth = 1080,
        screenHeight = 2400,
        nodes = messages.mapIndexed { index, message ->
            ScreenNodeSnapshot(
                id = "n$index",
                text = message.normalizedText,
                contentDescription = null,
                className = "TextView",
                viewIdResourceName = null,
                bounds = message.bounds ?: VisualBounds(0, index * 100, 300, index * 100 + 80),
                visibleToUser = true,
                depth = 1,
                childCount = 0
            )
        },
        capturedAt = 1
    )
    val capture = CurrentScreenCaptureResult(snapshot, messages, source)
    val context = ContextAssembler().assemble(messages)
    val decision = TacticalDecisionEngine().decide(context)
    val routes = if (includeRoutes) ReplyRouteGenerator().generate(context, decision) else emptyList()
    return CurrentScreenPipelineResult(
        captureResult = capture,
        context = context,
        lastSpeakerDecision = LastSpeakerDecisionUseCase().decide(messages),
        tacticalDecision = decision,
        routes = routes,
        apiCalled = apiCalled
    )
}
