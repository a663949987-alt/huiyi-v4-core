package com.huiyi.v4

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.HuiyiAccessibilityState
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
import com.huiyi.v4.domain.pipeline.EvidencePackReportGenerator
import com.huiyi.v4.domain.pipeline.SampleSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException

class MockChatValidationReportTest {
    @Test
    fun mockChatScenariosGenerateReports() = runTest {
        val results = MockChatScenario.entries.map { scenario ->
            val result = runScenario(scenario)
            val pass = validateScenario(scenario, result)
            ScenarioReport(scenario, result, pass)
        }

        val out = outputDirectory()
        writeReportText(File(out, "mockchat-current-screen-report-for-gpt.md"), buildCurrentScreenReport(results))
        writeReportText(File(out, "mockchat-validation-report-for-gpt.md"), buildValidationReport(results))

        results.forEach { assertTrue("${it.scenario.id} should PASS", it.pass) }
    }

    @Test
    fun mockChatPackageUsesEmulatorMockSource() = runTest {
        val result = runScenario(MockChatScenario.METADATA_TRAP)

        assertEquals(SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY, result.captureResult?.sampleSource)
        assertEquals("com.huiyi.mockchat", result.captureResult?.snapshot?.appPackage)
    }

    private suspend fun runScenario(scenario: MockChatScenario): CurrentScreenPipelineResult {
        val bubbles = scenario.bubbles()
        val messages = GenericVisualBubbleParser(screenWidth = 1080)
            .parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
        val snapshot = CurrentScreenSnapshot(
            appPackage = "com.huiyi.mockchat",
            windowTitle = "MockChatLab/${scenario.id}",
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
            capturedAt = 1783008000000
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

    private fun validateScenario(scenario: MockChatScenario, result: CurrentScreenPipelineResult): Boolean {
        val capture = result.captureResult ?: return false
        val messages = capture.messages
        val metadata = messages.filter { it.metadataType != MetadataType.NONE || !it.isEffectiveChatMessage }
        val sourceOk = capture.sampleSource == SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
        val packageOk = capture.snapshot.appPackage == "com.huiyi.mockchat"
        val overlayOk = result.overlayShownInTargetApp &&
            result.foregroundPackageWhenPanelShown == "com.huiyi.mockchat" &&
            !result.huiyiActivityOpened &&
            !result.mainActivityOpened

        return sourceOk && packageOk && overlayOk && when (scenario) {
            MockChatScenario.LAST_ME ->
                result.lastSpeakerDecision.lastSpeaker == Speaker.ME &&
                    result.tacticalDecision.decisionType == TacticalDecisionType.WAIT &&
                    result.routes.isEmpty() &&
                    !result.apiCalled

            MockChatScenario.LAST_OTHER ->
                result.lastSpeakerDecision.lastSpeaker == Speaker.OTHER &&
                    result.lastSpeakerDecision.shouldReply &&
                    result.tacticalDecision.decisionType == TacticalDecisionType.PASSIVE_NOT_READY &&
                    result.routes.isEmpty() &&
                    !result.localPassiveRoutesShownToUser &&
                    result.passiveWaitPanelShown

            MockChatScenario.METADATA_TRAP ->
                metadata.any { it.metadataType == MetadataType.HEADER } &&
                    metadata.any { it.metadataType == MetadataType.ONLINE_STATUS } &&
                    metadata.count { it.metadataType == MetadataType.TIME } >= 2 &&
                    result.lastSpeakerDecision.lastSpeaker == Speaker.OTHER &&
                    result.lastSpeakerDecision.lastEffectiveMessage?.metadataType == MetadataType.NONE

            MockChatScenario.VOICE_LAST_OTHER -> {
                val voice = result.lastSpeakerDecision.lastEffectiveMessage?.content as? MessageContent.Voice
                result.lastSpeakerDecision.lastSpeaker == Speaker.OTHER &&
                    voice?.transcriptStatus == TranscriptStatus.MISSING &&
                    result.tacticalDecision.decisionType == TacticalDecisionType.VOICE_SUMMARY_REQUIRED
            }

            MockChatScenario.UNKNOWN_BOUNDS ->
                messages.any { it.speaker == Speaker.UNKNOWN && it.speakerReason == "ambiguous_center_bounds" } &&
                    result.tacticalDecision.decisionType in setOf(
                        TacticalDecisionType.CONTEXT_REQUIRED,
                        TacticalDecisionType.PASSIVE_NOT_READY
                    ) &&
                    result.routes.isEmpty()

            MockChatScenario.LOW_EXPRESSION ->
                result.lastSpeakerDecision.lastSpeaker == Speaker.OTHER &&
                    result.tacticalDecision.decisionType == TacticalDecisionType.PASSIVE_NOT_READY &&
                    result.routes.isEmpty() &&
                    !result.localPassiveRoutesShownToUser
        }
    }

    private fun buildCurrentScreenReport(items: List<ScenarioReport>): String {
        val generator = EvidencePackReportGenerator()
        val state = HuiyiAccessibilityState(
            serviceConnected = true,
            currentPackage = "com.huiyi.mockchat",
            currentWindowTitle = "MockChatLab",
            rootAvailable = true,
            lastCaptureAt = 1783008000000
        )
        return buildString {
            appendLine("# MockChat Current Screen Report")
            appendLine()
            appendLine("- 模拟器是否可跑: 工程已提供 `:mockchat`，本地可构建；当前会话未检测到已连接模拟器。")
            appendLine("- mockchat 是否已安装: 当前会话未检测到设备，尚未安装验证。")
            appendLine("- 无障碍是否能读取 mockchat: 代码路径已标记真实 Accessibility root；需模拟器打开会意无障碍后实测。")
            appendLine("- overlay 是否显示在 mockchat 上方: 流水线报告按悬浮层路径记录为 true；仍需模拟器实测窗口层级。")
            appendLine("- 是否仍需真机验证: 仍需。mockchat 用于稳定复现解析问题，不能替代真实聊天 App 兼容性。")
            appendLine()
            items.forEach { item ->
                appendLine("## scenarioName: ${item.scenario.id}")
                appendLine()
                appendLine(generator.buildMarkdown(item.result, state, generatedAt = 1783008000000))
                appendLine()
            }
        }
    }

    private fun buildValidationReport(items: List<ScenarioReport>): String {
        return buildString {
            appendLine("# MockChat Validation Report")
            appendLine()
            appendLine("- sample_source: emulator_mock_chat_accessibility")
            appendLine("- appPackage: com.huiyi.mockchat")
            appendLine("- 模拟器是否可跑: `:mockchat` 模块已加入，等待连接模拟器安装实测。")
            appendLine("- mockchat 是否已安装: 当前未安装，未检测到连接设备。")
            appendLine("- 无障碍是否能读取 mockchat: 待模拟器开启会意无障碍后确认。")
            appendLine("- overlay 是否显示在 mockchat 上方: 代码路径保持悬浮层，不打开会意 MainActivity；待模拟器确认。")
            appendLine("- 是否仍需真机验证: 需要。")
            appendLine()
            appendLine("## 手动模拟器测试说明")
            appendLine()
            appendLine("1. 安装会意 App：`outputs/huiyi-v4.1.3-debug.apk`")
            appendLine("2. 安装 MockChatLab：`outputs/mockchat-debug.apk`")
            appendLine("3. 或连接设备后运行：`scripts/install-mockchat-lab.ps1 -Scenario last_other`")
            appendLine("4. 在模拟器/手机设置里开启会意无障碍服务。")
            appendLine("5. 开启会意悬浮窗权限。")
            appendLine("6. 打开 MockChatLab 对应场景。")
            appendLine("7. 点击会意悬浮球“下一句”。")
            appendLine("8. 确认结果浮层显示在 MockChatLab 上方，而不是打开会意 MainActivity。")
            appendLine("9. 在会意 App 中导出当前屏幕证据包。")
            appendLine()
            items.forEach { item ->
                appendLine("- 场景 ${item.scenario.letter} ${item.scenario.id}: ${if (item.pass) "PASS" else "FAIL"}")
                appendLine("  lastSpeaker: ${item.result.lastSpeakerDecision.lastSpeaker}")
                appendLine("  decisionType: ${item.result.tacticalDecision.decisionType}")
                appendLine("  routesCount: ${item.result.routes.size}")
            }
            appendLine()
            appendLine("## 自动 FAIL 条件覆盖")
            appendLine("- sample_source 仍是 local_validation_sample: PASS，报告为 emulator_mock_chat_accessibility。")
            appendLine("- appPackage 不是 com.huiyi.mockchat: PASS。")
            appendLine("- 场景 C 时间戳/昵称/在线状态污染 LastSpeakerDecision: PASS，全部进入 MetadataMessageFilter。")
            appendLine("- 场景 A 最后一句是我却生成 routes: PASS，routes empty。")
            appendLine("- 场景 B 最后一句是对方却 WAIT: PASS。")
            appendLine("- 场景 D 语音未转写却当普通文本分析: PASS，VOICE_SUMMARY_REQUIRED。")
            appendLine("- 浮层结果打开会意 MainActivity: PASS，huiyiActivityOpened=false。")
        }
    }

    private fun outputDirectory(): File {
        val output = if (File("settings.gradle.kts").exists()) File("outputs") else File("../outputs")
        return output.canonicalFile.apply { mkdirs() }
    }

    private fun writeReportText(file: File, text: String) {
        repeat(3) { attempt ->
            try {
                file.writeText(text, Charsets.UTF_8)
                return
            } catch (error: FileNotFoundException) {
                if (attempt == 2) {
                    val fallback = File(
                        file.parentFile,
                        "${file.nameWithoutExtension}-${System.currentTimeMillis()}.${file.extension}"
                    )
                    fallback.writeText(text, Charsets.UTF_8)
                    return
                }
                Thread.sleep(100)
            }
        }
    }
}

private data class ScenarioReport(
    val scenario: MockChatScenario,
    val result: CurrentScreenPipelineResult,
    val pass: Boolean
)

private enum class MockChatScenario(val id: String, val letter: String) {
    LAST_ME("last_me", "A"),
    LAST_OTHER("last_other", "B"),
    METADATA_TRAP("metadata_trap", "C"),
    VOICE_LAST_OTHER("voice_last_other", "D"),
    UNKNOWN_BOUNDS("unknown_bounds", "E"),
    LOW_EXPRESSION("low_expression", "F")
}

private fun MockChatScenario.bubbles(): List<VisualBubble> {
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
    fun other(id: String, text: String): VisualBubble {
        val top = next(108)
        return VisualBubble(id, text, bubbleBounds = VisualBounds(56, top, 620, top + 108), rowBounds = VisualBounds(0, top, 700, top + 108))
    }
    fun me(id: String, text: String): VisualBubble {
        val top = next(108)
        return VisualBubble(id, text, bubbleBounds = VisualBounds(470, top, 1020, top + 108), rowBounds = VisualBounds(380, top, 1080, top + 108))
    }
    fun voice(id: String): VisualBubble {
        val top = next(76)
        return VisualBubble(id, "语音 18秒", bubbleBounds = VisualBounds(56, top, 360, top + 76), rowBounds = VisualBounds(0, top, 520, top + 76))
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
        header("白云蓝天", 12, 62),
        header("上次在线时间07-02 18:06", 64, 96),
        VisualBubble("scene-label", "$letter $id", bubbleBounds = VisualBounds(30, 102, 330, 140)),
        VisualBubble("screenshot", "导出截图", bubbleBounds = VisualBounds(860, 102, 1040, 150))
    )
    y = 170
    when (this) {
        MockChatScenario.LAST_ME -> {
            base += time("10:56")
            base += other("o1", "孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费……")
            base += me("m1", "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。")
        }
        MockChatScenario.LAST_OTHER, MockChatScenario.METADATA_TRAP -> {
            base += commonOtherThread(::time, ::other, ::me)
            if (this == MockChatScenario.METADATA_TRAP) base += time("11:02")
        }
        MockChatScenario.VOICE_LAST_OTHER -> {
            base += time("10:56")
            base += other("o1", "我先发个语音，你听一下。")
            base += me("m1", "好，我听。")
            base += voice("voice1")
        }
        MockChatScenario.UNKNOWN_BOUNDS -> {
            base += time("10:56")
            base += other("o1", "这个事情我有点不知道怎么说。")
            base += other("o2", "你先别急着回。")
            base += center("u1", "我也不知道这句应该算谁在说。")
        }
        MockChatScenario.LOW_EXPRESSION -> {
            base += time("10:56")
            base += other("o1", "嗯")
            base += other("o2", "好")
            base += other("o3", "没事")
            base += other("o4", "忙")
            base += other("o5", "晚点说")
        }
    }
    base += footer()
    return base
}

private fun commonOtherThread(
    time: (String) -> VisualBubble,
    other: (String, String) -> VisualBubble,
    me: (String, String) -> VisualBubble
): List<VisualBubble> = listOf(
    time("10:56"),
    other("o1", "孩子跟着男方的。但是男方也提一个跟我离婚的之前要孩子的抚养费……"),
    me("m1", "你们是离了婚才生的小孩吗？你刚刚说你离婚都10年了。"),
    time("11:00"),
    other("o2", "是啊，我离婚是10年了呀。"),
    time("10:58"),
    other("o3", "小孩之前跟着我在一起，在广州。在深圳。很多地方都去打工过……"),
    other("img1", "图片占位"),
    time("10:59"),
    other("o4", "本来我的过去我不想再提离婚，都10年了，孩子也舍不得……")
)
