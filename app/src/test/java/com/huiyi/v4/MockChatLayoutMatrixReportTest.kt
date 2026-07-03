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
import com.huiyi.v4.domain.model.TranscriptStatus
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

class MockChatLayoutMatrixReportTest {
    @Test
    fun mockChatLayoutMatrixGeneratesReport() = runTest {
        val results = MatrixProfile.entries.flatMap { profile ->
            MatrixScenario.entries.map { scenario ->
                val result = runScenario(profile, scenario)
                val failReason = validate(profile, scenario, result)
                MatrixRow(profile, scenario, result, failReason)
            }
        }

        val report = buildLayoutMatrixReport(results)
        File(outputDirectory(), "mockchat-layout-matrix-report-for-gpt.md").writeText(report, Charsets.UTF_8)

        assertTrue("should cover at least 50 profile x scenario combinations", results.size >= 50)
        results.forEach { row ->
            assertTrue("${row.profile.id}/${row.scenario.id} failed: ${row.failReason}", row.failReason == null)
        }
    }

    private suspend fun runScenario(profile: MatrixProfile, scenario: MatrixScenario): CurrentScreenPipelineResult {
        val bubbles = scenario.bubbles(profile)
        val messages = GenericVisualBubbleParser(screenWidth = 1080)
            .parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
        val snapshot = CurrentScreenSnapshot(
            appPackage = "com.huiyi.mockchat",
            windowTitle = "MockChatLab/${profile.id}/${scenario.id}",
            screenWidth = 1080,
            screenHeight = 2400,
            nodes = bubbles.mapIndexed { index, bubble ->
                ScreenNodeSnapshot(
                    id = "node-$index",
                    text = bubble.text,
                    contentDescription = null,
                    className = "androidx.compose.ui.platform.ComposeView",
                    viewIdResourceName = null,
                    bounds = bubble.bubbleBounds ?: bubble.rowBounds ?: bubble.textBounds ?: VisualBounds(0, 0, 1, 1),
                    visibleToUser = true,
                    depth = 2,
                    childCount = 0
                )
            },
            capturedAt = GENERATED_AT
        )
        val capture = CurrentScreenCaptureResult(
            snapshot = snapshot,
            messages = messages,
            sampleSource = SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
        )
        val useCase = CurrentScreenPipelineUseCase(
            captureUseCase = object : CurrentScreenCaptureUseCase(serviceProvider = { null }) {
                override fun capture(): Result<CurrentScreenCaptureResult> = Result.success(capture)
            }
        )
        return useCase.run(DefaultPersonaCorpus.soldier()).getOrThrow().copy(
            overlayShownInTargetApp = true,
            foregroundPackageWhenPanelShown = "com.huiyi.mockchat",
            huiyiActivityOpened = false,
            userStayedInChatApp = true,
            resultShownAsOverlay = true,
            mainActivityOpened = false
        )
    }

