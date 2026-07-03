package com.huiyi.v4.domain.simulation

import com.huiyi.v4.accessibility.CurrentScreenSnapshot
import com.huiyi.v4.accessibility.ScreenNodeSnapshot
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.LiaoqiRealParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureResult
import com.huiyi.v4.domain.pipeline.CurrentScreenCaptureUseCase
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineUseCase
import com.huiyi.v4.domain.pipeline.SampleSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class AccessibilityNodeFixture(
    val category: FixtureCategory,
    val name: String,
    val appPackage: String,
    val windowTitle: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val nodes: List<FixtureNode>,
    val expectedLastSpeaker: Speaker?,
    val expectedDecisionType: TacticalDecisionType?,
    val expectedRouteCount: Int?,
    val expectedPanelState: PanelState,
    val notes: String = ""
)

data class FixtureNode(
    val id: String,
    val text: String?,
    val contentDescription: String? = null,
    val className: String? = "android.widget.TextView",
    val bounds: VisualBounds,
    val visibleToUser: Boolean = true,
    val depth: Int = 2,
    val childCount: Int = 0,
    val parentBounds: VisualBounds? = null,
    val ancestorBoundsChain: List<VisualBounds> = emptyList()
)

enum class FixtureCategory(val id: String) {
    LIAOQI_LAST_OTHER_PASS("liaoqi_last_other_pass"),
    LIAOQI_LAST_ME_WAIT("liaoqi_last_me_wait"),
    LIAOQI_POST_PANEL_CONTAMINATED("liaoqi_post_panel_contaminated"),
    LIAOQI_READ_RECEIPT_STATUS("liaoqi_read_receipt_status"),
    GENERIC_TIME_METADATA_TRAP("generic_time_metadata_trap"),
    UNSUPPORTED_APP_NO_CHAT_ROWS("unsupported_app_no_chat_rows")
}

enum class PanelState {
    WAIT_PANEL,
    ROUTE_PANEL,
    CONTEXT_REQUIRED_PANEL,
    VOICE_SUMMARY_CARD,
    UNSUPPORTED_APP_PANEL
}

data class NormalizedConversation(
    val fixtureName: String,
    val appPackage: String,
    val sampleSource: SampleSource,
    val parserName: String,
    val parserFallbackUsed: Boolean,
    val messages: List<MessageNode>,
    val effectiveMessages: List<MessageNode>,
    val lastSpeaker: Speaker?,
    val decisionType: TacticalDecisionType,
    val routeCount: Int,
    val panelState: PanelState,
    val apiCalled: Boolean
)

data class FixtureReplayAssertion(
    val fixtureName: String,
    val category: FixtureCategory,
    val result: String,
    val failReason: String,
    val normalized: NormalizedConversation
) {
    val passed: Boolean get() = result == "PASS"
}

object AccessibilityNodeFixtureFactory {
    private val json = Json { ignoreUnknownKeys = true }

    fun fromRealDeviceReportJson(
        name: String,
        reportJson: String,
        category: FixtureCategory,
        expectedLastSpeaker: Speaker?,
        expectedDecisionType: TacticalDecisionType?,
        expectedRouteCount: Int?,
        expectedPanelState: PanelState
    ): AccessibilityNodeFixture {
        val root = json.parseToJsonElement(reportJson).jsonObject
        val appPackage = root.string("appPackage").ifBlank { "unknown" }
        val screenWidth = root.int("screenWidth") ?: 1080
        val screenHeight = root.int("screenHeight") ?: 2400
        val rawNodes = root.arrayOrNull("nodes")
            ?: root.arrayOrNull("rawNodes")
            ?: root.arrayOrNull("nodeDump")
        val parsedMessages = root.arrayOrNull("parsedMessages")
        val nodes = when {
            rawNodes != null && rawNodes.isNotEmpty() -> rawNodes.mapIndexed { index, element ->
                val obj = element.jsonObject
                FixtureNode(
                    id = obj.string("id").ifBlank { "node-$index" },
                    text = obj.string("text").ifBlank { obj.string("readableText").ifBlank { null } },
                    contentDescription = obj.string("contentDescription").ifBlank { null },
                    className = obj.string("className").ifBlank { "android.widget.TextView" },
                    bounds = obj.bounds("bounds") ?: VisualBounds(0, 0, 1, 1),
                    visibleToUser = obj.string("visibleToUser").ifBlank { "true" }.toBoolean(),
                    depth = obj.int("depth") ?: 2,
                    childCount = obj.int("childCount") ?: 0,
                    parentBounds = obj.bounds("parentBounds"),
                    ancestorBoundsChain = obj.boundsArray("ancestorBoundsChain")
                )
            }
            parsedMessages != null -> parsedMessages.mapIndexed { index, element ->
                val obj = element.jsonObject
                FixtureNode(
                    id = obj.string("id").ifBlank { "parsed-$index" },
                    text = textForParsedMessage(obj),
                    className = "android.widget.TextView",
                    bounds = obj.bounds("textBounds") ?: obj.bounds("bounds") ?: VisualBounds(0, 0, 1, 1),
                    visibleToUser = true,
                    depth = 2,
                    childCount = 0,
                    parentBounds = obj.bounds("parentBounds") ?: obj.bounds("rowBounds"),
                    ancestorBoundsChain = obj.boundsArray("ancestorBoundsChain")
                )
            }
            else -> emptyList()
        }
        return AccessibilityNodeFixture(
            category = category,
            name = name,
            appPackage = appPackage,
            windowTitle = root.string("windowTitle").ifBlank { "fixture" },
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            nodes = nodes,
            expectedLastSpeaker = expectedLastSpeaker,
            expectedDecisionType = expectedDecisionType,
            expectedRouteCount = expectedRouteCount,
            expectedPanelState = expectedPanelState,
            notes = "generated_from_real_device_report_json"
        )
    }

