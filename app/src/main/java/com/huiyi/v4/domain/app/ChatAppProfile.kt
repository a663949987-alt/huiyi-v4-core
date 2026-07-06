package com.huiyi.v4.domain.app

import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import java.io.File

enum class ChatAppSupportLevel {
    LEVEL_3_DEDICATED_PROFILE,
    LEVEL_2_GENERIC_TRIAL,
    LEVEL_1_UNSUPPORTED_WITH_ADAPTATION_PACK,
    LEVEL_0_BLOCK
}

enum class ChatAppParserConfidence {
    LOW,
    MEDIUM,
    HIGH
}

data class ChatAppProfile(
    val id: String,
    val displayName: String,
    val packageNames: Set<String>,
    val titleKeywords: Set<String> = emptySet(),
    val supportLevel: ChatAppSupportLevel,
    val parserName: String,
    val notes: String = ""
)

data class GenericChatTrialMetrics(
    val effectiveMessageCount: Int,
    val unknownRatio: Double,
    val speakerConfidenceAverage: Int,
    val metadataFilteredRatio: Double,
    val lastEffectiveMessageTextNonBlank: Boolean,
    val windowTitleStable: Boolean,
    val sameAppPackageStable: Boolean
)

data class GenericChatTrialResult(
    val passed: Boolean,
    val metrics: GenericChatTrialMetrics,
    val confidence: ChatAppParserConfidence,
    val failReason: String
)

data class ChatAppProfileDetectionInput(
    val appPackage: String?,
    val windowTitle: String?,
    val currentAppPackage: String? = appPackage,
    val currentWindowTitle: String? = windowTitle,
    val messages: List<MessageNode> = emptyList(),
    val parserConfidence: Int = 0,
    val windowTitleStable: Boolean? = null,
    val sameAppPackageStable: Boolean? = null
)

data class ChatAppProfileDetectionResult(
    val profile: ChatAppProfile,
    val supportLevel: ChatAppSupportLevel,
    val targetAppSupported: Boolean,
    val source: String,
    val parserConfidence: ChatAppParserConfidence,
    val genericTrial: GenericChatTrialResult? = null,
    val reason: String,
    val shouldGenerateAdaptationPack: Boolean
) {
    val blocked: Boolean get() = supportLevel == ChatAppSupportLevel.LEVEL_0_BLOCK
}

object ChatAppProfileRegistry {
    val liaoqiProfile = ChatAppProfile(
        id = "liaoqi",
        displayName = "Liaoqi",
        packageNames = setOf("com.bajiao.im.liaoqi"),
        supportLevel = ChatAppSupportLevel.LEVEL_3_DEDICATED_PROFILE,
        parserName = "LiaoqiRealParser"
    )

    val xiaoenaiGenericProfile = ChatAppProfile(
        id = "xiaoenai_generic",
        displayName = "Xiaoenai Generic Trial",
        packageNames = setOf("com.xiaoenai.app"),
        titleKeywords = setOf("\u5c0f\u6069\u7231"),
        supportLevel = ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL,
        parserName = "GenericVisualBubbleParser"
    )

    val genericChatProfile = ChatAppProfile(
        id = "generic_chat",
        displayName = "Generic Chat Trial",
        packageNames = emptySet(),
        supportLevel = ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL,
        parserName = "GenericVisualBubbleParser"
    )

    val unsupportedProfile = ChatAppProfile(
        id = "unsupported_adaptation_pack",
        displayName = "Unsupported App Adaptation Pack",
        packageNames = emptySet(),
        supportLevel = ChatAppSupportLevel.LEVEL_1_UNSUPPORTED_WITH_ADAPTATION_PACK,
        parserName = "NoSafeChatProfile"
    )

    val blockedProfile = ChatAppProfile(
        id = "blocked_environment",
        displayName = "Blocked Environment",
        packageNames = emptySet(),
        supportLevel = ChatAppSupportLevel.LEVEL_0_BLOCK,
        parserName = "BlockedEnvironment"
    )

    private val mockProfiles = listOf(
        "liaoqi_like",
        "xiaoenai_like",
        "wechat_like",
        "qq_like",
        "redbook_like",
        "dating_like",
        "minimal_like"
    ).map { id ->
        ChatAppProfile(
            id = id,
            displayName = id,
            packageNames = setOf("com.huiyi.mockchat"),
            titleKeywords = setOf(id),
            supportLevel = ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL,
            parserName = "GenericVisualBubbleParser",
            notes = "mock_profile_for_matrix"
        )
    }