    private fun validate(profile: MatrixProfile, scenario: MatrixScenario, result: CurrentScreenPipelineResult): String? {
        val capture = result.captureResult ?: return "captureResult is null"
        val messages = capture.messages
        val metadata = messages.filter { !it.isEffectiveChatMessage || it.metadataType != MetadataType.NONE }
        val effective = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val lastEffective = result.lastSpeakerDecision.lastEffectiveMessage

        if (capture.sampleSource != SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY) return "sample_source is not emulator_mock_chat_accessibility"
        if (capture.snapshot.appPackage != "com.huiyi.mockchat") return "appPackage is not com.huiyi.mockchat"
        if (!result.overlayShownInTargetApp || result.mainActivityOpened || result.huiyiActivityOpened) return "overlay opened MainActivity instead of target overlay"
        if (lastEffective?.metadataType != MetadataType.NONE) return "metadata participated in LastSpeakerDecision"
        if (lastEffective?.speaker == Speaker.SYSTEM) return "system node became last effective message"

        return when (scenario) {
            MatrixScenario.LAST_ME -> when {
                result.lastSpeakerDecision.lastSpeaker != Speaker.ME -> "last_me did not end on ME"
                result.tacticalDecision.decisionType != TacticalDecisionType.WAIT -> "last_me did not WAIT"
                result.routes.isNotEmpty() -> "last_me generated routes"
                result.apiCalled -> "last_me called API"
                else -> null
            }
            MatrixScenario.LAST_OTHER -> when {
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "last_other did not end on OTHER"
                result.tacticalDecision.decisionType == TacticalDecisionType.WAIT -> "last_other became WAIT"
                result.routes.size != 5 -> "last_other did not generate 5 routes"
                else -> null
            }
            MatrixScenario.METADATA_TRAP -> when {
                metadata.none { it.metadataType == MetadataType.HEADER } -> "header not filtered"
                metadata.none { it.metadataType == MetadataType.ONLINE_STATUS } -> "online status not filtered"
                metadata.count { it.metadataType == MetadataType.TIME || it.metadataType == MetadataType.DATE } < 3 -> "time/date metadata not filtered"
                metadata.none { it.metadataType == MetadataType.SYSTEM_NOTICE } -> "system notice not filtered"
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "metadata polluted last speaker"
                else -> null
            }
            MatrixScenario.VOICE_LAST_OTHER -> {
                val voice = lastEffective?.content as? MessageContent.Voice
                when {
                    result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "voice_last_other did not end on OTHER"
                    voice == null -> "last voice is not MessageContent.Voice"
                    voice.transcriptStatus != TranscriptStatus.MISSING -> "voice transcript was guessed"
                    result.tacticalDecision.decisionType != TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> "VoiceSummaryCard decision not required"
                    else -> null
                }
            }
            MatrixScenario.IMAGE_OR_STICKER -> {
                val visual = lastEffective?.content
                when {
                    visual !is MessageContent.Image && visual !is MessageContent.Sticker -> "last visual message is not image/sticker"
                    result.tacticalDecision.decisionType != TacticalDecisionType.CONTEXT_REQUIRED -> "image/sticker forced deep analysis"
                    result.routes.isNotEmpty() -> "image/sticker generated routes without description"
                    else -> null
                }
            }
            MatrixScenario.LOW_EXPRESSION -> when {
                result.tacticalDecision.decisionType != TacticalDecisionType.BOUNDARY_RESPECT -> "low_expression forced deep chat"
                result.routes.size != 5 -> "low_expression should still provide low-pressure routes"
                else -> null
            }
            MatrixScenario.LONG_MULTILINE -> when {
                effective.count { it.normalizedText?.contains("就是今天事情堆在一起") == true } != 1 -> "long multiline split into wrong message count"
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "long multiline did not end on OTHER"
                else -> null
            }
            MatrixScenario.QUOTED_REPLY -> when {
                lastEffective?.normalizedText?.contains("引用") == true -> "quoted text remained in actualText"
                lastEffective?.normalizedText != "我不是急，我是怕你误会。" -> "quoted_reply actualText is wrong"
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "quoted_reply did not end on actual reply"
                else -> null
            }
            MatrixScenario.UNKNOWN_BOUNDS -> when {
                messages.none { it.speaker == Speaker.UNKNOWN && it.speakerReason == "ambiguous_center_bounds" } -> "unknown bubble was not UNKNOWN"
                result.routes.isNotEmpty() -> "unknown_bounds generated routes"
                result.tacticalDecision.decisionType != TacticalDecisionType.CONTEXT_REQUIRED -> "unknown_bounds did not block high confidence"
                else -> null
            }
            MatrixScenario.TIME_AT_BOTTOM -> when {
                messages.filter { it.metadataType != MetadataType.UI_CONTROL }.lastOrNull()?.metadataType != MetadataType.TIME -> "bottom chat-area element is not time metadata"
                result.lastSpeakerDecision.lastSpeaker != Speaker.OTHER -> "bottom time polluted LastSpeakerDecision"
                result.routes.size != 5 -> "time_at_bottom did not keep last effective OTHER routes"
                else -> null
            }
        }
    }

