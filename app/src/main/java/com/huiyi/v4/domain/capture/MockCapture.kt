package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.VisualBounds

class MockScreenCaptureChannel : ScreenCaptureChannel {
    override val type: ScreenCaptureType = ScreenCaptureType.MOCK

    override fun isAvailable(): Boolean = true

    override suspend fun capture(): ScreenFrame = ScreenFrame(
        id = "mock-frame",
        width = 1080,
        height = 2400,
        createdAt = System.currentTimeMillis(),
        sourceLabel = "mock"
    )
}

class MockOcrEngine : OcrEngine {
    override suspend fun recognize(frame: ScreenFrame): List<VisualTextLine> = listOf(
        VisualTextLine("line-1", "今天店里真的好忙，有点累", VisualBounds(80, 1100, 620, 1210), 92),
        VisualTextLine("line-2", "那你先喝点水，别硬撑", VisualBounds(500, 1260, 1000, 1370), 94),
        VisualTextLine("line-3", "[语音] 18秒", VisualBounds(90, 1430, 510, 1540), 86)
    )
}
