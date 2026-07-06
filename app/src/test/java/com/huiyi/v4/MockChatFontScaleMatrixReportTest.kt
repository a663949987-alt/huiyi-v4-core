package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.SampleSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MockChatFontScaleMatrixReportTest {
    @Test
    fun mockChatFontScaleMatrixGeneratesReport() = runTest {
        val rows = FontMatrixProfile.entries.flatMap { profile ->
            val regular = FontMatrixScenario.entries.flatMap { scenario ->
                listOf(FontScaleProfile.NORMAL, FontScaleProfile.LARGE, FontScaleProfile.EXTRA_LARGE).map { fontScale ->
                    runRow(profile, scenario, fontScale)
                }
            }
            val huge = listOf(
                FontMatrixScenario.LAST_ME,
                FontMatrixScenario.LAST_OTHER,
                FontMatrixScenario.METADATA_TRAP,
                FontMatrixScenario.LONG_MULTILINE,
                FontMatrixScenario.LOW_EXPRESSION
            ).map { scenario -> runRow(profile, scenario, FontScaleProfile.HUGE) }
            regular + huge
        }
        val report = buildReport(rows)
        File(outputDirectory(), "mockchat-fontscale-matrix-report-for-gpt.md").writeText(report, Charsets.UTF_8)

        assertTrue("font scale matrix should cover at least 100 rows", rows.size >= 100)
        rows.forEach { row ->
            assertTrue("${row.profile.id}/${row.scenario.id}/${row.fontScale.id}: ${row.failReason}", row.failReason == null)
        }
    }

    private suspend fun runRow(
        profile: FontMatrixProfile,
        scenario: FontMatrixScenario,
        fontScale: FontScaleProfile
    ): FontScaleRow {
        val bubbles = scenario.bubbles(profile, fontScale)
        val messages = GenericVisualBubbleParser(screenWidth = profile.screenWidth)
            .parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
        val snapshot = CurrentScreenSnapshot(
            appPackage = "com.huiyi.mockchat",
            windowTitle = "MockChatLab/${profile.id}/${scenario.id}/${fontScale.id}",
            screenWidth = profile.screenWidth,
            screenHeight = profile.screenHeight,
            nodes = bubbles.mapIndexed { index, bubble ->
                ScreenNodeSnapshot(
                    id = "font-node-$index",
                    text = bubble.text,
                    contentDescription = null,
                    className = "androidx.compose.ui.platform.ComposeView",
                    viewIdResourceName = null,
                    bounds = bubble.bubbleBounds ?: bubble.rowBounds ?: bubble.textBounds ?: VisualBounds(0, 0, 1, 1),
                    visibleToUser = true,
                    depth = 2,
                    childCount = 0,
                    parentBounds = bubble.parentBounds,
                    ancestorBoundsChain = bubble.ancestorBoundsChain
                )
            },
            capturedAt = GENERATED_AT,
            density = profile.density,
            scaledDensity = profile.density * fontScale.scale,
            fontScale = fontScale.scale,
            smallestScreenWidthDp = (profile.screenWidth / profile.density).toInt(),
            displaySizeCategory = "fontscale_matrix"
        )
        val capture = CurrentScreenCaptureResult(snapshot, messages, SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY)
        val useCase = CurrentScreenPipelineUseCase(
            captureUseCase = object : CurrentScreenCaptureUseCase(serviceProvider = { null }) {
                override fun capture(): Result<CurrentScreenCaptureResult> = Result.success(capture)
            }
        )
        val result = useCase.run(DefaultPersonaCorpus.soldier()).getOrThrow().copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.huiyi.mockchat",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )
        return FontScaleRow(profile, scenario, fontScale, result, validate(scenario, fontScale, result))
    }

    private fun validate(
        scenario: FontMatrixScenario,
        fontScale: FontScaleProfile,
        result: CurrentScreenPipelineResult
    ): String? {
        val messages = result.captureResult?.messages.orEmpty()
        val metadata = messages.filter { it.metadataType != MetadataType.NONE || it.speaker == Speaker.SYSTEM }
        val candidates = messages.filter { it.metadataType == MetadataType.NONE && it.speaker != Speaker.SYSTEM }
        val effective = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val unknownCount = candidates.count { it.speaker == Speaker.UNKNOWN }
        val unknownRatio = unknownCount.toFloat() / candidates.size.coerceAtLeast(1)
        val lastEffective = result.lastSpeakerDecision.lastEffectiveMessage

        if (fontScale in setOf(FontScaleProfile.LARGE, FontScaleProfile.EXTRA_LARGE)) {
            when (scenario) {
                FontMatrixScenario.LAST_ME -> if (result.lastSpeakerDecision.lastSpeaker != Speaker.ME ||
                    result.tacticalDecision.decisionType != TacticalDecisionType.WAIT ||
                    result.routes.isNotEmpty()
                ) return "font_large/extra_large last_me failed"
                FontMatrixScenario.LAST_OTHER -> if (result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER ||
                    result.tacticalDecision.decisionType != TacticalDecisionType.PASSIVE_NOT_READY ||
                    result.routes.isNotEmpty() ||
                    result.localPassiveRoutesShownToUser
                ) return "font_large/extra_large last_other failed"
                else -> Unit
            }
        }
        if (lastEffective?.metadataType != MetadataType.NONE) return "time/header metadata participated in LastSpeakerDecision"
        if (metadata.any { it.normalizedText in NATURAL_LANGUAGE_SAMPLES }) return "natural language was metadataFiltered"
        if (candidates.size >= 5 && effective.size <= 1) return "candidateChatMessageCount >= 5 but effectiveMessageCount <= 1"
        if (unknownRatio > 0.30f) return "unknownSpeakerCount / candidateChatMessageCount > 30%"
        return when (scenario) {
            FontMatrixScenario.LAST_ME -> when {
                result.lastSpeakerDecision.lastSpeaker != Speaker.ME -> "last_me did not end on ME"
                result.routes.isNotEmpty() -> "last_me generated routes"
                else -> null
            }
            FontMatrixScenario.LAST_OTHER -> when {
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "last_other did not end on OTHER"
                result.tacticalDecision.decisionType != TacticalDecisionType.PASSIVE_NOT_READY -> "last_other did not wait for cloud playbook"
                result.routes.isNotEmpty() -> "last_other exposed local passive routes"
                result.localPassiveRoutesShownToUser -> "last_other showed local passive routes"
                else -> null
            }
            FontMatrixScenario.METADATA_TRAP -> when {
                metadata.count { it.metadataType == MetadataType.TIME || it.metadataType == MetadataType.DATE } < 2 -> "time/date metadata not filtered"
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "metadata trap polluted last speaker"
                else -> null
            }
            FontMatrixScenario.LONG_MULTILINE -> when {
                effective.count { it.normalizedText?.contains("月底最后一天") == true } != 1 -> "long multiline split or lost"
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "long multiline did not end on OTHER"
                else -> null
            }
            FontMatrixScenario.LOW_EXPRESSION -> when {
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "low_expression did not end on OTHER"
                result.tacticalDecision.decisionType != TacticalDecisionType.PASSIVE_NOT_READY -> "low_expression did not wait for cloud playbook"
                result.routes.isNotEmpty() -> "low_expression exposed local passive routes"
                else -> null
            }
        }
    }

    private fun buildReport(rows: List<FontScaleRow>): String {
        val passed = rows.count { it.failReason == null }
        val failed = rows.size - passed
        return buildString {
            appendLine("# MockChatLab FontScale Matrix Report")
            appendLine()
            appendLine("- generatedAt: $GENERATED_AT")
            appendLine("- mockchatVersion: v4.1.5-fontscale")
            appendLine("- huiyiVersion: v4.1.5-debug")
            appendLine("- sample_source: emulator_mock_chat_accessibility")
            appendLine("- totalProfiles: ${FontMatrixProfile.entries.size}")
            appendLine("- totalScenarios: ${rows.size}")
            appendLine("- passed: $passed")
            appendLine("- failed: $failed")
            appendLine("- coversLargeFont: true")
            appendLine("- includesRealHuaweiLargeTextProfile: true")
            appendLine()
            rows.forEach { row ->
                val messages = row.result.captureResult?.messages.orEmpty()
                val metadataCount = messages.count { it.metadataType != MetadataType.NONE || it.speaker == Speaker.SYSTEM }
                val candidateCount = messages.count { it.metadataType == MetadataType.NONE && it.speaker != Speaker.SYSTEM }
                val unknownCount = messages.count { it.metadataType == MetadataType.NONE && it.speaker == Speaker.UNKNOWN }
                val effectiveCount = messages.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
                appendLine("- profileName: ${row.profile.name}")
                appendLine("  scenarioName: ${row.scenario.id}")
                appendLine("  fontScale: ${row.fontScale.scale}")
                appendLine("  screenWidth: ${row.profile.screenWidth}")
                appendLine("  screenHeight: ${row.profile.screenHeight}")
                appendLine("  parsedMessageCount: ${messages.size}")
                appendLine("  metadataFilteredCount: $metadataCount")
                appendLine("  candidateChatMessageCount: $candidateCount")
                appendLine("  unknownSpeakerCount: $unknownCount")
                appendLine("  effectiveMessageCount: $effectiveCount")
                appendLine("  lastEffectiveSpeaker: ${row.result.lastSpeakerDecision.lastSpeaker}")
                appendLine("  decisionType: ${row.result.tacticalDecision.decisionType}")
                appendLine("  routeCount: ${row.result.routes.size}")
                appendLine("  result: ${if (row.failReason == null) "PASS" else "FAIL"}")
                appendLine("  failReason: ${row.failReason ?: "none"}")
            }
        }
    }

    private fun outputDirectory(): File {
        val output = if (File("settings.gradle.kts").exists()) File("outputs") else File("../outputs")
        return output.canonicalFile.apply { mkdirs() }
    }

    private companion object {
        const val GENERATED_AT = 1783094400000L
        val NATURAL_LANGUAGE_SAMPLES = setOf(
            "你爱吃什么",
            "好啊，乖乖，我不跟你聊了，拜拜。",
            "😊好",
            "今天我们要开个会，月底最后一天……",
            "看你挺忙的，忙完注意休息哈"
        )
    }
}

