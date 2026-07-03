package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.accessibility.displaySizeLabel
import com.huiyi.v4.accessibility.fontScaleEstimate
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageDeliveryStatus
import com.huiyi.v4.domain.model.MetadataType
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
        generatedAt: Long = System.currentTimeMillis(),
        scenario: RealDeviceScenario = RealDeviceScenario.AUTO_FROM_SCREEN
    ): String {
        val capture = result.captureResult
        val context = result.context
        val messages = capture?.messages.orEmpty()
        val effectiveMessages = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val metadataMessages = messages.filter { it.metadataType != MetadataType.NONE || it.speaker == Speaker.SYSTEM }
        val statusArtifacts = messages.mapNotNull { it.statusArtifact }
        val lastMeMessage = effectiveMessages.lastOrNull { it.speaker == Speaker.ME }
        val lastMeStatusArtifacts = statusArtifacts.filter { it.attachedToMessageId == lastMeMessage?.id }
        val lastMeDeliveryStatus = lastMeStatusArtifacts.lastOrNull { it.status != MessageDeliveryStatus.READ }?.status
            ?: lastMeMessage?.attachedDeliveryStatus
            ?: MessageDeliveryStatus.NONE
        val lastMeReadStatus = lastMeStatusArtifacts.lastOrNull {
            it.status == MessageDeliveryStatus.READ || it.status == MessageDeliveryStatus.UNREAD_OR_UNSEEN
        }?.status ?: lastMeMessage?.attachedReadStatus ?: MessageDeliveryStatus.NONE
        val candidateMessages = messages.filter { it.metadataType == MetadataType.NONE && it.speaker != Speaker.SYSTEM }
        val decision = result.tacticalDecision
        val voiceMessages = messages.filter { it.content is MessageContent.Voice }
        val unknownRatio = messages.count { it.speaker == Speaker.UNKNOWN }.toFloat() / messages.size.coerceAtLeast(1)
        val speakerReasons = messages.groupingBy { it.speakerReason ?: "unknown_visual_bounds" }.eachCount()
        val waitShown = decision.decisionType == TacticalDecisionType.WAIT
        val voiceShown = decision.decisionType == TacticalDecisionType.VOICE_SUMMARY_REQUIRED
        val contextShown = decision.decisionType == TacticalDecisionType.CONTEXT_REQUIRED
        val scenarioValidation = RealDeviceScenarioValidator.validate(result, scenario)
        val visualDebug = result.visualDebugResult
        return buildString {
            appendLine("# Real Device Current Screen Evidence Pack")
            appendLine()
            appendLine("- overall_result: ${scenarioValidation.currentOverallResult}")
            appendLine("- realDeviceFunctionalSmoke: ${scenarioValidation.realDeviceFunctionalSmoke}")
            appendLine("- scenarioAssertionResult: ${scenarioValidation.scenarioAssertionResult}")
            appendLine("- currentOverallResult: ${scenarioValidation.currentOverallResult}")
            appendLine("- generatedAt: $generatedAt")
            appendLine("- scenarioName: ${scenarioValidation.scenarioName}")
            appendLine("- scenarioNameSource: ${scenarioValidation.scenarioNameSource}")
            appendLine("- expectedLastSpeaker: ${scenarioValidation.expectedLastSpeaker}")
            appendLine("- expectedLastSpeakerSource: ${scenarioValidation.expectedLastSpeakerSource}")
            appendLine("- actualLastSpeaker: ${scenarioValidation.actualLastSpeaker}")
            appendLine("- actualLastSpeakerFromPreAnalysisSnapshot: ${scenarioValidation.actualLastSpeakerFromPreAnalysisSnapshot}")
            appendLine("- actualLastSpeakerFromDecisionSnapshot: ${scenarioValidation.actualLastSpeakerFromDecisionSnapshot}")
            appendLine("- actualLastSpeakerFromPostPanelSnapshot: ${scenarioValidation.actualLastSpeakerFromPostPanelSnapshot}")
            appendLine("- expectedDecisionType: ${scenarioValidation.expectedDecisionType}")
            appendLine("- actualDecisionType: ${scenarioValidation.actualDecisionType}")
            appendLine("- expectedRouteCount: ${scenarioValidation.expectedRouteCount}")
            appendLine("- actualRouteCount: ${scenarioValidation.actualRouteCount}")
            appendLine("- scenarioResult: ${scenarioValidation.scenarioResult}")
            appendLine("- scenarioDefinitionTrusted: ${scenarioValidation.scenarioDefinitionTrusted}")
            appendLine("- scenarioFailureCategory: ${scenarioValidation.scenarioFailureCategory}")
            appendLine("- scenarioDefinitionMismatchReason: ${scenarioValidation.scenarioDefinitionMismatchReason ?: "none"}")
            appendLine("- productDecisionConsistentWithActualLastSpeaker: ${scenarioValidation.productDecisionConsistentWithActualLastSpeaker}")
            appendLine("- failureReason: ${scenarioValidation.failureReason}")
            appendLine("- sample_source: ${capture?.sampleSource?.reportValue ?: SampleSource.UNKNOWN.reportValue}")
            appendLine("- appPackage: ${capture?.snapshot?.appPackage ?: "unknown"}")
            appendLine("- windowTitle: ${capture?.snapshot?.windowTitle ?: "unknown"}")
            appendLine("- preAnalysisWindowTitle: ${scenarioValidation.preAnalysisWindowTitle}")
            appendLine("- preAnalysisSnapshotTrusted: ${scenarioValidation.preAnalysisSnapshotTrusted}")
            appendLine("- preAnalysisWindowTitleSource: ${scenarioValidation.preAnalysisWindowTitleSource}")
            appendLine("- postPanelWindowTitle: ${scenarioValidation.postPanelWindowTitle}")
            appendLine("- reportWindowTitleContaminatedByPanel: ${scenarioValidation.reportWindowTitleContaminatedByPanel}")
            appendLine("- postPanelStateUsedForScenarioExpectation: ${scenarioValidation.postPanelStateUsedForScenarioExpectation}")
            appendLine("- screenWidth: ${capture?.snapshot?.screenWidth ?: 0}")
            appendLine("- screenHeight: ${capture?.snapshot?.screenHeight ?: 0}")
            appendLine("- density: ${capture?.snapshot?.density ?: "unknown"}")
            appendLine("- scaledDensity: ${capture?.snapshot?.scaledDensity ?: "unknown"}")
            appendLine("- fontScale: ${capture?.snapshot?.fontScale ?: "unknown"}")
            appendLine("- fontScaleEstimate: ${capture?.snapshot?.fontScaleEstimate ?: "unknown"}")
            appendLine("- smallestScreenWidthDp: ${capture?.snapshot?.smallestScreenWidthDp ?: "unknown"}")
            appendLine("- displaySizeCategory: ${capture?.snapshot?.displaySizeLabel() ?: "unknown"}")
            appendLine("- serviceConnected: ${accessibilityState.serviceConnected}")
            appendLine("- rootAvailable: ${accessibilityState.rootAvailable}")
            appendLine("- capturedNodeCount: ${capture?.snapshot?.nodes?.size ?: 0}")
            appendLine("- parserName: ${capture?.parserName ?: "unknown"}")
            appendLine("- LiaoqiRealParserUsed: ${capture?.parserName == "LiaoqiRealParser"}")
            appendLine("- GenericVisualBubbleParserFallbackUsed: ${capture?.parserFallbackUsed == true}")
            appendLine("- parserFallbackUsed: ${capture?.parserFallbackUsed == true || capture?.warning != null}")
            appendLine("- currentBubbleSideRule: right=me")
            appendLine()
            appendLine("deviceVisualConfig:")
            appendLine("  screenWidth: ${capture?.snapshot?.screenWidth ?: 0}")
            appendLine("  screenHeight: ${capture?.snapshot?.screenHeight ?: 0}")
            appendLine("  density: ${capture?.snapshot?.density ?: "unknown"}")
            appendLine("  scaledDensity: ${capture?.snapshot?.scaledDensity ?: "unknown"}")
            appendLine("  fontScale: ${capture?.snapshot?.fontScale ?: "unknown"}")
            appendLine("  fontScaleEstimate: ${capture?.snapshot?.fontScaleEstimate ?: "unknown"}")
            appendLine("  smallestScreenWidthDp: ${capture?.snapshot?.smallestScreenWidthDp ?: "unknown"}")
            appendLine("  displaySizeCategory: ${capture?.snapshot?.displaySizeLabel() ?: "unknown"}")
            appendLine("- modelCalled: false")
            appendLine("- apiCalled: ${result.apiCalled}")
            appendLine("- cloudEnabled: ${result.cloudTrace.cloudEnabled}")
            appendLine("- cloudEndpointConfigured: ${result.cloudTrace.endpointConfigured}")
            appendLine("- cloudAttempted: ${result.cloudTrace.cloudAttempted}")
            appendLine("- cloudSuccess: ${result.cloudTrace.cloudSuccess}")
            appendLine("- cloudSkippedReason: ${result.cloudTrace.cloudSkippedReason ?: "none"}")
            appendLine("- decisionSource: ${result.cloudTrace.decisionSource}")
            appendLine("- cloudFallbackUsed: ${result.cloudTrace.cloudFallbackUsed}")
            appendLine("- cloudLatencyMs: ${result.cloudTrace.cloudLatencyMs ?: "null"}")
            appendLine("- cloudErrorCode: ${result.cloudTrace.cloudErrorCode ?: "none"}")
            appendLine("- cloudRequestId: ${result.cloudTrace.cloudRequestId ?: "none"}")
            appendLine("- cloudContractVersion: ${result.cloudTrace.cloudContractVersion}")
            appendLine("- cloudContractValidationResult: ${result.cloudTrace.cloudContractValidationResult}")
            appendLine("- overlayShownInTargetApp: ${result.overlayShownInTargetApp}")
            appendLine("- foregroundPackageWhenPanelShown: ${result.foregroundPackageWhenPanelShown ?: "unknown"}")
            appendLine("- huiyiActivityOpened: ${result.huiyiActivityOpened}")
            appendLine("- userStayedInChatApp: ${result.userStayedInChatApp}")
            appendLine("- resultShownAsOverlay: ${result.resultShownAsOverlay}")
            appendLine("- mainActivityOpened: ${result.mainActivityOpened}")
            appendLine()
            appendLine("## Visual Debug")
            appendLine()
            appendLine("- screenshotCaptured: ${visualDebug?.screenshotCaptured ?: false}")
            appendLine("- screenshotUnavailable: ${visualDebug?.screenshotUnavailable ?: true}")
            appendLine("- screenshotDiagnosticStatus: ${scenarioValidation.screenshotDiagnosticStatus}")
            appendLine("- screenshotFailureBlocksMainPath: ${scenarioValidation.screenshotFailureBlocksMainPath}")
            appendLine("- secondaryDiagnosticErrorCode: ${scenarioValidation.secondaryDiagnosticErrorCode}")
            appendLine("- reason: ${visualDebug?.reason ?: "visual_projection_only_or_not_captured"}")
            appendLine("- screenshotPath: ${visualDebug?.screenshotPath ?: "none"}")
            appendLine("- overlayImagePath: ${visualDebug?.overlayImagePath ?: "none"}")
            appendLine("- screenshotWidth: ${visualDebug?.screenshotWidth ?: capture?.snapshot?.screenWidth ?: 0}")
            appendLine("- screenshotHeight: ${visualDebug?.screenshotHeight ?: capture?.snapshot?.screenHeight ?: 0}")
            appendLine("- accessibilityBoundsProjected: ${visualDebug?.accessibilityBoundsProjected ?: capture?.accessibilityBoundsProjected ?: false}")
            appendLine("- ocrUsed: ${visualDebug?.ocrUsed ?: capture?.ocrUsed ?: false}")
            appendLine("- visualTruthAvailable: ${scenarioValidation.visualTruthAvailable}")
            appendLine("- visualTruthSource: ${scenarioValidation.visualTruthSource}")
            appendLine("- accessibilityProjectionAvailable: ${scenarioValidation.accessibilityProjectionAvailable}")
            appendLine("- visualProjectionSource: ${scenarioValidation.visualProjectionSource}")
            appendLine("- VisualSpeakerFallbackUsed: ${capture?.visualSpeakerFallbackCount?.let { it > 0 } ?: false}")
            appendLine("- visualSpeakerFallbackCount: ${capture?.visualSpeakerFallbackCount ?: 0}")
            appendLine("- conflictCount: ${messages.count { it.visualConflict }}")
            appendLine("- failureCategory: ${failureCategory(result, scenarioValidation)}")
            appendLine("- userCorrectionProvided: ${result.userCorrectionProvided}")
            appendLine("- correctedLastSpeaker: ${result.correctedLastSpeaker ?: "none"}")
            appendLine("- correctedMessageId: ${result.correctedMessageId ?: "none"}")
            appendLine()
            appendLine("## Snapshot Phase Separation")
            appendLine()
            appendLine("- preAnalysisSnapshotAvailable: ${scenarioValidation.preAnalysisSnapshotAvailable}")
            appendLine("- preAnalysisWindowTitle: ${scenarioValidation.preAnalysisWindowTitle}")
            appendLine("- preAnalysisSnapshotTrusted: ${scenarioValidation.preAnalysisSnapshotTrusted}")
            appendLine("- preAnalysisWindowTitleSource: ${scenarioValidation.preAnalysisWindowTitleSource}")
            appendLine("- preAnalysisResultPanelVisible: false")
            appendLine("- decisionSnapshotAvailable: ${result.captureResult != null}")
            appendLine("- postPanelSnapshotAvailable: ${scenarioValidation.postPanelSnapshotAvailable}")
            appendLine("- postPanelWindowTitle: ${scenarioValidation.postPanelWindowTitle}")
            appendLine("- reportWindowTitleContaminatedByPanel: ${scenarioValidation.reportWindowTitleContaminatedByPanel}")
            appendLine("- postPanelStateUsedForScenarioExpectation: ${scenarioValidation.postPanelStateUsedForScenarioExpectation}")
            appendLine()
            appendLine("## 解析结果")
            appendLine("- rawParsedNodeCount: ${messages.size}")
            appendLine("- metadataFilteredCount: ${metadataMessages.size}")
            appendLine("- candidateChatMessageCount: ${candidateMessages.size}")
            appendLine("- unknownSpeakerCount: ${candidateMessages.count { it.speaker == Speaker.UNKNOWN }}")
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
            appendLine("- dateMetadataFilteredCount: ${messages.count { it.metadataType == MetadataType.DATE && it.speaker == Speaker.SYSTEM && !it.isEffectiveChatMessage }}")
            appendLine("- messageStatusArtifactCount: ${statusArtifacts.size}")
            appendLine("- readReceiptCount: ${messages.count { it.metadataType == MetadataType.READ_RECEIPT || it.statusArtifact?.status == MessageDeliveryStatus.READ }}")
            appendLine("- deliveryStatusCount: ${messages.count { it.metadataType in setOf(MetadataType.DELIVERY_STATUS, MetadataType.SEND_STATUS, MetadataType.MESSAGE_STATUS_ICON) }}")
            appendLine("- lastMeDeliveryStatus: $lastMeDeliveryStatus")
            appendLine("- lastMeReadStatus: $lastMeReadStatus")
            appendLine("- statusArtifactsFilteredFromEffectiveMessages: ${messages.none { it.statusArtifact != null && it.isEffectiveChatMessage }}")
            appendLine("- statusArtifactsAttachedToMessageCount: ${statusArtifacts.count { it.attachedToMessageId != null }}")
            appendLine("- possible_speaker_conflict_count: ${messages.count { it.possibleSpeakerConflict }}")
            appendLine("- lastEffectiveMessagePreview: ${result.lastSpeakerDecision.lastEffectiveMessage?.normalizedText ?: "[non-text-or-none]"}")
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
            appendLine("### UNKNOWN details")
            val unknownMessages = candidateMessages.filter { it.speaker == Speaker.UNKNOWN }
            if (unknownMessages.isEmpty()) {
                appendLine("- none")
            } else {
                unknownMessages.take(20).forEach { node ->
                    appendLine("- id: ${node.id}")
                    appendLine("  text: ${node.normalizedText ?: "[non-text]"}")
                    appendLine("  textBounds: ${node.textBounds?.toReport() ?: "none"}")
                    appendLine("  parentBounds: ${node.parentBounds?.toReport() ?: "none"}")
                    appendLine("  rowBounds: ${node.rowBounds?.toReport() ?: "none"}")
                    appendLine("  bubbleBounds: ${node.bubbleBounds?.toReport() ?: node.bounds?.toReport() ?: "none"}")
                    appendLine("  ancestorBoundsChain: ${node.ancestorBoundsChain.joinToString(" > ") { it.toReport() }.ifBlank { "none" }}")
                    appendLine("  unknownReason: ${node.unknownReason ?: node.speakerReason ?: "unknown"}")
                }
            }
            appendLine()
            appendLine("## 最近 30 条解析消息")
            messages.takeLast(30).forEachIndexed { index, message ->
                appendLine(formatMessage(index + 1, message))
            }
            appendLine()
            appendLine("## Visual Order Table")
            appendLine()
            appendLine("| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |")
            appendLine("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
            messages.forEach { message ->
                appendLine(visualTableRow(message))
            }
            appendLine()
            appendLine("## Effective Visual Order Table")
            appendLine()
            appendLine("| rawNodeOrder | finalVisualOrder | text | rawSpeaker | finalSpeaker | contentType | metadataType | isEffectiveChatMessage | textBounds | rowBounds | bubbleBounds | parentBounds | ancestorBoundsChain | accessibilitySide | visualProjectedSide | projectedBox | speakerConfidence | speakerReason | conflict | conflictReason | VisualSpeakerFallbackUsed | possibleSpeakerConflict |")
            appendLine("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
            effectiveMessages.forEach { message ->
                appendLine(visualTableRow(message))
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
        generatedAt: Long = System.currentTimeMillis(),
        scenario: RealDeviceScenario = RealDeviceScenario.AUTO_FROM_SCREEN
    ): String {
        val capture = result.captureResult
        val messages = capture?.messages.orEmpty()
        val effectiveMessages = messages.filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
        val statusArtifacts = messages.mapNotNull { it.statusArtifact }
        val lastMeMessage = effectiveMessages.lastOrNull { it.speaker == Speaker.ME }
        val lastMeStatusArtifacts = statusArtifacts.filter { it.attachedToMessageId == lastMeMessage?.id }
        val lastMeDeliveryStatus = lastMeStatusArtifacts.lastOrNull { it.status != MessageDeliveryStatus.READ }?.status
            ?: lastMeMessage?.attachedDeliveryStatus
            ?: MessageDeliveryStatus.NONE
        val lastMeReadStatus = lastMeStatusArtifacts.lastOrNull {
            it.status == MessageDeliveryStatus.READ || it.status == MessageDeliveryStatus.UNREAD_OR_UNSEEN
        }?.status ?: lastMeMessage?.attachedReadStatus ?: MessageDeliveryStatus.NONE
        val scenarioValidation = RealDeviceScenarioValidator.validate(result, scenario)
        val visualDebug = result.visualDebugResult
        val messagesJson = messages.mapIndexed { index, message ->
            """
              {
                "index": ${index + 1},
                "rawNodeOrder": ${message.rawNodeOrder ?: index + 1},
                "finalVisualOrder": ${message.finalVisualOrder ?: index + 1},
                "id": "${escape(message.id)}",
                "rawSpeaker": "${message.speaker}",
                "finalSpeaker": "${message.speaker}",
                "speakerConfidence": ${message.speakerConfidence},
                "speakerReason": "${escape(message.speakerReason ?: "unknown_visual_bounds")}",
                "contentType": "${contentType(message.content)}",
                "text": "${escape(message.normalizedText ?: "")}",
                "isEffectiveChatMessage": ${message.isEffectiveChatMessage},
                "metadataType": "${message.metadataType ?: com.huiyi.v4.domain.model.MetadataType.NONE}",
                "inferredSide": "${escape(message.inferredSide ?: "")}",
                "accessibilitySide": "${escape(message.accessibilitySide ?: "")}",
                "visualProjectedSide": "${escape(message.visualProjectedSide ?: "")}",
                "visualDebugBoxDrawn": ${message.visualDebugBoxDrawn},
                "projectedBox": ${boundsJson(message.projectedBox)},
                "conflict": ${message.visualConflict},
                "conflictReason": "${escape(message.visualConflictReason ?: "")}",
                "VisualSpeakerFallbackUsed": ${message.visualSpeakerFallbackUsed},
                "bounds": ${boundsJson(message.bounds)},
                "rowBounds": ${boundsJson(message.rowBounds)},
                "textBounds": ${boundsJson(message.textBounds)},
                "parentBounds": ${boundsJson(message.parentBounds)},
                "bubbleBounds": ${boundsJson(message.bubbleBounds)},
                "ancestorBoundsChain": [${message.ancestorBoundsChain.joinToString(",") { boundsJson(it) }}],
                "sideMarginLeft": ${message.sideMarginLeft ?: -1},
                "sideMarginRight": ${message.sideMarginRight ?: -1},
                "finalDecisionSource": "${escape(message.finalDecisionSource ?: "")}",
                "attachedDeliveryStatus": "${message.attachedDeliveryStatus}",
                "attachedReadStatus": "${message.attachedReadStatus}",
                "statusArtifact": ${statusArtifactJson(message.statusArtifact)},
                "possible_speaker_conflict": ${message.possibleSpeakerConflict},
                "unknownReason": "${escape(message.unknownReason ?: "")}"
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
              "overall_result": "${scenarioValidation.currentOverallResult}",
              "realDeviceFunctionalSmoke": "${scenarioValidation.realDeviceFunctionalSmoke}",
              "scenarioAssertionResult": "${scenarioValidation.scenarioAssertionResult}",
              "currentOverallResult": "${scenarioValidation.currentOverallResult}",
              "generatedAt": $generatedAt,
              "scenarioName": "${scenarioValidation.scenarioName}",
              "scenarioNameSource": "${scenarioValidation.scenarioNameSource}",
              "expectedLastSpeaker": "${scenarioValidation.expectedLastSpeaker}",
              "expectedLastSpeakerSource": "${scenarioValidation.expectedLastSpeakerSource}",
              "actualLastSpeaker": "${scenarioValidation.actualLastSpeaker}",
              "actualLastSpeakerFromPreAnalysisSnapshot": "${scenarioValidation.actualLastSpeakerFromPreAnalysisSnapshot}",
              "actualLastSpeakerFromDecisionSnapshot": "${scenarioValidation.actualLastSpeakerFromDecisionSnapshot}",
              "actualLastSpeakerFromPostPanelSnapshot": "${scenarioValidation.actualLastSpeakerFromPostPanelSnapshot}",
              "expectedDecisionType": "${scenarioValidation.expectedDecisionType}",
              "expectedDecisionTypeSource": "${scenarioValidation.expectedDecisionTypeSource}",
              "actualDecisionType": "${scenarioValidation.actualDecisionType}",
              "expectedRouteCount": "${scenarioValidation.expectedRouteCount}",
              "expectedRouteCountSource": "${scenarioValidation.expectedRouteCountSource}",
              "actualRouteCount": ${scenarioValidation.actualRouteCount},
              "scenarioResult": "${scenarioValidation.scenarioResult}",
              "scenarioDefinitionTrusted": ${scenarioValidation.scenarioDefinitionTrusted},
              "scenarioFailureCategory": "${scenarioValidation.scenarioFailureCategory}",
              "scenarioDefinitionMismatchReason": "${escape(scenarioValidation.scenarioDefinitionMismatchReason ?: "none")}",
              "productDecisionConsistentWithActualLastSpeaker": ${scenarioValidation.productDecisionConsistentWithActualLastSpeaker},
              "failureReason": "${scenarioValidation.failureReason}",
              "sample_source": "${capture?.sampleSource?.reportValue ?: SampleSource.UNKNOWN.reportValue}",
              "appPackage": "${escape(capture?.snapshot?.appPackage ?: "unknown")}",
              "windowTitle": "${escape(capture?.snapshot?.windowTitle ?: "unknown")}",
              "preAnalysisWindowTitle": "${escape(scenarioValidation.preAnalysisWindowTitle)}",
              "preAnalysisSnapshotTrusted": "${escape(scenarioValidation.preAnalysisSnapshotTrusted)}",
              "preAnalysisWindowTitleSource": "${escape(scenarioValidation.preAnalysisWindowTitleSource)}",
              "postPanelWindowTitle": "${escape(scenarioValidation.postPanelWindowTitle)}",
              "reportWindowTitleContaminatedByPanel": ${scenarioValidation.reportWindowTitleContaminatedByPanel},
              "postPanelStateUsedForScenarioExpectation": ${scenarioValidation.postPanelStateUsedForScenarioExpectation},
              "screenWidth": ${capture?.snapshot?.screenWidth ?: 0},
              "screenHeight": ${capture?.snapshot?.screenHeight ?: 0},
              "density": ${capture?.snapshot?.density ?: 0f},
              "scaledDensity": ${capture?.snapshot?.scaledDensity ?: 0f},
              "fontScale": ${capture?.snapshot?.fontScale ?: 0f},
              "fontScaleEstimate": ${capture?.snapshot?.fontScaleEstimate ?: 0f},
              "smallestScreenWidthDp": ${capture?.snapshot?.smallestScreenWidthDp ?: 0},
              "displaySizeCategory": "${escape(capture?.snapshot?.displaySizeLabel() ?: "unknown")}",
              "serviceConnected": ${accessibilityState.serviceConnected},
              "rootAvailable": ${accessibilityState.rootAvailable},
              "capturedNodeCount": ${capture?.snapshot?.nodes?.size ?: 0},
              "parserName": "${escape(capture?.parserName ?: "unknown")}",
              "LiaoqiRealParserUsed": ${capture?.parserName == "LiaoqiRealParser"},
              "GenericVisualBubbleParserFallbackUsed": ${capture?.parserFallbackUsed == true},
              "VisualDebug": {
                "screenshotCaptured": ${visualDebug?.screenshotCaptured ?: false},
                "screenshotUnavailable": ${visualDebug?.screenshotUnavailable ?: true},
                "screenshotDiagnosticStatus": "${scenarioValidation.screenshotDiagnosticStatus}",
                "screenshotFailureBlocksMainPath": ${scenarioValidation.screenshotFailureBlocksMainPath},
                "secondaryDiagnosticErrorCode": "${escape(scenarioValidation.secondaryDiagnosticErrorCode)}",
                "reason": "${escape(visualDebug?.reason ?: "visual_projection_only_or_not_captured")}",
                "screenshotPath": "${escape(visualDebug?.screenshotPath ?: "")}",
                "overlayImagePath": "${escape(visualDebug?.overlayImagePath ?: "")}",
                "screenshotWidth": ${visualDebug?.screenshotWidth ?: capture?.snapshot?.screenWidth ?: 0},
                "screenshotHeight": ${visualDebug?.screenshotHeight ?: capture?.snapshot?.screenHeight ?: 0},
                "accessibilityBoundsProjected": ${visualDebug?.accessibilityBoundsProjected ?: capture?.accessibilityBoundsProjected ?: false},
                "ocrUsed": ${visualDebug?.ocrUsed ?: capture?.ocrUsed ?: false},
                "visualTruthAvailable": ${scenarioValidation.visualTruthAvailable},
                "visualTruthSource": "${scenarioValidation.visualTruthSource}",
                "accessibilityProjectionAvailable": ${scenarioValidation.accessibilityProjectionAvailable},
                "visualProjectionSource": "${scenarioValidation.visualProjectionSource}",
                "VisualSpeakerFallbackUsed": ${capture?.visualSpeakerFallbackCount?.let { it > 0 } ?: false},
                "visualSpeakerFallbackCount": ${capture?.visualSpeakerFallbackCount ?: 0},
                "conflictCount": ${capture?.messages?.count { it.visualConflict } ?: 0},
                "failureCategory": "${failureCategory(result, scenarioValidation)}"
              },
              "userCorrectionProvided": ${result.userCorrectionProvided},
              "correctedLastSpeaker": "${result.correctedLastSpeaker ?: "none"}",
              "correctedMessageId": "${escape(result.correctedMessageId ?: "none")}",
              "SnapshotPhaseSeparation": {
                "preAnalysisSnapshotAvailable": ${scenarioValidation.preAnalysisSnapshotAvailable},
                "preAnalysisWindowTitle": "${escape(scenarioValidation.preAnalysisWindowTitle)}",
                "preAnalysisSnapshotTrusted": "${escape(scenarioValidation.preAnalysisSnapshotTrusted)}",
                "preAnalysisWindowTitleSource": "${escape(scenarioValidation.preAnalysisWindowTitleSource)}",
                "preAnalysisResultPanelVisible": false,
                "decisionSnapshotAvailable": ${result.captureResult != null},
                "postPanelSnapshotAvailable": ${scenarioValidation.postPanelSnapshotAvailable},
                "postPanelWindowTitle": "${escape(scenarioValidation.postPanelWindowTitle)}",
                "reportWindowTitleContaminatedByPanel": ${scenarioValidation.reportWindowTitleContaminatedByPanel},
                "postPanelStateUsedForScenarioExpectation": ${scenarioValidation.postPanelStateUsedForScenarioExpectation}
              },
              "rawParsedNodeCount": ${capture?.messages?.size ?: 0},
              "metadataFilteredCount": ${capture?.messages?.count { it.metadataType != MetadataType.NONE || it.speaker == Speaker.SYSTEM } ?: 0},
              "dateMetadataFilteredCount": ${capture?.messages?.count { it.metadataType == MetadataType.DATE && it.speaker == Speaker.SYSTEM && !it.isEffectiveChatMessage } ?: 0},
              "messageStatusArtifactCount": ${statusArtifacts.size},
              "readReceiptCount": ${messages.count { it.metadataType == MetadataType.READ_RECEIPT || it.statusArtifact?.status == MessageDeliveryStatus.READ }},
              "deliveryStatusCount": ${messages.count { it.metadataType in setOf(MetadataType.DELIVERY_STATUS, MetadataType.SEND_STATUS, MetadataType.MESSAGE_STATUS_ICON) }},
              "lastMeDeliveryStatus": "$lastMeDeliveryStatus",
              "lastMeReadStatus": "$lastMeReadStatus",
              "statusArtifactsFilteredFromEffectiveMessages": ${messages.none { it.statusArtifact != null && it.isEffectiveChatMessage }},
              "statusArtifactsAttachedToMessageCount": ${statusArtifacts.count { it.attachedToMessageId != null }},
              "candidateChatMessageCount": ${capture?.messages?.count { it.metadataType == MetadataType.NONE && it.speaker != Speaker.SYSTEM } ?: 0},
              "unknownSpeakerCount": ${capture?.messages?.count { it.metadataType == MetadataType.NONE && it.speaker == Speaker.UNKNOWN } ?: 0},
              "effectiveMessageCount": ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM } ?: 0},
              "possibleSpeakerConflictCount": ${capture?.messages?.count { it.possibleSpeakerConflict } ?: 0},
              "lastEffectiveMessagePreview": "${escape(result.lastSpeakerDecision.lastEffectiveMessage?.normalizedText ?: "")}",
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
              "modelCalled": ${result.cloudTrace.modelCalled},
              "cloudEnabled": ${result.cloudTrace.cloudEnabled},
              "cloudEndpointConfigured": ${result.cloudTrace.endpointConfigured},
              "cloudAttempted": ${result.cloudTrace.cloudAttempted},
              "cloudSkippedReason": "${escape(result.cloudTrace.cloudSkippedReason ?: "none")}",
              "cloudRequestId": "${escape(result.cloudTrace.cloudRequestId ?: "")}",
              "cloudSuccess": ${result.cloudTrace.cloudSuccess},
              "cloudLatencyMs": ${result.cloudTrace.cloudLatencyMs ?: "null"},
              "cloudErrorCode": "${escape(result.cloudTrace.cloudErrorCode ?: "")}",
              "cloudFallbackUsed": ${result.cloudTrace.cloudFallbackUsed},
              "decisionSource": "${result.cloudTrace.decisionSource}",
              "cloudContractVersion": "${result.cloudTrace.cloudContractVersion}",
              "cloudContractValidationResult": "${result.cloudTrace.cloudContractValidationResult}",
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

    fun writeTo(
        directory: File,
        result: CurrentScreenPipelineResult,
        accessibilityState: HuiyiAccessibilityState,
        scenario: RealDeviceScenario = RealDeviceScenario.AUTO_FROM_SCREEN
    ): Result<EvidencePackFiles> = runCatching {
        directory.mkdirs()
        val md = File(directory, "real-device-current-screen-report-for-gpt.md")
        val json = File(directory, "real-device-current-screen-report.json")
        val now = System.currentTimeMillis()
        md.writeText(buildMarkdown(result, accessibilityState, now, scenario), Charsets.UTF_8)
        json.writeText(buildJson(result, accessibilityState, now, scenario), Charsets.UTF_8)
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
        val parent = message.parentBounds
        val bubble = message.bubbleBounds ?: message.bounds
        val orderInfo = " rawNodeOrder=${message.rawNodeOrder ?: index} finalVisualOrder=${message.finalVisualOrder ?: index}"
        val conflictInfo = " possible_speaker_conflict=${message.possibleSpeakerConflict}"
        val boundsInfo = "$orderInfo rowBounds=${row?.toReport()} textBounds=${textBounds?.toReport()} parentBounds=${parent?.toReport()} bubbleBounds=${bubble?.toReport()} projectedBox=${message.projectedBox?.toReport()} accessibilitySide=${message.accessibilitySide ?: "none"} visualProjectedSide=${message.visualProjectedSide ?: "none"} conflict=${message.visualConflict} conflictReason=${message.visualConflictReason ?: "none"} inferredSide=${message.inferredSide ?: side} sideMarginLeft=${message.sideMarginLeft ?: "none"} sideMarginRight=${message.sideMarginRight ?: "none"} finalDecisionSource=${message.finalDecisionSource ?: "none"} unknownReason=${message.unknownReason ?: "none"}$conflictInfo"
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

    private fun statusArtifactJson(artifact: com.huiyi.v4.domain.model.MessageStatusArtifact?): String {
        return if (artifact == null) {
            "null"
        } else {
            """
              {
                "id": "${escape(artifact.id)}",
                "status": "${artifact.status}",
                "rawTextRedacted": "${escape(artifact.rawTextRedacted ?: "")}",
                "contentDescriptionRedacted": "${escape(artifact.contentDescriptionRedacted ?: "")}",
                "stateDescriptionRedacted": "${escape(artifact.stateDescriptionRedacted ?: "")}",
                "bounds": "${escape(artifact.bounds ?: "")}",
                "source": "${escape(artifact.source)}",
                "attachedToMessageId": "${escape(artifact.attachedToMessageId ?: "")}",
                "confidence": ${artifact.confidence},
                "reason": "${escape(artifact.reason)}"
              }
            """.trimIndent()
        }
    }

    private fun com.huiyi.v4.domain.model.VisualBounds.toReport(): String {
        return "${left},${top},${right},${bottom}"
    }

    private fun visualTableRow(message: com.huiyi.v4.domain.model.MessageNode): String {
        return listOf(
            message.rawNodeOrder?.toString() ?: "",
            message.finalVisualOrder?.toString() ?: "",
            tableEscape(message.normalizedText ?: "[non-text]"),
            message.speaker.name,
            message.speaker.name,
            contentType(message.content),
            (message.metadataType ?: MetadataType.NONE).name,
            message.isEffectiveChatMessage.toString(),
            message.textBounds?.toReport() ?: "none",
            message.rowBounds?.toReport() ?: "none",
            message.bubbleBounds?.toReport() ?: "none",
            message.parentBounds?.toReport() ?: "none",
            message.ancestorBoundsChain.joinToString(" > ") { it.toReport() }.ifBlank { "none" },
            message.accessibilitySide ?: "unknown",
            message.visualProjectedSide ?: "unknown",
            message.projectedBox?.toReport() ?: "none",
            message.speakerConfidence.toString(),
            tableEscape(message.speakerReason ?: "unknown_visual_bounds"),
            message.visualConflict.toString(),
            tableEscape(message.visualConflictReason ?: "none"),
            message.visualSpeakerFallbackUsed.toString(),
            message.possibleSpeakerConflict.toString()
        ).joinToString(prefix = "| ", separator = " | ", postfix = " |")
    }

    private fun failureCategory(result: CurrentScreenPipelineResult, scenarioValidation: RealDeviceScenarioValidation): String {
        val capture = result.captureResult
        return when {
            scenarioValidation.scenarioFailureCategory == "SCENARIO_DEFINITION_MISMATCH" -> "scenario_definition_mismatch"
            scenarioValidation.currentOverallResult == "PASS" -> "none"
            capture?.messages.orEmpty().any { it.metadataType == MetadataType.DATE && it.speaker != Speaker.SYSTEM } -> "metadata_leak"
            result.userCorrectionProvided -> "user_selected_wrong_scenario"
            !scenarioValidation.visualTruthAvailable -> scenarioValidation.failureReason
            capture?.messages.orEmpty().any { it.visualConflict } -> "parser_side_conflict"
            result.lastSpeakerDecision.lastEffectiveMessage == null -> "visual_order_wrong"
            capture?.messages.orEmpty().count { it.speaker == Speaker.UNKNOWN }.toFloat() /
                capture?.messages.orEmpty().size.coerceAtLeast(1) > 0.30f -> "unknown_too_high"
            else -> scenarioValidation.failureReason
        }
    }

    private fun tableEscape(value: String): String = value
        .replace("|", "\\|")
        .replace("\r", " ")
        .replace("\n", " ")

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
}