    private fun textForParsedMessage(obj: JsonObject): String {
        val text = obj.string("text")
        if (text.isNotBlank() && text != "[REDACTED_PRIVATE_CHAT]") return text
        return when (obj.string("contentType").lowercase()) {
            "voice" -> "voice 18s"
            "image" -> "[image]"
            "sticker" -> "[sticker]"
            else -> obj.string("metadataType").takeIf { it != "NONE" } ?: "fixture message"
        }
    }

    private fun JsonObject.arrayOrNull(name: String): JsonArray? = this[name]?.jsonArray
    private fun JsonObject.string(name: String): String = this[name]?.jsonPrimitive?.contentOrNull.orEmpty()
    private fun JsonObject.int(name: String): Int? = this[name]?.jsonPrimitive?.intOrNull
    private fun JsonObject.bounds(name: String): VisualBounds? = this[name]?.jsonObject?.let {
        VisualBounds(it.int("left") ?: 0, it.int("top") ?: 0, it.int("right") ?: 0, it.int("bottom") ?: 0)
    }?.takeIf { it.right > it.left && it.bottom > it.top }

    private fun JsonObject.boundsArray(name: String): List<VisualBounds> {
        return this[name]?.jsonArray?.mapNotNull { element ->
            val obj = element.jsonObject
            VisualBounds(obj.int("left") ?: 0, obj.int("top") ?: 0, obj.int("right") ?: 0, obj.int("bottom") ?: 0)
                .takeIf { it.right > it.left && it.bottom > it.top }
        }.orEmpty()
    }
}

