package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.TranscriptStatus
import java.io.File

data class EvidencePackFiles(
    val markdown: File,
    val json: File
)

class EvidencePackReportGenerator {
    fun overallResult(result: CurrentScreenPipelineResult): String {
        val capture = result.captureResult ?: return "FAIL"
        val appPackage = capture.snapshot.appPackage.orEmpty()
        val unknownRatio = capture.messages.count { it.speaker == Speaker.UNKNOWN }.toFloat() / capture.messages.size.coerceAtLeast(1)
        val unknownChatNodeWithRoutes = capture.messages.any {
            it.speaker == Speaker.UNKNOWN &&
                it.metadataType == com.huiyi.v4.domain.model.MetadataType.NONE
        } && result.routes.isNotEmpty()
        val voiceMistake = capture.messages.any {
            it.content is MessageContent.Voice &&
                it.content.transcriptStatus == TranscriptStatus.MISSING &&
                it.normalizedText?.isNotBlank() == true
        }
        return if (
            capture.sampleSource !in setOf(SampleSource.REAL_DEVICE_ACCESSIBILITY, SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY) ||
            appPackage == "local.validation.sample" ||
            appPackage.startsWith("local.validation") ||
            result.apiCalled ||
            (result.lastSpeakerDecision.lastSpeaker == Speaker.ME && result.routes.isNotEmpty()) ||
            (result.lastSpeakerDecision.lastSpeaker == Speaker.OTHER && result.routes.size != 5 &&
                result.tacticalDecision.decisionType !in setOf(TacticalDecisionType.VOICE_SUMMARY_REQUIRED, TacticalDecisionType.CONTEXT_REQUIRED)) ||
            voiceMistake ||
            (unknownRatio > 0.30f && result.routes.isNotEmpty()) ||
            unknownChatNodeWithRoutes
        ) {
            "FAIL"
        } else {
            "PASS"
        }
    }