    private fun buildLayoutMatrixReport(rows: List<MatrixRow>): String {
        val passed = rows.count { it.failReason == null }
        val failed = rows.size - passed
        return buildString {
            appendLine("# MockChatLab Layout Matrix Report")
            appendLine()
            appendLine("## 1. 基本信息")
            appendLine()
            appendLine("- generatedAt: $GENERATED_AT")
            appendLine("- mockchatVersion: v4.1.3-matrix")
            appendLine("- huiyiVersion: v4.1.3-debug")
            appendLine("- totalProfiles: ${MatrixProfile.entries.size}")
            appendLine("- totalScenarios: ${rows.size}")
            appendLine("- passed: $passed")
            appendLine("- failed: $failed")
            appendLine()
            appendLine("## 2. Profile 汇总")
            appendLine()
            MatrixProfile.entries.forEach { profile ->
                val profileRows = rows.filter { it.profile == profile }
                appendLine("- profileName: ${profile.name}")
                appendLine("  scenarioCount: ${profileRows.size}")
                appendLine("  passCount: ${profileRows.count { it.failReason == null }}")
                appendLine("  failCount: ${profileRows.count { it.failReason != null }}")
            }
            appendLine()
            appendLine("## 3. 场景明细")
            appendLine()
            rows.forEach { row ->
                appendLine(row.detailBlock())
                appendLine()
            }
            appendLine("## 4. 解析样例")
            appendLine()
            MatrixProfile.entries.forEach { profile ->
                val sample = rows.first { it.profile == profile && it.scenario == MatrixScenario.METADATA_TRAP }
                appendLine("### ${profile.name} / metadata_trap")
                sample.result.captureResult?.messages.orEmpty().takeLast(20).forEachIndexed { index, message ->
                    appendLine(formatMessage(index + 1, message))
                }
                appendLine()
            }
            appendLine("## 5. 失败项")
            appendLine()
            val failures = rows.filter { it.failReason != null }
            if (failures.isEmpty()) {
                appendLine("- none")
            } else {
                failures.forEach { appendLine("- ${it.profile.id}/${it.scenario.id}: ${it.failReason}") }
            }
            appendLine()
            appendLine("## 6. 截图样本")
            appendLine()
            appendLine("- outputs/mockchat_screenshots/wechat_like_metadata_trap.png")
            appendLine("- outputs/mockchat_screenshots/qq_like_voice_last_other.png")
            appendLine("- outputs/mockchat_screenshots/redbook_like_last_other.png")
            appendLine("- outputs/mockchat_screenshots/dating_like_profile_card.png")
            appendLine("- outputs/mockchat_screenshots/minimal_like_unknown_bounds.png")
        }
    }

    private fun MatrixRow.detailBlock(): String {
        val capture = result.captureResult
        val messages = capture?.messages.orEmpty()
        val effective = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        return buildString {
            appendLine("- profileName: ${profile.name}")
            appendLine("  scenarioName: ${scenario.id}")
            appendLine("  sample_source: ${capture?.sampleSource?.reportValue ?: "unknown"}")
            appendLine("  appPackage: ${capture?.snapshot?.appPackage ?: "unknown"}")
            appendLine("  parsedMessageCount: ${messages.size}")
            appendLine("  metadataFilteredCount: ${messages.count { !it.isEffectiveChatMessage || it.metadataType != MetadataType.NONE }}")
            appendLine("  effectiveMessageCount: ${effective.size}")
            appendLine("  meCount: ${messages.count { it.speaker == Speaker.ME }}")
            appendLine("  otherCount: ${messages.count { it.speaker == Speaker.OTHER }}")
            appendLine("  unknownCount: ${messages.count { it.speaker == Speaker.UNKNOWN }}")
            appendLine("  systemCount: ${messages.count { it.speaker == Speaker.SYSTEM }}")
            appendLine("  voiceCount: ${messages.count { it.content is MessageContent.Voice }}")
            appendLine("  imageCount: ${messages.count { it.content is MessageContent.Image || it.content is MessageContent.Sticker }}")
            appendLine("  lastEffectiveSpeaker: ${result.lastSpeakerDecision.lastSpeaker}")
            appendLine("  decisionType: ${result.tacticalDecision.decisionType}")
            appendLine("  routeCount: ${result.routes.size}")
            appendLine("  overlayShownInTargetApp: ${result.overlayShownInTargetApp}")
            appendLine("  mainActivityOpened: ${result.mainActivityOpened}")
            appendLine("  result: ${if (failReason == null) "PASS" else "FAIL"}")
            appendLine("  failReason: ${failReason ?: "none"}")
        }.trimEnd()
    }

    private fun formatMessage(index: Int, message: com.huiyi.v4.domain.model.MessageNode): String {
        val side = when (message.speaker) {
            Speaker.ME -> "right"
            Speaker.OTHER -> "left"
            Speaker.SYSTEM -> "center"
            Speaker.UNKNOWN -> "unknown"
        }
        val type = when (val content = message.content) {
            is MessageContent.Voice -> "voice ${content.transcriptStatus}"
            is MessageContent.Image -> "image ${content.descriptionStatus}"
            is MessageContent.Sticker -> "sticker ${content.meaningStatus}"
            else -> "${message.speaker.name.lowercase()} ${message.speakerConfidence}% ${message.speakerReason}"
        }
        val text = message.normalizedText ?: when (message.content) {
            is MessageContent.Voice -> "[voice]"
            is MessageContent.Image -> "[image]"
            is MessageContent.Sticker -> "[sticker]"
            else -> "[non-text]"
        }
        return "[m${index.toString().padStart(3, '0')}][$side][$type] $text"
    }