private data class FontScaleRow(
    val profile: FontMatrixProfile,
    val scenario: FontMatrixScenario,
    val fontScale: FontScaleProfile,
    val result: CurrentScreenPipelineResult,
    val failReason: String?
)

private enum class FontScaleProfile(val id: String, val scale: Float) {
    NORMAL("font_normal", 1.0f),
    LARGE("font_large", 1.15f),
    EXTRA_LARGE("font_extra_large", 1.3f),
    HUGE("font_huge", 1.5f)
}

private enum class FontMatrixProfile(
    val id: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val density: Float,
    val left: Int,
    val right: Int,
    val width: Int
) {
    WECHAT_LIKE("wechat_like", 1080, 2400, 3.0f, 56, 470, 560),
    QQ_LIKE("qq_like", 1080, 2400, 3.0f, 70, 455, 540),
    REDBOOK_DM_LIKE("redbook_like", 1080, 2400, 3.0f, 48, 500, 520),
    DATING_APP_LIKE("dating_like", 1080, 2400, 3.0f, 82, 480, 530),
    MINIMAL_CHAT_LIKE("minimal_like", 1080, 2400, 3.0f, 120, 620, 360),
    LIAOQI_HUAWEI_LARGE_TEXT("liaoqi_huawei_large_text", 1084, 2302, 3.0f, 58, 430, 596)
}