    val profiles: List<ChatAppProfile> = listOf(liaoqiProfile, xiaoenaiGenericProfile) + mockProfiles

    fun dedicatedPackages(): Set<String> = profiles
        .filter { it.supportLevel == ChatAppSupportLevel.LEVEL_3_DEDICATED_PROFILE }
        .flatMap { it.packageNames }
        .toSet()

    fun knownProfileFor(appPackage: String?, windowTitle: String?): ChatAppProfile? {
        val pkg = appPackage.orEmpty()
        val title = windowTitle.orEmpty()
        return profiles.firstOrNull { profile ->
            pkg in profile.packageNames &&
                (profile.titleKeywords.isEmpty() || profile.titleKeywords.any { title.contains(it, ignoreCase = true) })
        }
    }
}

object GenericChatTrial {
    const val MIN_EFFECTIVE_MESSAGES = 3
    const val MAX_UNKNOWN_RATIO = 0.30
    const val MIN_SPEAKER_CONFIDENCE_AVERAGE = 70
    const val MAX_METADATA_FILTERED_RATIO = 0.75

    fun evaluate(input: ChatAppProfileDetectionInput): GenericChatTrialResult {
        val currentPackage = input.currentAppPackage ?: input.appPackage
        val currentTitle = input.currentWindowTitle ?: input.windowTitle
        val candidateMessages = input.messages.filter {
            it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM
        }
        val effectiveMessages = candidateMessages.filter { it.speaker == Speaker.ME || it.speaker == Speaker.OTHER }
        val unknownCount = candidateMessages.count { it.speaker == Speaker.UNKNOWN }
        val metadataCount = input.messages.count {
            !it.isEffectiveChatMessage || it.speaker == Speaker.SYSTEM || it.metadataType != MetadataType.NONE
        }
        val messageSpeakerConfidence = effectiveMessages
            .map { it.speakerConfidence.coerceIn(0, 100) }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toInt()
            ?: 0
        val avgSpeakerConfidence = if (input.parserConfidence in 1..99) {
            minOf(messageSpeakerConfidence, input.parserConfidence.coerceIn(0, 100))
        } else {
            messageSpeakerConfidence
        }
        val lastTextNonBlank = effectiveMessages.lastOrNull()?.textForTrial()?.isNotBlank() == true
        val sameAppStable = input.sameAppPackageStable ?: run {
            val app = input.appPackage.orEmpty()
            val current = currentPackage.orEmpty()
            app.isNotBlank() && (current.isBlank() || app == current)
        }
        val windowStable = input.windowTitleStable ?: currentTitle.orEmpty().isNotBlank()
        val metrics = GenericChatTrialMetrics(
            effectiveMessageCount = effectiveMessages.size,
            unknownRatio = if (candidateMessages.isEmpty()) 0.0 else unknownCount.toDouble() / candidateMessages.size,
            speakerConfidenceAverage = avgSpeakerConfidence,
            metadataFilteredRatio = if (input.messages.isEmpty()) 0.0 else metadataCount.toDouble() / input.messages.size,
            lastEffectiveMessageTextNonBlank = lastTextNonBlank,
            windowTitleStable = windowStable,
            sameAppPackageStable = sameAppStable
        )
        val failReason = when {
            ChatAppProfileDetector.isBlockedEnvironment(currentTitle, currentPackage) -> "BLOCKED_ENVIRONMENT"
            !metrics.windowTitleStable -> "WINDOW_TITLE_UNSTABLE"
            !metrics.sameAppPackageStable -> "APP_PACKAGE_UNSTABLE"
            metrics.effectiveMessageCount < MIN_EFFECTIVE_MESSAGES -> "INSUFFICIENT_EFFECTIVE_MESSAGES"
            metrics.unknownRatio > MAX_UNKNOWN_RATIO -> "UNKNOWN_RATIO_TOO_HIGH"
            metrics.speakerConfidenceAverage < MIN_SPEAKER_CONFIDENCE_AVERAGE -> "LOW_GENERIC_CONFIDENCE"
            !metrics.lastEffectiveMessageTextNonBlank -> "LAST_EFFECTIVE_MESSAGE_EMPTY"
            metrics.metadataFilteredRatio > MAX_METADATA_FILTERED_RATIO -> "METADATA_FILTERED_RATIO_TOO_HIGH"
            else -> "PASS"
        }
        return GenericChatTrialResult(
            passed = failReason == "PASS",
            metrics = metrics,
            confidence = when {
                avgSpeakerConfidence >= 85 && metrics.unknownRatio <= 0.15 -> ChatAppParserConfidence.HIGH
                avgSpeakerConfidence >= MIN_SPEAKER_CONFIDENCE_AVERAGE -> ChatAppParserConfidence.MEDIUM
                else -> ChatAppParserConfidence.LOW
            },
            failReason = failReason
        )
    }
}