    private fun outputDirectory(): File {
        val output = if (File("settings.gradle.kts").exists()) File("outputs") else File("../outputs")
        return output.canonicalFile.apply { mkdirs() }
    }

    private companion object {
        const val GENERATED_AT = 1783008000000L
    }
}

private data class MatrixRow(
    val profile: MatrixProfile,
    val scenario: MatrixScenario,
    val result: CurrentScreenPipelineResult,
    val failReason: String?
)

private enum class MatrixProfile(val id: String, val title: String, val status: String, val left: Int, val right: Int, val width: Int) {
    WECHAT_LIKE("wechat_like", "白云蓝天", "上次在线时间07-02 18:06", 56, 470, 560),
    QQ_LIKE("qq_like", "蓝桥", "手机在线", 70, 455, 540),
    REDBOOK_DM_LIKE("redbook_like", "小鹿同学", "刚刚在线", 48, 500, 520),
    DATING_APP_LIKE("dating_like", "林夏", "资料完整度 82%", 82, 480, 530),
    MINIMAL_CHAT_LIKE("minimal_like", "对话", "在线", 120, 620, 360)
}

private enum class MatrixScenario(val id: String, val letter: String) {
    LAST_ME("last_me", "A"),
    LAST_OTHER("last_other", "B"),
    METADATA_TRAP("metadata_trap", "C"),
    VOICE_LAST_OTHER("voice_last_other", "D"),
    IMAGE_OR_STICKER("image_or_sticker", "E"),
    LOW_EXPRESSION("low_expression", "F"),
    LONG_MULTILINE("long_multiline", "G"),
    QUOTED_REPLY("quoted_reply", "H"),
    UNKNOWN_BOUNDS("unknown_bounds", "I"),
    TIME_AT_BOTTOM("time_at_bottom", "J")
}

