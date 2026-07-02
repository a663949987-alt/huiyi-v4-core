package com.huiyi.v4

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.SampleSource
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class UnknownSpeakerBlocksHighConfidenceTest {
    @Test
    fun highUnknownRatioGeneratesNoRoutes() = runTest {
        val messages = listOf(
            unknownNode("u1", 1),
            unknownNode("u2", 2),
            textNode("o1", Speaker.OTHER, "你好", 3)
        )
        val useCase = CurrentScreenPipelineUseCase(
            captureUseCase = object : CurrentScreenCaptureUseCase(serviceProvider = { null }) {
                override fun capture(): Result<CurrentScreenCaptureResult> {
                    return Result.success(evidenceResult("com.chat.real", SampleSource.REAL_DEVICE_ACCESSIBILITY, messages).captureResult!!)
                }
            }
        )

        val result = useCase.run(DefaultPersonaCorpus.soldier()).getOrThrow()

        assertTrue(result.routes.isEmpty())
    }

    private fun unknownNode(id: String, sequence: Long): MessageNode = MessageNode(
        id = id,
        contactId = null,
        speaker = Speaker.UNKNOWN,
        content = MessageContent.Text("未知消息"),
        normalizedText = "未知消息",
        source = MessageSource.ACCESSIBILITY_CURRENT_SCREEN,
        localSequence = sequence,
        confidence = 60,
        speakerConfidence = 30,
        contentConfidence = 80,
        bounds = VisualBounds(450, 0, 550, 80),
        pageIndex = 0,
        createdAt = sequence,
        sceneId = null,
        speakerReason = "unknown_visual_bounds",
        parserName = "GenericVisualBubbleParser"
    )
}