private fun MessageNode.textForTrial(): String? {
    return normalizedText ?: when (val value = content) {
        is MessageContent.Text -> value.text
        is MessageContent.Voice -> value.transcriptText ?: value.userSummary
        is MessageContent.Image -> value.descriptionText
        is MessageContent.Sticker -> value.meaningText
        is MessageContent.Video -> value.descriptionText
    }
}

object ChatAppProfileDetector {
    fun detect(input: ChatAppProfileDetectionInput): ChatAppProfileDetectionResult {
        val appPackage = input.appPackage.orEmpty()
        val windowTitle = input.currentWindowTitle ?: input.windowTitle
        val currentPackage = input.currentAppPackage ?: input.appPackage
        if (isBlockedEnvironment(windowTitle, currentPackage)) {
            return ChatAppProfileDetectionResult(
                profile = ChatAppProfileRegistry.blockedProfile,
                supportLevel = ChatAppSupportLevel.LEVEL_0_BLOCK,
                targetAppSupported = false,
                source = "BLOCK",
                parserConfidence = ChatAppParserConfidence.LOW,
                reason = "WINDOW_IS_DESKTOP_OR_LAUNCHER",
                shouldGenerateAdaptationPack = false
            )
        }
        val known = ChatAppProfileRegistry.knownProfileFor(appPackage, windowTitle)
        if (known?.supportLevel == ChatAppSupportLevel.LEVEL_3_DEDICATED_PROFILE) {
            return ChatAppProfileDetectionResult(
                profile = known,
                supportLevel = known.supportLevel,
                targetAppSupported = true,
                source = known.id,
                parserConfidence = ChatAppParserConfidence.HIGH,
                reason = "DEDICATED_PROFILE",
                shouldGenerateAdaptationPack = false
            )
        }
        val trial = GenericChatTrial.evaluate(input)
        if (trial.passed) {
            val profile = known ?: ChatAppProfileRegistry.genericChatProfile
            return ChatAppProfileDetectionResult(
                profile = profile,
                supportLevel = ChatAppSupportLevel.LEVEL_2_GENERIC_TRIAL,
                targetAppSupported = true,
                source = "GENERIC_TRIAL",
                parserConfidence = trial.confidence,
                genericTrial = trial,
                reason = "GENERIC_TRIAL_PASS",
                shouldGenerateAdaptationPack = false
            )
        }
        return ChatAppProfileDetectionResult(
            profile = known ?: ChatAppProfileRegistry.unsupportedProfile,
            supportLevel = ChatAppSupportLevel.LEVEL_1_UNSUPPORTED_WITH_ADAPTATION_PACK,
            targetAppSupported = false,
            source = "UNSUPPORTED_WITH_ADAPTATION_PACK",
            parserConfidence = trial.confidence,
            genericTrial = trial,
            reason = trial.failReason,
            shouldGenerateAdaptationPack = true
        )
    }

    fun isBlockedEnvironment(title: String?, packageName: String?): Boolean {
        val titleText = title.orEmpty()
        val packageText = packageName.orEmpty()
        if (packageText == "com.huiyi.v4" || packageText == "com.android.systemui") return true
        if (packageText.contains("launcher", ignoreCase = true)) return true
        if (packageText.equals("com.huawei.android.launcher", ignoreCase = true)) return true
        val desktopMarkers = listOf(
            "\u534e\u4e3a\u684c\u9762",
            "\u684c\u9762",
            "Launcher",
            "launcher"
        )
        if (desktopMarkers.any { titleText.contains(it, ignoreCase = true) }) return true
        val ownPanelMarkers = listOf(
            "\u4f1a\u610f\u96f7\u8fbe",
            "\u8fd9\u6b21\u4e0d\u5bf9",
            "\u6ca1\u8bfb\u5230\u5f53\u524d\u804a\u5929",
            "\u6ca1\u8bfb\u5230\u804a\u5929",
            "\u8bf7\u56de\u5230",
            "\u9690\u85cf"
        )
        return ownPanelMarkers.any { titleText.contains(it, ignoreCase = true) }
    }

}

