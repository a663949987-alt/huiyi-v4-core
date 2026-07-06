package com.huiyi.v4

import com.huiyi.v4.domain.app.ChatAppProfileDetectionInput
import com.huiyi.v4.domain.app.ChatAppProfileDetector
import com.huiyi.v4.domain.app.ChatAppProfileRegistry
import com.huiyi.v4.domain.app.ChatAppSupportLevel
import com.huiyi.v4.domain.app.UnsupportedAppAdaptationExporter
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.playbook.DynamicPlaybookEngine
import com.huiyi.v4.domain.playbook.DynamicPlaybookMode
import com.huiyi.v4.domain.playbook.DynamicPlaybookRequest
import com.huiyi.v4.domain.playbook.ExpressSelfEligibilityMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiAppProfileMatrixTest {
    private val persona = DefaultPersonaCorpus.soldier()

    @Test
    fun ChatAppProfileRegistryContainsDedicatedGenericAndMockProfilesTest() {
        assertTrue(ChatAppProfileRegistry.profiles.any { it.id == "liaoqi" })
        assertTrue(ChatAppProfileRegistry.profiles.any { it.id == "xiaoenai_generic" })
        assertTrue(ChatAppProfileRegistry.profiles.count { it.packageNames.contains("com.huiyi.mockchat") } >= 5)
        assertEquals(setOf("com.bajiao.im.liaoqi"), ChatAppProfileRegistry.dedicatedPackages())
    }

    @Test
    fun MultiAppProfileMatrixPassesCoreBranchesTest() {
        val rows = matrixRows()
        val detections = rows.associate { row ->
            row.name to ChatAppProfileDetector.detect(
                ChatAppProfileDetectionInput(
                    appPackage = row.appPackage,
                    windowTitle = row.windowTitle,
                    currentAppPackage = row.currentAppPackage ?: row.appPackage,
                    currentWindowTitle = row.windowTitle,
                    messages = row.messages,
                    parserConfidence = row.parserConfidence
                )
            )
        }

        assertEquals(ChatAppSupportLevel.LEVEL_3_DEDICATED_PROFILE, detections.getValue("liaoqi").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, detections.getValue("xiaoenai").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, detections.getValue("wechatLike").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, detections.getValue("qqLike").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, detections.getValue("redbookLike").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL, detections.getValue("datingLike").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_1_UNSUPPORTED_WITH_ADAPTATION_PACK, detections.getValue("webviewLowAccessibility").supportLevel)
        assertTrue(detections.getValue("webviewLowAccessibility").shouldGenerateAdaptationPack)
        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, detections.getValue("launcher").supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, detections.getValue("huiyiOverlay").supportLevel)
    }

    @Test
    fun GenericTrialAllowsConservativeNextSentenceAndExpressSelfForMockStylesTest() {
        val engine = DynamicPlaybookEngine()
        val genericRows = matrixRows().filter {
            it.name in setOf("xiaoenai", "wechatLike", "qqLike", "redbookLike", "datingLike")
        }

        genericRows.forEach { row ->
            val next = engine.nextSentence(request(row, DynamicPlaybookMode.NEXT_SENTENCE))
            val express = engine.expressSelf(request(row, DynamicPlaybookMode.EXPRESS_SELF))
            assertEquals("${row.name} last speaker", Speaker.OTHER, next.lastSpeakerDecision.lastSpeaker)
            assertTrue("${row.name} next sentence should wait for cloud/cache", next.passiveWaitPanelShown)
            assertFalse("${row.name} next sentence must not show local passive routes", next.localPassiveRoutesShownToUser)
            assertEquals("${row.name} express self mode", ExpressSelfEligibilityMode.ALLOW_GENERIC_TRIAL, express.expressSelfEligibility?.mode)
            assertTrue("${row.name} express self routes", express.routes.isNotEmpty())
        }
    }

    @Test
    fun UnsupportedAdaptationPackIsRedactedAndNoRawPrivateChatTest() {
        val row = matrixRows().first { it.name == "webviewLowAccessibility" }
        val detection = ChatAppProfileDetector.detect(
            ChatAppProfileDetectionInput(
                appPackage = row.appPackage,
                windowTitle = row.windowTitle,
                messages = row.messages,
                parserConfidence = row.parserConfidence
            )
        )
        val pack = UnsupportedAppAdaptationExporter().build(
            appPackage = row.appPackage,
            windowTitle = row.windowTitle,
            messages = row.messages,
            reasonWhyUnsupported = detection.reason
        )

        assertTrue(detection.shouldGenerateAdaptationPack)
        assertFalse(pack.screenshotIncluded)
        assertFalse(pack.rawPrivateChatIncluded)
        assertTrue(pack.visibleTextRedacted.all { it.startsWith("[redacted:") || it.isBlank() })
        assertFalse(pack.visibleTextRedacted.joinToString("\n").contains("真实私聊"))
    }

    @Test
    fun LauncherAndOverlayBlockBeforeLastStableAnalysisTest() {
        val launcher = ChatAppProfileDetector.detect(
            ChatAppProfileDetectionInput(
                appPackage = "com.xiaoenai.app",
                windowTitle = "华为桌面",
                currentAppPackage = "com.xiaoenai.app",
                currentWindowTitle = "华为桌面",
                messages = healthyMessages(),
                parserConfidence = 88
            )
        )
        val overlay = ChatAppProfileDetector.detect(
            ChatAppProfileDetectionInput(
                appPackage = "com.bajiao.im.liaoqi",
                windowTitle = "会意雷达 这次不对，发给 GPT 隐藏",
                currentAppPackage = "com.huiyi.v4",
                currentWindowTitle = "会意雷达 这次不对，发给 GPT 隐藏",
                messages = healthyMessages(),
                parserConfidence = 88
            )
        )

        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, launcher.supportLevel)
        assertEquals(ChatAppSupportLevel.LEVEL_0_BLOCK, overlay.supportLevel)
        assertFalse(launcher.targetAppSupported)
        assertFalse(overlay.targetAppSupported)
    }

    private fun request(row: MatrixRow, mode: DynamicPlaybookMode): DynamicPlaybookRequest = DynamicPlaybookRequest(
        mode = mode,
        appPackage = row.appPackage,
        windowTitle = row.windowTitle,
        messages = row.messages,
        personaCorpus = persona,
        capturedAt = 1000L,
        currentTopics = listOf("planning", "reality", "future"),
        chatWindowHash = row.name,
        targetAppSupported = false,
        currentAppPackage = row.currentAppPackage ?: row.appPackage,
        currentWindowTitleRedacted = row.windowTitle,
        parserConfidence = row.parserConfidence
    )

    private fun matrixRows(): List<MatrixRow> = listOf(
        MatrixRow("liaoqi", "com.bajiao.im.liaoqi", "聊起", healthyMessages()),
        MatrixRow("xiaoenai", "com.xiaoenai.app", "小恩爱", healthyMessages()),
        MatrixRow("wechatLike", "com.huiyi.mockchat", "wechat_like", healthyMessages()),
        MatrixRow("qqLike", "com.huiyi.mockchat", "qq_like", healthyMessagesWithStatus()),
        MatrixRow("redbookLike", "com.huiyi.mockchat", "redbook_like", healthyMessages()),
        MatrixRow("datingLike", "com.huiyi.mockchat", "dating_like", healthyMessages()),
        MatrixRow("webviewLowAccessibility", "com.example.webview.chat", "WebView Chat", lowAccessibilityMessages(), parserConfidence = 48),
        MatrixRow("launcher", "com.xiaoenai.app", "华为桌面", healthyMessages(), currentAppPackage = "com.xiaoenai.app"),
        MatrixRow("huiyiOverlay", "com.bajiao.im.liaoqi", "会意雷达 这次不对，发给 GPT 隐藏", healthyMessages(), currentAppPackage = "com.huiyi.v4")
    )

    private fun healthyMessages() = listOf(
        textNode("me-1", Speaker.ME, "我明白你的意思。", 1),
        textNode("other-1", Speaker.OTHER, "这个事情还是要考虑现实和规划。", 2),
        textNode("me-2", Speaker.ME, "嗯，我也觉得不能只靠嘴上说。", 3),
        textNode("other-2", Speaker.OTHER, "稳定和以后也挺重要的。", 4)
    )

    private fun healthyMessagesWithStatus() = healthyMessages() + listOf(
        textNode("status-1", Speaker.SYSTEM, "已读", 5).copy(
            isEffectiveChatMessage = false,
            metadataType = MetadataType.READ_RECEIPT
        ),
        textNode("time-1", Speaker.SYSTEM, "10:56", 6).copy(
            isEffectiveChatMessage = false,
            metadataType = MetadataType.TIME
        )
    )

    private fun lowAccessibilityMessages() = listOf(
        textNode("unknown-1", Speaker.UNKNOWN, "真实私聊内容一", 1).copy(speakerConfidence = 30),
        textNode("unknown-2", Speaker.UNKNOWN, "真实私聊内容二", 2).copy(speakerConfidence = 30)
    )

    private data class MatrixRow(
        val name: String,
        val appPackage: String,
        val windowTitle: String,
        val messages: List<com.huiyi.v4.domain.model.MessageNode>,
        val currentAppPackage: String? = appPackage,
        val parserConfidence: Int = 88
    )
}