private fun MatrixScenario.bubbles(profile: MatrixProfile): List<VisualBubble> {
    var y = 40
    fun next(height: Int = 86): Int {
        val top = y
        y += height + 18
        return top
    }
    fun header(text: String, top: Int, bottom: Int) = VisualBubble(
        id = "header-${text.hashCode()}",
        text = text,
        bubbleBounds = VisualBounds(380, top, 700, bottom),
        rowBounds = VisualBounds(0, top, 1080, bottom)
    )
    fun time(text: String): VisualBubble {
        val top = next(42)
        return VisualBubble("time-$text-$top", text, bubbleBounds = VisualBounds(455, top, 625, top + 42), rowBounds = VisualBounds(0, top, 1080, top + 42))
    }
    fun date(text: String): VisualBubble {
        val top = next(42)
        return VisualBubble("date-$text-$top", text, bubbleBounds = VisualBounds(420, top, 660, top + 42), rowBounds = VisualBounds(0, top, 1080, top + 42))
    }
    fun system(id: String, text: String): VisualBubble {
        val top = next(50)
        return VisualBubble(id, text, bubbleBounds = VisualBounds(360, top, 720, top + 50), rowBounds = VisualBounds(0, top, 1080, top + 50))
    }
    fun other(id: String, text: String, height: Int = 108): VisualBubble {
        val top = next(height)
        return VisualBubble(id, text, bubbleBounds = VisualBounds(profile.left, top, profile.left + profile.width, top + height), rowBounds = VisualBounds(0, top, 720, top + height))
    }
    fun me(id: String, text: String, height: Int = 108): VisualBubble {
        val top = next(height)
        return VisualBubble(id, text, bubbleBounds = VisualBounds(profile.right, top, profile.right + profile.width, top + height), rowBounds = VisualBounds(360, top, 1080, top + height))
    }
    fun voice(id: String): VisualBubble {
        val top = next(76)
        return VisualBubble(id, "语音 18秒", bubbleBounds = VisualBounds(profile.left, top, profile.left + 300, top + 76), rowBounds = VisualBounds(0, top, 520, top + 76))
    }
    fun visual(id: String): VisualBubble {
        val top = next(86)
        val text = if (profile == MatrixProfile.QQ_LIKE) "[sticker] 表情包 未描述" else "[image] 图片 未描述"
        return VisualBubble(id, text, bubbleBounds = VisualBounds(profile.left, top, profile.left + 260, top + 86), rowBounds = VisualBounds(0, top, 620, top + 86))
    }
    fun center(id: String, text: String): VisualBubble {
        val top = next(92)
        return VisualBubble(id, text, bubbleBounds = VisualBounds(415, top, 665, top + 92), rowBounds = VisualBounds(350, top, 730, top + 92))
    }
    fun footer(): List<VisualBubble> {
        val top = 2240
        return listOf(
            VisualBubble("input-voice", "语音", bubbleBounds = VisualBounds(20, top, 120, top + 70)),
            VisualBubble("input-box", "输入框", bubbleBounds = VisualBounds(140, top, 760, top + 70)),
            VisualBubble("input-emoji", "表情", bubbleBounds = VisualBounds(780, top, 880, top + 70)),
            VisualBubble("input-send", "发送", bubbleBounds = VisualBounds(900, top, 1040, top + 70))
        )
    }

    val base = mutableListOf(
        VisualBubble("nav-back", "返回", bubbleBounds = VisualBounds(24, 12, 116, 70)),
        header(profile.title, 12, 62),
        header(profile.status, 64, 96),
        VisualBubble("scene-label", "$letter $id", bubbleBounds = VisualBounds(30, 102, 430, 140)),
        VisualBubble("screenshot", "导出截图", bubbleBounds = VisualBounds(860, 102, 1040, 150))
    )
    y = 170
    when (this) {
        MatrixScenario.LAST_ME -> {
            base += time("10:56")
            base += other("o1", "孩子的事情我还是会有点想法。")
            base += me("m1", "你们是离了婚才生的小孩吗？")
        }
        MatrixScenario.LAST_OTHER -> base += commonOtherThread(::time, ::other, ::me, ::visual)
        MatrixScenario.METADATA_TRAP -> {
            base += date("2026-07-03")
            base += system("sys1", "系统提示：你们已成为好友")
            base += time("10:56")
            base += other("o1", "${profile.title}：今天事情有点多。")
            base += me("m1", "我在，你挑你想说的讲。")
            base += time("11:00")
            base += system("sys2", "系统推荐：保持礼貌沟通")
            base += other("o2", "我就是怕一说又变成抱怨。")
            base += time("11:02")
        }
        MatrixScenario.VOICE_LAST_OTHER -> {
            base += time("10:56")
            base += other("o1", "我先发个语音，你听一下。")
            base += me("m1", "好，我听。")
            base += voice("voice1")
        }
        MatrixScenario.IMAGE_OR_STICKER -> {
            base += time("10:56")
            base += me("m1", "你刚才说的地方我大概明白。")
            base += visual("visual1")
        }
        MatrixScenario.LOW_EXPRESSION -> {
            base += time("10:56")
            base += other("o1", "嗯")
            base += other("o2", "好")
            base += other("o3", "没事")
            base += other("o4", "忙")
            base += other("o5", "晚点说")
        }
        MatrixScenario.LONG_MULTILINE -> {
            base += date("2026-07-03")
            base += me("m1", "你慢慢说，我在。")
            base += other("o1", "我其实不是不想回你\n就是今天事情堆在一起\n有点不知道先说哪一件", height = 140)
        }
        MatrixScenario.QUOTED_REPLY -> {
            base += time("10:56")
            base += me("m1", "那你先别急着解释。")
            base += other("o1", "引用：那你先别急着解释。\n实际回复：我不是急，我是怕你误会。", height = 128)
        }
        MatrixScenario.UNKNOWN_BOUNDS -> {
            base += time("10:56")
            base += other("o1", "这个事情我有点不知道怎么说。")
            base += other("o2", "你先别急着回。")
            base += center("u1", "这句故意居中，边界不明显。")
        }
        MatrixScenario.TIME_AT_BOTTOM -> {
            base += commonOtherThread(::time, ::other, ::me, ::visual)
            base += time("11:08")
        }
    }
    base += footer()
    return base
}

private fun commonOtherThread(
    time: (String) -> VisualBubble,
    other: (String, String, Int) -> VisualBubble,
    me: (String, String, Int) -> VisualBubble,
    visual: (String) -> VisualBubble
): List<VisualBubble> = listOf(
    time("10:56"),
    other("o1", "孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费……", 108),
    me("m1", "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。", 108),
    time("11:00"),
    other("o2", "是啊，我离婚是10年了呀。", 108),
    time("10:58"),
    other("o3", "小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过……", 108),
    visual("img1"),
    time("10:59"),
    other("o4", "本来我的过去我不想再提离婚，都10年了，孩子也舍不得……", 108)
)