data class UnsupportedAppAdaptationPack(
    val appPackage: String,
    val windowTitleRedacted: String,
    val rootNodeSummary: List<String>,
    val visibleTextRedacted: List<String>,
    val candidateMessageRows: List<String>,
    val candidateLeftRightBounds: List<String>,
    val metadataCandidates: List<String>,
    val inputBoxCandidates: List<String>,
    val sendButtonCandidates: List<String>,
    val reasonWhyUnsupported: String,
    val screenshotIncluded: Boolean = false,
    val rawPrivateChatIncluded: Boolean = false
)

class UnsupportedAppAdaptationExporter {
    fun build(
        appPackage: String?,
        windowTitle: String?,
        messages: List<MessageNode>,
        reasonWhyUnsupported: String
    ): UnsupportedAppAdaptationPack {
        return UnsupportedAppAdaptationPack(
            appPackage = appPackage.orEmpty().ifBlank { "unknown" },
            windowTitleRedacted = windowTitle.orEmpty().ifBlank { "unknown" }.take(80),
            rootNodeSummary = listOf(
                "messageCount=${messages.size}",
                "effectiveCount=${messages.count { it.isEffectiveChatMessage }}",
                "unknownCount=${messages.count { it.speaker == Speaker.UNKNOWN }}"
            ),
            visibleTextRedacted = messages.take(12).map { it.textForRedaction().redactShape() },
            candidateMessageRows = messages
                .filter { it.isEffectiveChatMessage }
                .take(12)
                .map { "${it.id}:${it.speaker}:${it.textForRedaction().redactShape()}" },
            candidateLeftRightBounds = messages
                .mapNotNull { message -> message.bounds?.let { "${message.id}:${message.speaker}:${it.boundsString()}" } }
                .take(16),
            metadataCandidates = messages
                .filter { it.metadataType != MetadataType.NONE || !it.isEffectiveChatMessage }
                .take(12)
                .map { "${it.id}:${it.metadataType}:${it.textForRedaction().redactShape()}" },
            inputBoxCandidates = emptyList(),
            sendButtonCandidates = emptyList(),
            reasonWhyUnsupported = reasonWhyUnsupported,
            screenshotIncluded = false,
            rawPrivateChatIncluded = false
        )
    }

    fun export(rootDir: File, pack: UnsupportedAppAdaptationPack, timestamp: String = System.currentTimeMillis().toString()): File {
        val safePackage = pack.appPackage.replace(Regex("[^A-Za-z0-9_.-]"), "_")
        val dir = File(rootDir, "$safePackage-$timestamp")
        dir.mkdirs()
        File(dir, "adaptation-pack.json").writeText(pack.toJson(), Charsets.UTF_8)
        return dir
    }

    private fun UnsupportedAppAdaptationPack.toJson(): String = """
        {
          "appPackage": "${appPackage.json()}",
          "windowTitleRedacted": "${windowTitleRedacted.json()}",
          "rootNodeSummary": ${rootNodeSummary.jsonArray()},
          "visibleTextRedacted": ${visibleTextRedacted.jsonArray()},
          "candidateMessageRows": ${candidateMessageRows.jsonArray()},
          "candidateLeftRightBounds": ${candidateLeftRightBounds.jsonArray()},
          "metadataCandidates": ${metadataCandidates.jsonArray()},
          "inputBoxCandidates": ${inputBoxCandidates.jsonArray()},
          "sendButtonCandidates": ${sendButtonCandidates.jsonArray()},
          "reasonWhyUnsupported": "${reasonWhyUnsupported.json()}",
          "screenshotIncluded": $screenshotIncluded,
          "rawPrivateChatIncluded": $rawPrivateChatIncluded
        }
    """.trimIndent()

    private fun List<String>.jsonArray(): String = joinToString(prefix = "[", postfix = "]") { "\"${it.json()}\"" }
    private fun String.json(): String = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    private fun String.redactShape(): String = if (isBlank()) "" else "[redacted:${length}chars]"
    private fun VisualBounds.boundsString(): String = "[$left,$top,$right,$bottom]"
    private fun MessageNode.textForRedaction(): String {
        return normalizedText ?: when (val value = content) {
            is MessageContent.Text -> value.text
            is MessageContent.Voice -> value.transcriptText ?: value.userSummary ?: "[voice]"
            is MessageContent.Image -> value.descriptionText ?: "[image]"
            is MessageContent.Sticker -> value.meaningText ?: "[sticker]"
            is MessageContent.Video -> value.descriptionText ?: "[video]"
        }
    }
}
