package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.VisualBounds

data class ScreenFrame(
    val id: String,
    val width: Int,
    val height: Int,
    val createdAt: Long,
    val sourceLabel: String
)

data class VisualTextLine(
    val id: String,
    val text: String,
    val bounds: VisualBounds,
    val confidence: Int
)

interface ScreenCaptureChannel {
    val type: ScreenCaptureType
    fun isAvailable(): Boolean
    suspend fun capture(): ScreenFrame
}

enum class ScreenCaptureType {
    ACCESSIBILITY_SCREENSHOT,
    MEDIA_PROJECTION,
    MANUAL_IMPORT,
    MOCK
}

interface OcrEngine {
    suspend fun recognize(frame: ScreenFrame): List<VisualTextLine>
}
