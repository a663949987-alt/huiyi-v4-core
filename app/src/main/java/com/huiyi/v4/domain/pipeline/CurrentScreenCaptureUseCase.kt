package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource

data class CurrentScreenCaptureResult(
    val snapshot: CurrentScreenSnapshot,
    val messages: List<MessageNode>,
    val warning: String? = null
)

class CurrentScreenCaptureUseCase(
    private val serviceProvider: () -> HuiyiAccessibilityService? = { HuiyiAccessibilityService.instance },
    private val parserFactory: (Int) -> GenericVisualBubbleParser = { width -> GenericVisualBubbleParser(screenWidth = width) }
) {
    fun capture(): Result<CurrentScreenCaptureResult> {
        val service = serviceProvider() ?: return Result.failure(IllegalStateException("无障碍服务未连接。"))
        return service.captureCurrentScreen().mapCatching { snapshot ->
            val bubbles = snapshot.nodes.toVisualBubbles()
            val messages = parserFactory(snapshot.screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
                .filter { it.normalizedText?.isNotBlank() == true || it.content is com.huiyi.v4.domain.model.MessageContent.Voice }
            if (messages.isEmpty()) error("当前屏幕未识别到聊天消息。")
            CurrentScreenCaptureResult(
                snapshot = snapshot,
                messages = messages,
                warning = if (bubbles.size < snapshot.nodes.count { it.readableText != null }) "WARNING: fallback parser filtered low quality nodes." else null
            )
        }
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
                    bubbleBounds = node.bounds,
                    textBounds = node.bounds,
                    confidence = if (node.className?.contains("TextView") == true) 88 else 72
                )
            }
            .toList()
    }
}