    fun buildMarkdown(
        result: CurrentScreenPipelineResult,
        accessibilityState: HuiyiAccessibilityState,
        generatedAt: Long = System.currentTimeMillis()
    ): String {
        val capture = result.captureResult
        val context = result.context
        val messages = capture?.messages.orEmpty()
        val effectiveMessages = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val metadataMessages = messages.filter { !it.isEffectiveChatMessage || it.metadataType != com.huiyi.v4.domain.model.MetadataType.NONE }
        val decision = result.tacticalDecision
        val voiceMessages = messages.filter { it.content is MessageContent.Voice }
        val unknownRatio = messages.count { it.speaker == Speaker.UNKNOWN }.toFloat() / messages.size.coerceAtLeast(1)
        val speakerReasons = messages.groupingBy { it.speakerReason ?: "unknown_visual_bounds" }.eachCount()
        val waitShown = decision.decisionType == TacticalDecisionType.WAIT
        val voiceShown = decision.decisionType == TacticalDecisionType.VOICE_SUMMARY_REQUIRED
        val contextShown = decision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED
        return buildString {
            appendLine("# Real Device Current Screen Evidence Pack")
            appendLine()
            appendLine("- overall_result: ${overallResult(result)}")
            appendLine("- generatedAt: $generatedAt")
            appendLine("- sample_source: ${capture?.sampleSource?.reportValue ?: SampleSource.UNKNOWN.reportValue}")
            appendLine("- appPackage: ${capture?.snapshot?.appPackage ?: "unknown"}")
            appendLine("- windowTitle: ${capture?.snapshot?.windowTitle ?: "unknown"}")
            appendLine("- screenWidth: ${capture?.snapshot?.screenWidth ?: 0}")
            appendLine("- screenHeight: ${capture?.snapshot?.screenHeight ?: 0}")
            appendLine("- serviceConnected: ${accessibilityState.serviceConnected}")
            appendLine("- rootAvailable: ${accessibilityState.rootAvailable}")
            appendLine("- capturedNodeCount: ${capture?.snapshot?.nodes?.size ?: 0}")
            appendLine("- parserName: GenericVisualBubbleParser")
            appendLine("- parserFallbackUsed: ${capture?.warning != null}")
            appendLine("- currentBubbleSideRule: right=me")
            appendLine("- modelCalled: false")
            appendLine("- apiCalled: ${result.apiCalled}")
            appendLine("- overlayShownInTargetApp: ${result.overlayShownInTargetApp}")
            appendLine("- foregroundPackageWhenPanelShown: ${result.foregroundPackageWhenPanelShown ?: "unknown"}")
            appendLine("- huiyiActivityOpened: ${result.huiyiActivityOpened}")
            appendLine("- userStayedInChatApp: ${result.userStayedInChatApp}")
            appendLine("- resultShownAsOverlay: ${result.resultShownAsOverlay}")
            appendLine("- mainActivityOpened: ${result.mainActivityOpened}")
            appendLine()
            appendLine("## 解析结果")
            appendLine("- rawParsedNodeCount: ${messages.size}")
            appendLine("- metadataFilteredCount: ${metadataMessages.size}")
            appendLine("- effectiveMessageCount: ${effectiveMessages.size}")
            appendLine("- effectiveMeCount: ${effectiveMessages.count { it.speaker == Speaker.ME }}")
            appendLine("- effectiveOtherCount: ${effectiveMessages.count { it.speaker == Speaker.OTHER }}")
            appendLine("- parsedMessageCount: ${messages.size}")
            appendLine("- meCount: ${messages.count { it.speaker == Speaker.ME }}")
            appendLine("- otherCount: ${messages.count { it.speaker == Speaker.OTHER }}")
            appendLine("- unknownCount: ${messages.count { it.speaker == Speaker.UNKNOWN }}")
            appendLine("- unknownRatio: ${"%.2f".format(unknownRatio)}")
            appendLine("- systemCount: ${messages.count { it.speaker == Speaker.SYSTEM }}")
            appendLine("- voiceCount: ${voiceMessages.size}")
            appendLine("- imageCount: ${messages.count { it.content is MessageContent.Image }}")
            appendLine()
            appendLine("### filteredMetadataSamples")
            if (metadataMessages.isEmpty()) {
                appendLine("- none")
            } else {
                metadataMessages.take(20).forEach { node ->
                    appendLine("- [${node.metadataType ?: "UNKNOWN_METADATA"}] ${node.normalizedText ?: "[non-text]"}")
                }
            }
            appendLine()
            appendLine("### speakerReason 分布")
            speakerReasons.forEach { (reason, count) -> appendLine("- $reason: $count") }
            appendLine()
            appendLine("## 最近 30 条解析消息")
            messages.takeLast(30).forEachIndexed { index, message ->
                appendLine(formatMessage(index + 1, message))
            }
            appendLine()
            appendLine("## LastSpeakerDecision")
            appendLine("- lastRawNodeId: ${messages.lastOrNull()?.id ?: "none"}")
            appendLine("- lastEffectiveMessageId: ${result.lastSpeakerDecision.lastEffectiveMessage?.id ?: "none"}")
            appendLine("- lastEffectiveMessageText: ${result.lastSpeakerDecision.lastEffectiveMessage?.normalizedText ?: "none"}")
            appendLine("- lastEffectiveSpeaker: ${result.lastSpeakerDecision.lastSpeaker ?: "none"}")
            appendLine("- lastSpeaker: ${result.lastSpeakerDecision.lastSpeaker ?: "none"}")
            appendLine("- shouldReply: ${result.lastSpeakerDecision.shouldReply}")
            appendLine("- decisionType: ${decision.decisionType}")
            appendLine("- reason: ${result.lastSpeakerDecision.reason}")
            appendLine()
            appendLine("## ContextAssembler")
            appendLine("- contextCompleteness.score: ${context?.contentCompleteness?.score ?: 0}")
            appendLine("- canDeepAnalyze: ${context?.contentCompleteness?.canDeepAnalyze ?: false}")
            appendLine("- missingTypes: ${context?.contentCompleteness?.missingTypes?.joinToString() ?: "none"}")
            appendLine("- coCreationOpportunity.exists: ${context?.coCreationOpportunity?.exists ?: false}")
            appendLine("- coCreationOpportunity.type: ${context?.coCreationOpportunity?.type ?: "none"}")
            appendLine("- unfinishedMeaning: ${context?.coCreationOpportunity?.unfinishedMeaning ?: "none"}")
            appendLine("- currentSceneSummary: ${context?.turns?.joinToString(" | ") { it.summary } ?: "none"}")
            appendLine()
            appendLine("## TacticalDecision")
            appendLine("- decisionType: ${decision.decisionType}")
            appendLine("- situation: ${decision.situation}")
            appendLine("- coreInsight: ${decision.coreInsight}")
            appendLine("- userLikelyMistake: ${decision.userLikelyMistake ?: "none"}")
            appendLine("- bestMove: ${decision.bestMove}")
            appendLine("- avoidMoves: ${decision.avoidMoves.joinToString(" / ")}")
            appendLine("- influenceIntensity: ${decision.influenceProfile.intensity}")
            appendLine("- riskLevel: ${decision.influenceProfile.riskLevel}")
            appendLine("- riskWarning: ${decision.influenceProfile.riskWarning ?: "none"}")
            appendLine("- fallbackMove: ${decision.fallbackMove ?: "none"}")
            appendLine()
            appendLine("## ReplyRoutes")
            if (result.routes.isEmpty()) {
                appendLine("- routes: empty")
                appendLine("- reason: WAIT or blocked by missing voice/context/unknown speaker.")
            } else {
                result.routes.forEach { route ->
                    appendLine("- route id: ${route.id}")
                    appendLine("  name: ${route.name}")
                    appendLine("  routeType: ${route.routeType}")
                    appendLine("  message: ${route.message}")
                    appendLine("  intensity: ${route.intensity}")
                    appendLine("  riskLevel: ${route.riskLevel}")
                    appendLine("  riskWarning: ${route.riskWarning ?: "none"}")
                    appendLine("  fallbackMove: ${route.fallbackMove ?: "none"}")
                }
            }
            appendLine()
            appendLine("## VoiceSummary")
            if (voiceMessages.isEmpty()) {
                appendLine("- voiceMessages: none")
            } else {
                voiceMessages.forEach { node ->
                    val voice = node.content as MessageContent.Voice
                    appendLine("- voiceMessageId: ${node.id}")
                    appendLine("  speaker: ${node.speaker}")
                    appendLine("  duration: ${voice.durationSeconds ?: "unknown"}")
                    appendLine("  transcriptStatus: ${voice.transcriptStatus}")
                    appendLine("  whether VoiceSummaryCard shown: $voiceShown")
                    appendLine("  userSummary: ${voice.userSummary ?: "none"}")
                }
            }
            appendLine()
            appendLine("## Persistence")
            appendLine("- message_nodes written count: ${context?.allMessages?.size ?: 0}")
            appendLine("- chat_scenes written count: ${if (context != null) 1 else 0}")
            appendLine("- reply_attempt created count after copy: not measured in this capture")
            appendLine("- last error: ${result.persistenceError ?: "none"}")
            appendLine()
            appendLine("## UI State")
            appendLine("- FloatingTacticalPanel shown: true")
            appendLine("- WAIT panel shown: $waitShown")
            appendLine("- VoiceSummaryCard shown: $voiceShown")
            appendLine("- ContextRequiredCard shown: $contextShown")
        }
    }