class AccessibilityNodeFixtureReplayer(
    private val persona: UserPersonaCorpus = DefaultPersonaCorpus.soldier()
) {
    suspend fun replay(fixture: AccessibilityNodeFixture): NormalizedConversation {
        if (fixture.nodes.isEmpty() && fixture.appPackage !in supportedPackages) {
            return NormalizedConversation(
                fixtureName = fixture.name,
                appPackage = fixture.appPackage,
                sampleSource = SampleSource.REAL_DEVICE_ACCESSIBILITY,
                parserName = "NoChatRows",
                parserFallbackUsed = false,
                messages = emptyList(),
                effectiveMessages = emptyList(),
                lastSpeaker = null,
                decisionType = TacticalDecisionType.CONTEXT_REQUIRED,
                routeCount = 0,
                panelState = PanelState.UNSUPPORTED_APP_PANEL,
                apiCalled = false
            )
        }
        val capture = captureResult(fixture)
        val result = CurrentScreenPipelineUseCase(
            captureUseCase = object : CurrentScreenCaptureUseCase(serviceProvider = { null }) {
                override fun capture(): Result<CurrentScreenCaptureResult> = Result.success(capture)
            }
        ).run(persona).getOrThrow()
        val effective = capture.messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        return NormalizedConversation(
            fixtureName = fixture.name,
            appPackage = fixture.appPackage,
            sampleSource = capture.sampleSource,
            parserName = capture.parserName,
            parserFallbackUsed = capture.parserFallbackUsed,
            messages = capture.messages,
            effectiveMessages = effective,
            lastSpeaker = result.lastSpeakerDecision.lastSpeaker,
            decisionType = result.tacticalDecision.decisionType,
            routeCount = result.routes.size,
            panelState = panelStateFor(fixture.appPackage, result.tacticalDecision.decisionType, result.routes.size),
            apiCalled = result.apiCalled
        )
    }

    suspend fun assertFixture(fixture: AccessibilityNodeFixture): FixtureReplayAssertion {
        val normalized = replay(fixture)
        val failReason = when {
            fixture.expectedLastSpeaker != null && normalized.lastSpeaker != fixture.expectedLastSpeaker ->
                "lastSpeaker expected ${fixture.expectedLastSpeaker} actual ${normalized.lastSpeaker}"
            fixture.expectedDecisionType != null && normalized.decisionType != fixture.expectedDecisionType ->
                "decisionType expected ${fixture.expectedDecisionType} actual ${normalized.decisionType}"
            fixture.expectedRouteCount != null && normalized.routeCount != fixture.expectedRouteCount ->
                "routeCount expected ${fixture.expectedRouteCount} actual ${normalized.routeCount}"
            normalized.panelState != fixture.expectedPanelState ->
                "panelState expected ${fixture.expectedPanelState} actual ${normalized.panelState}"
            else -> "none"
        }
        return FixtureReplayAssertion(
            fixtureName = fixture.name,
            category = fixture.category,
            result = if (failReason == "none") "PASS" else "FAIL",
            failReason = failReason,
            normalized = normalized
        )
    }

    private fun captureResult(fixture: AccessibilityNodeFixture): CurrentScreenCaptureResult {
        val snapshot = fixture.toSnapshot()
        val bubbles = snapshot.nodes.toVisualBubbles()
        val parsed = parseForApp(fixture.appPackage, fixture.screenWidth, bubbles)
        val messages = parsed.messages.filter {
            it.normalizedText?.isNotBlank() == true ||
                it.content is MessageContent.Voice ||
                it.content is MessageContent.Image ||
                it.content is MessageContent.Sticker
        }
        val source = if (fixture.appPackage == "com.huiyi.mockchat") {
            SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY
        } else {
            SampleSource.REAL_DEVICE_ACCESSIBILITY
        }
        return CurrentScreenCaptureResult(
            snapshot = snapshot,
            messages = messages,
            sampleSource = source,
            parserName = parsed.parserName,
            parserFallbackUsed = parsed.fallbackUsed
        )
    }

    private fun AccessibilityNodeFixture.toSnapshot(): CurrentScreenSnapshot {
        return CurrentScreenSnapshot(
            appPackage = appPackage,
            windowTitle = windowTitle,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            nodes = nodes.map { node ->
                ScreenNodeSnapshot(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    viewIdResourceName = null,
                    bounds = node.bounds,
                    visibleToUser = node.visibleToUser,
                    depth = node.depth,
                    childCount = node.childCount,
                    parentBounds = node.parentBounds,
                    ancestorBoundsChain = node.ancestorBoundsChain
                )
            },
            capturedAt = System.currentTimeMillis()
        )
    }

    private fun List<ScreenNodeSnapshot>.toVisualBubbles(): List<VisualBubble> {
        return asSequence()
            .filter { it.visibleToUser }
            .filter { it.bounds.right > it.bounds.left && it.bounds.bottom > it.bounds.top }
            .filter { it.readableText?.isNotBlank() == true }
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

    private fun parseForApp(appPackage: String, screenWidth: Int, bubbles: List<VisualBubble>): ParserSelection {
        if (appPackage == "com.bajiao.im.liaoqi") {
            val liaoqiMessages = LiaoqiRealParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            if (liaoqiMessages.any { it.isEffectiveChatMessage }) {
                return ParserSelection(liaoqiMessages, "LiaoqiRealParser", false)
            }
            val genericMessages = GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            return ParserSelection(genericMessages, "GenericVisualBubbleParser", true)
        }
        return ParserSelection(
            GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN),
            "GenericVisualBubbleParser",
            false
        )
    }

    private fun panelStateFor(appPackage: String, decision: TacticalDecisionType, routeCount: Int): PanelState {
        if (appPackage !in supportedPackages && routeCount == 0) return PanelState.UNSUPPORTED_APP_PANEL
        return when {
            decision == TacticalDecisionType.WAIT -> PanelState.WAIT_PANEL
            decision == TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> PanelState.VOICE_SUMMARY_CARD
            routeCount == 5 -> PanelState.ROUTE_PANEL
            else -> PanelState.CONTEXT_REQUIRED_PANEL
        }
    }

    private data class ParserSelection(
        val messages: List<MessageNode>,
        val parserName: String,
        val fallbackUsed: Boolean
    )

    private companion object {
        val supportedPackages = setOf("com.bajiao.im.liaoqi", "com.huiyi.mockchat")
    }
}