private enum class FontMatrixScenario(val id: String) {
    LAST_ME("last_me"),
    LAST_OTHER("last_other"),
    METADATA_TRAP("metadata_trap"),
    LONG_MULTILINE("long_multiline"),
    LOW_EXPRESSION("low_expression")
}

private fun FontMatrixScenario.bubbles(profile: FontMatrixProfile, fontScale: FontScaleProfile): List<VisualBubble> {
    var y = (140 * fontScale.scale).toInt()
    fun next(height: Int): Int {
        val scaledHeight = (height * fontScale.scale).toInt()
        val top = y
        y += scaledHeight + (18 * fontScale.scale).toInt()
        return top
    }

    fun bubble(
        id: String,
        text: String,
        left: Int,
        width: Int,
        height: Int = 112,
        rowLeft: Int = 0,
        rowRight: Int = profile.screenWidth
    ): VisualBubble {
        val top = next(height)
        val scaledHeight = (height * fontScale.scale).toInt()
        val bubbleBounds = VisualBounds(left, top, left + width, top + scaledHeight)
        val textInflation = if (fontScale.scale >= 1.3f) (profile.screenWidth * 0.08f).toInt() else 0
        val textBounds = VisualBounds(
            (bubbleBounds.left + 16 - textInflation).coerceAtLeast(0),
            bubbleBounds.top + 10,
            (bubbleBounds.right - 16 + textInflation).coerceAtMost(profile.screenWidth),
            bubbleBounds.bottom - 10
        )
        val rowBounds = VisualBounds(rowLeft, top, rowRight, top + scaledHeight)
        val parentBounds = VisualBounds(rowLeft, top - 4, rowRight, top + scaledHeight + 4)
        return VisualBubble(
            id = id,
            text = text,
            bubbleBounds = bubbleBounds,
            rowBounds = rowBounds,
            textBounds = textBounds,
            parentBounds = parentBounds,
            ancestorBoundsChain = listOf(VisualBounds(0, 0, profile.screenWidth, profile.screenHeight), parentBounds)
        )
    }

    fun other(id: String, text: String, height: Int = 112): VisualBubble {
        return bubble(id, text, profile.left, profile.width, height, 0, (profile.screenWidth * 0.72f).toInt())
    }

    fun me(id: String, text: String, height: Int = 112): VisualBubble {
        return bubble(id, text, profile.right, profile.width, height, (profile.screenWidth * 0.28f).toInt(), profile.screenWidth)
    }

    fun time(text: String): VisualBubble {
        val width = 168
        val left = (profile.screenWidth - width) / 2
        return bubble("time-$text-$y", text, left, width, 44, 0, profile.screenWidth)
    }

    fun date(text: String): VisualBubble {
        val width = 250
        val left = (profile.screenWidth - width) / 2
        return bubble("date-$text-$y", text, left, width, 44, 0, profile.screenWidth)
    }

    val base = mutableListOf(
        bubble("header-title", "白云蓝天", 370, 340, 44),
        bubble("header-status", "上次在线时间07-02 18:06", 330, 430, 38)
    )
    y = (230 * fontScale.scale).toInt()
    when (this) {
        FontMatrixScenario.LAST_ME -> {
            base += time("10:56")
            base += other("o1", "你爱吃什么")
            base += me("m1", "好啊，乖乖，我不跟你聊了，拜拜。")
        }
        FontMatrixScenario.LAST_OTHER -> {
            base += realHuaweiThread(::time, ::other, ::me)
        }
        FontMatrixScenario.METADATA_TRAP -> {
            base += date("2026-07-03")
            base += time("10:56")
            base += other("o1", "你爱吃什么")
            base += me("m1", "好啊，乖乖，我不跟你聊了，拜拜。")
            base += time("10:58")
            base += other("o2", "今天我们要开个会，月底最后一天……", 150)
            base += time("10:59")
        }
        FontMatrixScenario.LONG_MULTILINE -> {
            base += time("10:56")
            base += me("m1", "你先忙，我在。")
            base += other("o1", "今天我们要开个会，月底最后一天……\n事情有点多，我可能会晚点回你\n看你挺忙的，忙完注意休息哈", 190)
        }
        FontMatrixScenario.LOW_EXPRESSION -> {
            base += time("10:56")
            base += other("o1", "嗯")
            base += other("o2", "好")
            base += other("o3", "没事")
            base += other("o4", "忙")
            base += other("o5", "晚点说")
        }
    }
    return base
}

private fun realHuaweiThread(
    time: (String) -> VisualBubble,
    other: (String, String, Int) -> VisualBubble,
    me: (String, String, Int) -> VisualBubble
): List<VisualBubble> = listOf(
    time("10:56"),
    other("o1", "你爱吃什么", 112),
    me("m1", "好啊，乖乖，我不跟你聊了，拜拜。", 112),
    other("o2", "😊好", 112),
    me("m2", "😆吃饭前唱军歌，不如你唱的好听", 132),
    time("10:58"),
    other("o3", "为什么给我发这个😂五分钟的视频才看完", 132),
    other("o4", "今天我们要开个会，月底最后一天……", 150),
    time("10:59"),
    other("o5", "看你挺忙的，忙完注意休息哈", 112)
)