    fun buildJson(
        result: CurrentScreenPipelineResult,
        accessibilityState: HuiyiAccessibilityState,
        generatedAt: Long = System.currentTimeMillis()
    ): String {
        val capture = result.captureResult
        val messagesJson = capture?.messages.orEmpty().mapIndexed { index, message ->
            """
              {
                "index": ${index + 1},
                "id": "${escape(message.id)}",
                "speaker": "${message.speaker}",
                "speakerConfidence": ${message.speakerConfidence},
                "speakerReason": "${escape(message.speakerReason ?: "unknown_visual_bounds")}",
                "contentType": "${contentType(message.content)}",
                "text": "${escape(message.normalizedText ?: "")}",
                "isEffectiveChatMessage": ${message.isEffectiveChatMessage},
                "metadataType": "${message.metadataType ?: com.huiyi.v4.domain.model.MetadataType.NONE}",
                "inferredSide": "${escape(message.inferredSide ?: "")}",
                "bounds": ${boundsJson(message.bounds)},
                "rowBounds": ${boundsJson(message.rowBounds)},
                "textBounds": ${boundsJson(message.textBounds)}
              }
            """.trimIndent()
        }.joinToString(",\n")
        val routesJson = result.routes.joinToString(",\n") { route ->
            """
              {
                "id": "${escape(route.id)}",
                "name": "${escape(route.name)}",
                "routeType": "${route.routeType}",
                "message": "${escape(route.message)}",
                "intensity": "${route.intensity}",
                "riskLevel": "${route.riskLevel}",
                "riskWarning": "${escape(route.riskWarning ?: "")}",
                "fallbackMove": "${escape(route.fallbackMove ?: "")}"
              }
            """.trimIndent()
        }
        return """
            {
              "overall_result": "${overallResult(result)}",
              "generatedAt": $generatedAt,
              "sample_source": "${capture?.sampleSource?.reportValue ?: SampleSource.UNKNOWN.reportValue}",
              "appPackage": "${escape(capture?.snapshot?.appPackage ?: "unknown")}",
              "windowTitle": "${escape(capture?.snapshot?.windowTitle ?: "unknown")}",
              "screenWidth": ${capture?.snapshot?.screenWidth ?: 0},
              "screenHeight": ${capture?.snapshot?.screenHeight ?: 0},
              "serviceConnected": ${accessibilityState.serviceConnected},
              "rootAvailable": ${accessibilityState.rootAvailable},
              "capturedNodeCount": ${capture?.snapshot?.nodes?.size ?: 0},
              "rawParsedNodeCount": ${capture?.messages?.size ?: 0},
              "metadataFilteredCount": ${capture?.messages?.count { !it.isEffectiveChatMessage || it.metadataType != com.huiyi.v4.domain.model.MetadataType.NONE } ?: 0},
              "effectiveMessageCount": ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM } ?: 0},
              "parsedMessageCount": ${capture?.messages?.size ?: 0},
              "parsedMessages": [
            $messagesJson
              ],
              "LastSpeakerDecision": {
                "lastEffectiveMessageId": "${escape(result.lastSpeakerDecision.lastEffectiveMessage?.id ?: "none")}",
                "lastEffectiveMessageText": "${escape(result.lastSpeakerDecision.lastEffectiveMessage?.normalizedText ?: "none")}",
                "lastSpeaker": "${result.lastSpeakerDecision.lastSpeaker ?: "none"}",
                "shouldReply": ${result.lastSpeakerDecision.shouldReply},
                "reason": "${escape(result.lastSpeakerDecision.reason)}"
              },
              "TacticalDecision": {
                "decisionType": "${result.tacticalDecision.decisionType}",
                "situation": "${escape(result.tacticalDecision.situation)}",
                "coreInsight": "${escape(result.tacticalDecision.coreInsight)}",
                "bestMove": "${escape(result.tacticalDecision.bestMove)}",
                "riskLevel": "${result.tacticalDecision.influenceProfile.riskLevel}",
                "fallbackMove": "${escape(result.tacticalDecision.fallbackMove ?: "")}"
              },
              "lastSpeaker": "${result.lastSpeakerDecision.lastSpeaker ?: "none"}",
              "shouldReply": ${result.lastSpeakerDecision.shouldReply},
              "decisionType": "${result.tacticalDecision.decisionType}",
              "apiCalled": ${result.apiCalled},
              "overlayShownInTargetApp": ${result.overlayShownInTargetApp},
              "foregroundPackageWhenPanelShown": "${escape(result.foregroundPackageWhenPanelShown ?: "unknown")}",
              "huiyiActivityOpened": ${result.huiyiActivityOpened},
              "userStayedInChatApp": ${result.userStayedInChatApp},
              "resultShownAsOverlay": ${result.resultShownAsOverlay},
              "mainActivityOpened": ${result.mainActivityOpened},
              "routesCount": ${result.routes.size},
              "ReplyRoutes": [
            $routesJson
              ]
            }
        """.trimIndent()
    }

