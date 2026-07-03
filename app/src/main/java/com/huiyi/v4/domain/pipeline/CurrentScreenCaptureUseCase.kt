package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.LiaoqiRealParser
import com.huiyi.v4.domain.capture.VisualTruthAligner
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource

data class CurrentScreenCaptureResult(
    val snapshot: CurrentScreenSnapshot,
    val messages: List<MessageNode>,
    val sampleSource: SampleSource,
    val warning: String? = null,
    val parserName: String = "GenericVisualBubbleParser",
    val parserFallbackUsed: Boolean = false,
    val accessibilityBoundsProjected: Boolean = false,
    val ocrUsed: Boolean = false,
    val visualTruthAvailable: Boolean = false,
    val visualConflictCount: Int = 0,
    val visualSpeakerFallbackCount: Int = 0
)

open class CurrentScreenCaptureUseCase(
    private val serviceProvider: () -> HuiyiAccessibilityService? = { HuiyiAccessibilityService.instance }
) {
    open fun capture(): Result<CurrentScreenCaptureResult> {
        val service = serviceProvider() ?: return Result.failure(IllegalStateException("无障碍服务未连接。"))
        return service.captureCurrentScreen().mapCatching { snapshot ->
            val bubbles = snapshot.nodes.toVisualBubbles()
            val parsed = parseForApp(snapshot.appPackage, snapshot.screenWidth, bubbles)
            val visualAlignment = VisualTruthAligner(snapshot.screenWidth).align(parsed.messages)
            val messages = visualAlignment.messages
                .filter { it.normalizedText?.isNotBlank() == true || it.content is MessageContent.Voice }
            if (messages.isEmpty()) error("当前屏幕未识别到聊天消息。")
            val source = if (snapshot.appPackage == "com.huiyi.mockchat") {
                SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
            } else {
                SampleSource.REAL_DEVICE_ACCESSIBILITY
            }
            CurrentScreenCaptureResult(
                snapshot = snapshot,
                messages = messages,
                sampleSource = source,
                warning = if (bubbles.size < snapshot.nodes.count { it.readableText != null }) "WARNING: fallback parser filtered low quality nodes." else null,
                parserName = parsed.parserName,
                parserFallbackUsed = parsed.fallbackUsed,
                accessibilityBoundsProjected = visualAlignment.accessibilityBoundsProjected,
                ocrUsed = visualAlignment.ocrUsed,
                visualTruthAvailable = visualAlignment.visualTruthAvailable,
                visualConflictCount = visualAlignment.conflictCount,
                visualSpeakerFallbackCount = visualAlignment.visualSpeakerFallbackCount
            )
        }
    }

    private fun parseForApp(appPackage: String?, screenWidth: Int, bubbles: List<VisualBubble>): ParserSelection {
        if (appPackage == "com.bajiao.im.liaoqi") {
            val liaoqiMessages = LiaoqiRealParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            if (liaoqiMessages.any { it.isEffectiveChatMessage }) {
                return ParserSelection(liaoqiMessages, "LiaoqiRealParser", fallbackUsed = false)
            }
            val genericMessages = GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            return ParserSelection(genericMessages, "GenericVisualBubbleParser", fallbackUsed = true)
        }
        return ParserSelection(
            messages = GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN),
            parserName = "GenericVisualBubbleParser",
            fallbackUsed = false
        )
    }

    private fun List<ScreenNodeSnapshot>.toVisualBubbles(): List<VisualBubble> {
        return asSequence()
            .filter { it.visibleToUser }
            .filter { it.bounds.right > it.bounds.left && it.bounds.bottom > it.bounds.top }
            .filter { it.readableText?.isNotBlank() == true }
            .filterNot { it.bounds.bottom - it.bounds.top < 4 }
            .map { node ->
                VisualBubble(
                    id = node.id,
                    text = node.readableText,
                    rowBounds = node.parentBounds ?: node.bounds,
                    bubbleBounds = node.ancestorBoundsChain.asReversed()
                        .firstOrNull { bounds -> bounds != node.bounds && bounds.right > bounds.left && bounds.bottom > bounds.top }
                        ?: node.parentBounds
                        ?: node.bounds,
                    textBounds = node.bounds,
                    parentBounds = node.parentBounds,
                    ancestorBoundsChain = node.ancestorBoundsChain,
                    confidence = if (node.className?.contains("TextView") == true) 88 else 72
                )
            }
            .toList()
    }

    private data class ParserSelection(
        val messages: List<MessageNode>,
        val parserName: String,
        val fallbackUsed: Boolean
    )
}