    fun writeTo(directory: File, result: CurrentScreenPipelineResult, accessibilityState: HuiyiAccessibilityState): Result<EvidencePackFiles> = runCatching {
        directory.mkdirs()
        val md = File(directory, "real-device-current-screen-report-for-gpt.md")
        val json = File(directory, "real-device-current-screen-report.json")
        val now = System.currentTimeMillis()
        md.writeText(buildMarkdown(result, accessibilityState, now), Charsets.UTF_8)
        json.writeText(buildJson(result, accessibilityState, now), Charsets.UTF_8)
        EvidencePackFiles(md, json)
    }

    private fun formatMessage(index: Int, message: com.huiyi.v4.domain.model.MessageNode): String {
        val side = when (message.speaker) {
            Speaker.ME -> "right"
            Speaker.OTHER -> "left"
            Speaker.SYSTEM -> "system"
            Speaker.UNKNOWN -> "unknown"
        }
        val content = message.content
        val row = message.rowBounds ?: message.bounds
        val textBounds = message.textBounds ?: message.bounds
        val boundsInfo = " rowBounds=${row?.left},${row?.top},${row?.right},${row?.bottom} textBounds=${textBounds?.left},${textBounds?.top},${textBounds?.right},${textBounds?.bottom} inferredSide=${message.inferredSide ?: side}"
        return if (content is MessageContent.Voice) {
            "[m${index.toString().padStart(3, '0')}][$side][${message.speaker.name.lowercase()} voice ${content.transcriptStatus.name.lowercase()}] [语音 ${content.durationSeconds ?: "?"}秒]$boundsInfo speakerReason=${message.speakerReason}"
        } else {
            val reason = message.speakerReason ?: "unknown_visual_bounds"
            "[m${index.toString().padStart(3, '0')}][$side][${message.speaker.name.lowercase()} ${message.speakerConfidence}% $reason] ${message.normalizedText.orEmpty()}$boundsInfo speakerReason=$reason"
        }
    }

    private fun contentType(content: MessageContent): String = when (content) {
        is MessageContent.Text -> "text"
        is MessageContent.Voice -> "voice"
        is MessageContent.Image -> "image"
        is MessageContent.Video -> "video"
        is MessageContent.Sticker -> "sticker"
    }

    private fun boundsJson(bounds: com.huiyi.v4.domain.model.VisualBounds?): String {
        return if (bounds == null) {
            "null"
        } else {
            """{"left":${bounds.left},"top":${bounds.top},"right":${bounds.right},"bottom":${bounds.bottom}}"""
        }
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
}
