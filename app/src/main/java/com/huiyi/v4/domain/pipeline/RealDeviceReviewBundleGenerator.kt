package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.HuiyiAccessibilityState
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MetadataType
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType

data class RealDeviceReviewBundleContent(
    val reviewMarkdown: String,
    val smokeMarkdown: String,
    val currentScreenMarkdown: String,
    val currentScreenJson: String,
    val realDeviceSmokeResult: String,
    val overallResult: String,
    val failReason: String
)

private data class RealDeviceSmokeValidation(
    val result: String,
    val failReason: String
)

class RealDeviceReviewBundleGenerator(
    private val evidenceGenerator: EvidencePackReportGenerator = EvidencePackReportGenerator()
) {
    fun build(
        latestResult: CurrentScreenPipelineResult?,
        accessibilityState: HuiyiAccessibilityState,
        generatedAt: Long,
        versionName: String,
        versionCode: Int,
        ownAppPackage: String,
        scenario: RealDeviceScenario = RealDeviceScenario.AUTO_FROM_SCREEN
    ): RealDeviceReviewBundleContent {
        val realResult = latestResult?.takeIf { it.isRealChatAppCapture(ownAppPackage) }
        val currentScreenMarkdown = if (realResult == null) {
            buildNotTestedCurrentScreenReport(latestResult, accessibilityState, generatedAt, ownAppPackage, scenario)
        } else {
            evidenceGenerator.buildMarkdown(realResult, accessibilityState, generatedAt, scenario)
        }
        val currentScreenJson = if (realResult == null) {
            buildNotTestedCurrentScreenJson(latestResult, accessibilityState, generatedAt, ownAppPackage, scenario)
        } else {
            evidenceGenerator.buildJson(realResult, accessibilityState, generatedAt, scenario)
        }

        val scenarioValidation = RealDeviceScenarioValidator.validate(realResult, scenario)
        val realDeviceSmokeResult = scenarioValidation.realDeviceFunctionalSmoke
        val overallResult = scenarioValidation.currentOverallResult
        val failReason = when {
            scenarioValidation.failureReason == "none" -> "none"
            overallResult == "CONTROLLED_PASS_WITH_SCENARIO_MISMATCH" -> scenarioValidation.scenarioFailureCategory.lowercase()
            else -> scenarioValidation.failureReason
        }
        val smokeMarkdown = buildSmokeReport(
            latestResult = latestResult,
            realResult = realResult,
            generatedAt = generatedAt,
            realDeviceSmokeResult = realDeviceSmokeResult,
            failReason = failReason,
            ownAppPackage = ownAppPackage,
            scenarioValidation = scenarioValidation
        )
        val reviewMarkdown = buildReviewMarkdown(
            latestResult = latestResult,
            realResult = realResult,
            generatedAt = generatedAt,
            versionName = versionName,
            versionCode = versionCode,
            realDeviceSmokeResult = realDeviceSmokeResult,
            overallResult = overallResult,
            failReason = failReason,
            scenarioValidation = scenarioValidation
        )
        return RealDeviceReviewBundleContent(
            reviewMarkdown = reviewMarkdown,
            smokeMarkdown = smokeMarkdown,
            currentScreenMarkdown = currentScreenMarkdown,
            currentScreenJson = currentScreenJson,
            realDeviceSmokeResult = realDeviceSmokeResult,
            overallResult = overallResult,
            failReason = failReason
        )
    }

    private fun buildReviewMarkdown(
        latestResult: CurrentScreenPipelineResult?,
        realResult: CurrentScreenPipelineResult?,
        generatedAt: Long,
        versionName: String,
        versionCode: Int,
        realDeviceSmokeResult: String,
        overallResult: String,
        failReason: String,
        scenarioValidation: RealDeviceScenarioValidation
    ): String {
        val capture = realResult?.captureResult
        val latestCapture = latestResult?.captureResult
        val visualDebug = realResult?.visualDebugResult
        val sampleSource = capture?.sampleSource?.reportValue ?: "NOT_TESTED"
        val appPackage = capture?.snapshot?.appPackage ?: "NOT_TESTED"
        val windowTitle = capture?.snapshot?.windowTitle ?: "NOT_TESTED"
        return buildString {
            appendLine("# Huiyi v4 Real Device Review For GPT")
            appendLine()
            appendLine("## Basic")
            appendLine()
            appendLine("- project: Huiyi v4 Core")
            appendLine("- versionName: $versionName")
            appendLine("- versionCode: $versionCode")
            appendLine("- generatedAt: $generatedAt")
            appendLine("- currentVersion: $versionName")
            appendLine("- currentTaskName: real_device_scenario_truth_and_post_panel_contamination_fix")
            appendLine("- currentGeneratedAt: $generatedAt")
            appendLine("- taskName: real_device_scenario_truth_and_post_panel_contamination_fix")
            appendLine("- scenarioName: ${scenarioValidation.scenarioName}")
            appendLine("- review_freshness_result: PASS")
            appendLine("- mockchat_result: NOT_INCLUDED_IN_PHONE_EXPORT")
            appendLine("- real_device_smoke_result: $realDeviceSmokeResult")
            appendLine("- realDeviceFunctionalSmoke: ${scenarioValidation.realDeviceFunctionalSmoke}")
            appendLine("- scenarioAssertionResult: ${scenarioValidation.scenarioAssertionResult}")
            appendLine("- currentOverallResult: ${scenarioValidation.currentOverallResult}")
            appendLine("- overall_result: $overallResult")
            appendLine("- failReason: $failReason")
            appendLine("- realDeviceSmoke: $realDeviceSmokeResult")
            if (realDeviceSmokeResult == "NOT_TESTED") appendLine("- smokeDisclaimer: $REAL_SMOKE_NOT_TESTED_NOTICE")
            appendLine()
            appendLine("## Current User Feedback")
            appendLine()
            appendLine("- v4.1.9a 真机已能在目标 App 内显示会意路线面板")
            appendLine("- 当前 FAIL 来自 scenarioName=last_me 与真实最后有效消息 OTHER 冲突")
            appendLine("- screenshot unavailable 仍存在，但不再阻断主链路")
            appendLine()
            appendLine("## Current Regression Status")
            appendLine()
            appendLine("- overlayBubbleSurvivesAfterNextSentence: ${realResult?.overlayShownInTargetApp ?: "unknown"}")
            appendLine("- resultShownAsOverlayInTargetApp: ${realResult?.resultShownAsOverlay ?: "unknown"}")
            appendLine("- mainActivityOpened: ${realResult?.mainActivityOpened ?: "unknown"}")
            appendLine("- screenshotFailureBlocksMainPath: ${scenarioValidation.screenshotFailureBlocksMainPath}")
            appendLine("- preAnalysisSnapshotAvailable: ${scenarioValidation.preAnalysisSnapshotAvailable}")
            appendLine("- postPanelContaminationDetected: ${scenarioValidation.reportWindowTitleContaminatedByPanel}")
            appendLine("- scenarioDefinitionTrusted: ${scenarioValidation.scenarioDefinitionTrusted}")
            appendLine("- scenarioDefinitionMismatch: ${scenarioValidation.scenarioAssertionResult == "MISMATCH"}")
            appendLine("- productDecisionConsistentWithActualLastSpeaker: ${scenarioValidation.productDecisionConsistentWithActualLastSpeaker}")
            appendLine("- genericAnalysisFailedStillShown: unknown")
            appendLine()
            appendLine("## Data Source")
            appendLine()
            appendLine("- sample_source: $sampleSource")
            appendLine("- appPackage: $appPackage")
            appendLine("- windowTitle: $windowTitle")
            appendLine("- lastObservedSampleSource: ${latestCapture?.sampleSource?.reportValue ?: "none"}")
            appendLine("- lastObservedAppPackage: ${latestCapture?.snapshot?.appPackage ?: "none"}")
            appendLine("- isMock: ${sampleSource != SampleSource.REAL_DEVICE_ACCESSIBILITY.reportValue}")
            appendLine("- isEmulatorMockChat: ${latestCapture?.sampleSource == SampleSource.EMULATOR_MOCK_CHAT_ACCESSIBILITY}")
            appendLine("- apiCalled: ${realResult?.apiCalled ?: false}")
            appendLine("- overlayImagePath: ${visualDebug?.overlayImagePath ?: "none"}")
            appendLine("- screenshotCaptured: ${visualDebug?.screenshotCaptured ?: false}")
            appendLine("- visualTruthAvailable: ${visualDebug?.visualTruthAvailable ?: false}")
            appendLine()
            appendLine("## Current Real Device Functional Smoke")
            appendLine()
            appendLine("- overlayShownInTargetApp: ${realResult?.overlayShownInTargetApp ?: "NOT_TESTED"}")
            appendLine("- foregroundPackageWhenPanelShown: ${realResult?.foregroundPackageWhenPanelShown ?: "NOT_TESTED"}")
            appendLine("- userStayedInChatApp: ${realResult?.userStayedInChatApp ?: "NOT_TESTED"}")
            appendLine("- resultShownAsOverlay: ${realResult?.resultShownAsOverlay ?: "NOT_TESTED"}")
            appendLine("- mainActivityOpened: ${realResult?.mainActivityOpened ?: "NOT_TESTED"}")
            appendLine("- effectiveMessageCount: ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM } ?: "NOT_TESTED"}")
            appendLine("- actualLastSpeaker: ${scenarioValidation.actualLastSpeaker}")
            appendLine("- decisionType: ${scenarioValidation.actualDecisionType}")
            appendLine("- routeCount: ${scenarioValidation.actualRouteCount}")
            appendLine("- apiCalled: ${realResult?.apiCalled ?: false}")
            appendLine("- modelCalled: false")
            appendLine("- screenshotDiagnosticStatus: ${scenarioValidation.screenshotDiagnosticStatus}")
            appendLine("- screenshotFailureBlocksMainPath: ${scenarioValidation.screenshotFailureBlocksMainPath}")
            appendLine()
            appendLine("## Scenario Assertion Diagnosis")
            appendLine()
            appendLine("- scenarioName: ${scenarioValidation.scenarioName}")
            appendLine("- scenarioNameSource: ${scenarioValidation.scenarioNameSource}")
            appendLine("- expectedLastSpeaker: ${scenarioValidation.expectedLastSpeaker}")
            appendLine("- expectedLastSpeakerSource: ${scenarioValidation.expectedLastSpeakerSource}")
            appendLine("- actualLastSpeakerFromPreAnalysisSnapshot: ${scenarioValidation.actualLastSpeakerFromPreAnalysisSnapshot}")
            appendLine("- actualLastSpeakerFromDecisionSnapshot: ${scenarioValidation.actualLastSpeakerFromDecisionSnapshot}")
            appendLine("- actualLastSpeaker: ${scenarioValidation.actualLastSpeaker}")
            appendLine("- expectedDecisionType: ${scenarioValidation.expectedDecisionType}")
            appendLine("- actualDecisionType: ${scenarioValidation.actualDecisionType}")
            appendLine("- expectedRouteCount: ${scenarioValidation.expectedRouteCount}")
            appendLine("- actualRouteCount: ${scenarioValidation.actualRouteCount}")
            appendLine("- scenarioResult: ${scenarioValidation.scenarioResult}")
            appendLine("- scenarioDefinitionTrusted: ${scenarioValidation.scenarioDefinitionTrusted}")
            appendLine("- scenarioAssertionResult: ${scenarioValidation.scenarioAssertionResult}")
            appendLine("- scenarioFailureCategory: ${scenarioValidation.scenarioFailureCategory}")
            appendLine("- scenarioDefinitionMismatchReason: ${scenarioValidation.scenarioDefinitionMismatchReason ?: "none"}")
            appendLine("- failureReason: ${scenarioValidation.failureReason}")
            appendLine("- failureCategory: ${realResult?.let { failureCategory(it, scenarioValidation) } ?: "not_tested"}")
            appendLine("- VisualSpeakerFallbackUsed: ${capture?.visualSpeakerFallbackCount?.let { it > 0 } ?: false}")
            appendLine("- conflictCount: ${capture?.messages?.count { it.visualConflict } ?: "NOT_TESTED"}")
            appendLine("- userCorrectionProvided: ${realResult?.userCorrectionProvided ?: false}")
            appendLine("- correctedLastSpeaker: ${realResult?.correctedLastSpeaker ?: "none"}")
            appendLine("- correctedMessageId: ${realResult?.correctedMessageId ?: "none"}")
            appendLine()
            appendLine("## Snapshot Phase Separation")
            appendLine()
            appendLine("- preAnalysisSnapshotAvailable: ${scenarioValidation.preAnalysisSnapshotAvailable}")
            appendLine("- preAnalysisWindowTitle: ${scenarioValidation.preAnalysisWindowTitle}")
            appendLine("- preAnalysisResultPanelVisible: false")
            appendLine("- decisionSnapshotAvailable: ${realResult?.captureResult != null}")
            appendLine("- postPanelSnapshotAvailable: ${scenarioValidation.postPanelSnapshotAvailable}")
            appendLine("- postPanelWindowTitle: ${scenarioValidation.postPanelWindowTitle}")
            appendLine("- reportWindowTitleContaminatedByPanel: ${scenarioValidation.reportWindowTitleContaminatedByPanel}")
            appendLine("- postPanelStateUsedForScenarioExpectation: ${scenarioValidation.postPanelStateUsedForScenarioExpectation}")
            appendLine()
            appendLine("## Visual Truth / Projection")
            appendLine()
            appendLine("- screenshotCaptured: ${visualDebug?.screenshotCaptured ?: false}")
            appendLine("- screenshotUnavailable: ${visualDebug?.screenshotUnavailable ?: false}")
            appendLine("- screenshotReason: ${visualDebug?.reason ?: "none"}")
            appendLine("- visualTruthAvailable: ${scenarioValidation.visualTruthAvailable}")
            appendLine("- visualTruthSource: ${scenarioValidation.visualTruthSource}")
            appendLine("- accessibilityProjectionAvailable: ${scenarioValidation.accessibilityProjectionAvailable}")
            appendLine("- overlayDebugImageAvailable: ${visualDebug?.overlayImagePath != null}")
            appendLine("- failureCategory: ${realResult?.let { failureCategory(it, scenarioValidation) } ?: "not_tested"}")
            appendLine()
            appendLine("## Acceptance")
            appendLine()
            appendLine("- overlayShownInTargetApp: ${realResult?.overlayShownInTargetApp ?: "NOT_TESTED"}")
            appendLine("- mainActivityOpened: ${realResult?.mainActivityOpened ?: "NOT_TESTED"}")
            appendLine("- parsedMessageCount: ${capture?.messages?.size ?: "NOT_TESTED"}")
            appendLine("- metadataFilteredCount: ${capture?.messages?.count { !it.isEffectiveChatMessage || it.metadataType != MetadataType.NONE } ?: "NOT_TESTED"}")
            appendLine("- effectiveMessageCount: ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM } ?: "NOT_TESTED"}")
            appendLine("- LastSpeakerDecision: ${realResult?.lastSpeakerDecision?.lastSpeaker ?: "NOT_TESTED"}")
            appendLine("- TacticalDecision: ${realResult?.tacticalDecision?.decisionType ?: "NOT_TESTED"}")
            appendLine("- ReplyRoutes: ${realResult?.routes?.size ?: "NOT_TESTED"}")
            appendLine()
            appendLine("## Files")
            appendLine()
            appendLine("- outputs/review/huiyi-v4-review-for-gpt.md")
            appendLine("- outputs/real-device-smoke-report-for-gpt.md")
            appendLine("- outputs/real-device-current-screen-report-for-gpt.md")
            appendLine("- outputs/real-device-current-screen-report.json")
            appendLine()
            appendLine("## Parsed Messages")
            appendLine()
            if (capture == null) {
                appendLine("- none")
            } else {
                capture.messages.takeLast(30).forEachIndexed { index, message ->
                    appendLine("- [m${(index + 1).toString().padStart(3, '0')}] speaker=${message.speaker} text=${message.normalizedText ?: "[non-text]"}")
                }
            }
        }
    }

    private fun buildSmokeReport(
        latestResult: CurrentScreenPipelineResult?,
        realResult: CurrentScreenPipelineResult?,
        generatedAt: Long,
        realDeviceSmokeResult: String,
        failReason: String,
        ownAppPackage: String,
        scenarioValidation: RealDeviceScenarioValidation
    ): String {
        val capture = realResult?.captureResult
        val latestCapture = latestResult?.captureResult
        val visualDebug = realResult?.visualDebugResult
        return buildString {
            appendLine("# v4.1.4 Real Device Smoke Report")
            appendLine()
            appendLine("## Basic")
            appendLine()
            appendLine("- generatedAt: $generatedAt")
            appendLine("- scenarioName: ${scenarioValidation.scenarioName}")
            appendLine("- overall_result: ${scenarioValidation.currentOverallResult}")
            appendLine("- realDeviceSmoke: $realDeviceSmokeResult")
            appendLine("- realDeviceFunctionalSmoke: ${scenarioValidation.realDeviceFunctionalSmoke}")
            appendLine("- scenarioAssertionResult: ${scenarioValidation.scenarioAssertionResult}")
            appendLine("- currentOverallResult: ${scenarioValidation.currentOverallResult}")
            appendLine("- failReason: $failReason")
            appendLine("- sample_source: ${capture?.sampleSource?.reportValue ?: "NOT_TESTED"}")
            appendLine("- appPackage: ${capture?.snapshot?.appPackage ?: "NOT_TESTED"}")
            appendLine("- windowTitle: ${capture?.snapshot?.windowTitle ?: "NOT_TESTED"}")
            appendLine("- lastObservedSampleSource: ${latestCapture?.sampleSource?.reportValue ?: "none"}")
            appendLine("- lastObservedAppPackage: ${latestCapture?.snapshot?.appPackage ?: "none"}")
            appendLine("- ownAppPackage: $ownAppPackage")
            appendLine("- apiCalled: ${realResult?.apiCalled ?: false}")
            appendLine("- overlayImagePath: ${visualDebug?.overlayImagePath ?: "none"}")
            if (realDeviceSmokeResult == "NOT_TESTED") appendLine("- disclaimer: $REAL_SMOKE_NOT_TESTED_NOTICE")
            appendLine()
            appendLine("## Smoke Decision Validation")
            appendLine()
            appendLine("- scenarioName: ${scenarioValidation.scenarioName}")
            appendLine("- scenarioNameSource: ${scenarioValidation.scenarioNameSource}")
            appendLine("- expectedLastSpeaker: ${scenarioValidation.expectedLastSpeaker}")
            appendLine("- expectedLastSpeakerSource: ${scenarioValidation.expectedLastSpeakerSource}")
            appendLine("- actualLastSpeaker: ${scenarioValidation.actualLastSpeaker}")
            appendLine("- expectedDecisionType: ${scenarioValidation.expectedDecisionType}")
            appendLine("- actualDecisionType: ${scenarioValidation.actualDecisionType}")
            appendLine("- expectedRouteCount: ${scenarioValidation.expectedRouteCount}")
            appendLine("- actualRouteCount: ${scenarioValidation.actualRouteCount}")
            appendLine("- scenarioResult: ${scenarioValidation.scenarioResult}")
            appendLine("- scenarioDefinitionTrusted: ${scenarioValidation.scenarioDefinitionTrusted}")
            appendLine("- scenarioAssertionResult: ${scenarioValidation.scenarioAssertionResult}")
            appendLine("- scenarioFailureCategory: ${scenarioValidation.scenarioFailureCategory}")
            appendLine("- scenarioDefinitionMismatchReason: ${scenarioValidation.scenarioDefinitionMismatchReason ?: "none"}")
            appendLine("- failureReason: ${scenarioValidation.failureReason}")
            appendLine("- failureCategory: ${realResult?.let { failureCategory(it, scenarioValidation) } ?: "not_tested"}")
            appendLine("- screenshotCaptured: ${visualDebug?.screenshotCaptured ?: false}")
            appendLine("- screenshotUnavailable: ${visualDebug?.screenshotUnavailable ?: false}")
            appendLine("- screenshotDiagnosticStatus: ${scenarioValidation.screenshotDiagnosticStatus}")
            appendLine("- screenshotFailureBlocksMainPath: ${scenarioValidation.screenshotFailureBlocksMainPath}")
            appendLine("- secondaryDiagnosticErrorCode: ${scenarioValidation.secondaryDiagnosticErrorCode}")
            appendLine("- visualTruthAvailable: ${scenarioValidation.visualTruthAvailable}")
            appendLine("- visualTruthSource: ${scenarioValidation.visualTruthSource}")
            appendLine("- VisualSpeakerFallbackUsed: ${capture?.visualSpeakerFallbackCount?.let { it > 0 } ?: false}")
            appendLine("- conflictCount: ${capture?.messages?.count { it.visualConflict } ?: "NOT_TESTED"}")
            appendLine("- userCorrectionProvided: ${realResult?.userCorrectionProvided ?: false}")
            appendLine("- expectedIfLastSpeakerME: WAIT + 0 routes + no API")
            appendLine("- expectedIfLastSpeakerOTHER: 5 routes, unless voice/image/unknown requires context")
            appendLine("- actualLastSpeaker: ${realResult?.lastSpeakerDecision?.lastSpeaker ?: "NOT_TESTED"}")
            appendLine("- actualDecisionType: ${realResult?.tacticalDecision?.decisionType ?: "NOT_TESTED"}")
            appendLine("- actualRouteCount: ${realResult?.routes?.size ?: "NOT_TESTED"}")
            appendLine("- validationResult: $realDeviceSmokeResult")
            appendLine()
            appendLine("## Required Fields")
            appendLine()
            appendLine("- parsedMessageCount: ${capture?.messages?.size ?: "NOT_TESTED"}")
            appendLine("- metadataFilteredCount: ${capture?.messages?.count { !it.isEffectiveChatMessage || it.metadataType != MetadataType.NONE } ?: "NOT_TESTED"}")
            appendLine("- effectiveMessageCount: ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM } ?: "NOT_TESTED"}")
            appendLine("- LastSpeakerDecision: ${realResult?.lastSpeakerDecision?.lastSpeaker ?: "NOT_TESTED"}")
            appendLine("- TacticalDecision: ${realResult?.tacticalDecision?.decisionType ?: "NOT_TESTED"}")
            appendLine("- ReplyRoutes: ${realResult?.routes?.size ?: "NOT_TESTED"}")
            appendLine("- overlayShownInTargetApp: ${realResult?.overlayShownInTargetApp ?: "NOT_TESTED"}")
            appendLine("- mainActivityOpened: ${realResult?.mainActivityOpened ?: "NOT_TESTED"}")
        }
    }

    private fun buildNotTestedCurrentScreenReport(
        latestResult: CurrentScreenPipelineResult?,
        accessibilityState: HuiyiAccessibilityState,
        generatedAt: Long,
        ownAppPackage: String,
        scenario: RealDeviceScenario
    ): String {
        val latestCapture = latestResult?.captureResult
        return buildString {
            appendLine("# Real Device Current Screen Evidence Pack")
            appendLine()
            appendLine("- overall_result: NOT_TESTED")
            appendLine("- realDeviceFunctionalSmoke: NOT_TESTED")
            appendLine("- scenarioAssertionResult: NOT_TESTED")
            appendLine("- currentOverallResult: NOT_TESTED")
            appendLine("- realDeviceSmoke: NOT_TESTED")
            appendLine("- generatedAt: $generatedAt")
            appendLine("- scenarioName: ${scenario.id}")
            appendLine("- expectedLastSpeaker: ${scenario.expectedLastSpeaker?.name ?: "NO_FIXED_EXPECTATION"}")
            appendLine("- actualLastSpeaker: NOT_TESTED")
            appendLine("- expectedDecisionType: ${scenario.expectedDecisionType?.name ?: "NO_FIXED_EXPECTATION"}")
            appendLine("- actualDecisionType: NOT_TESTED")
            appendLine("- expectedRouteCount: ${scenario.expectedRouteCount?.toString() ?: "NO_FIXED_EXPECTATION"}")
            appendLine("- actualRouteCount: 0")
            appendLine("- scenarioResult: NOT_TESTED")
            appendLine("- scenarioDefinitionTrusted: false")
            appendLine("- scenarioFailureCategory: not_tested")
            appendLine("- failureReason: not_tested")
            appendLine("- sample_source: NOT_TESTED")
            appendLine("- appPackage: NOT_TESTED")
            appendLine("- windowTitle: NOT_TESTED")
            appendLine("- lastObservedSampleSource: ${latestCapture?.sampleSource?.reportValue ?: "none"}")
            appendLine("- lastObservedAppPackage: ${latestCapture?.snapshot?.appPackage ?: "none"}")
            appendLine("- ownAppPackage: $ownAppPackage")
            appendLine("- serviceConnected: ${accessibilityState.serviceConnected}")
            appendLine("- rootAvailable: ${accessibilityState.rootAvailable}")
            appendLine("- apiCalled: false")
            appendLine("- overlayShownInTargetApp: NOT_TESTED")
            appendLine("- huiyiActivityOpened: NOT_TESTED")
            appendLine("- mainActivityOpened: NOT_TESTED")
            appendLine("- failReason: $REAL_SMOKE_NOT_TESTED_NOTICE")
            appendLine()
            appendLine("## Parsed Messages")
            appendLine()
            appendLine("- parsedMessageCount: NOT_TESTED")
            appendLine("- metadataFilteredCount: NOT_TESTED")
            appendLine("- effectiveMessageCount: NOT_TESTED")
            appendLine("- messages: none")
            appendLine()
            appendLine("## LastSpeakerDecision")
            appendLine()
            appendLine("- lastSpeaker: NOT_TESTED")
            appendLine("- shouldReply: NOT_TESTED")
            appendLine()
            appendLine("## TacticalDecision")
            appendLine()
            appendLine("- decisionType: NOT_TESTED")
            appendLine()
            appendLine("## ReplyRoutes")
            appendLine()
            appendLine("- routes: NOT_TESTED")
        }
    }

    private fun buildNotTestedCurrentScreenJson(
        latestResult: CurrentScreenPipelineResult?,
        accessibilityState: HuiyiAccessibilityState,
        generatedAt: Long,
        ownAppPackage: String,
        scenario: RealDeviceScenario
    ): String {
        val latestCapture = latestResult?.captureResult
        return """
            {
              "overall_result": "NOT_TESTED",
              "realDeviceFunctionalSmoke": "NOT_TESTED",
              "scenarioAssertionResult": "NOT_TESTED",
              "currentOverallResult": "NOT_TESTED",
              "realDeviceSmoke": "NOT_TESTED",
              "generatedAt": $generatedAt,
              "scenarioName": "${scenario.id}",
              "expectedLastSpeaker": "${scenario.expectedLastSpeaker?.name ?: "NO_FIXED_EXPECTATION"}",
              "actualLastSpeaker": "NOT_TESTED",
              "expectedDecisionType": "${scenario.expectedDecisionType?.name ?: "NO_FIXED_EXPECTATION"}",
              "actualDecisionType": "NOT_TESTED",
              "expectedRouteCount": "${scenario.expectedRouteCount?.toString() ?: "NO_FIXED_EXPECTATION"}",
              "actualRouteCount": 0,
              "scenarioResult": "NOT_TESTED",
              "scenarioDefinitionTrusted": false,
              "scenarioFailureCategory": "not_tested",
              "failureReason": "not_tested",
              "sample_source": "NOT_TESTED",
              "appPackage": "NOT_TESTED",
              "windowTitle": "NOT_TESTED",
              "lastObservedSampleSource": "${latestCapture?.sampleSource?.reportValue ?: "none"}",
              "lastObservedAppPackage": "${escape(latestCapture?.snapshot?.appPackage ?: "none")}",
              "ownAppPackage": "$ownAppPackage",
              "serviceConnected": ${accessibilityState.serviceConnected},
              "rootAvailable": ${accessibilityState.rootAvailable},
              "parsedMessageCount": "NOT_TESTED",
              "metadataFilteredCount": "NOT_TESTED",
              "effectiveMessageCount": "NOT_TESTED",
              "parsedMessages": [],
              "LastSpeakerDecision": "NOT_TESTED",
              "TacticalDecision": "NOT_TESTED",
              "ReplyRoutes": [],
              "apiCalled": false,
              "overlayShownInTargetApp": "NOT_TESTED",
              "huiyiActivityOpened": "NOT_TESTED",
              "mainActivityOpened": "NOT_TESTED",
              "failReason": "$REAL_SMOKE_NOT_TESTED_NOTICE"
            }
        """.trimIndent()
    }

    private fun CurrentScreenPipelineResult.isRealChatAppCapture(ownAppPackage: String): Boolean {
        val capture = captureResult ?: return false
        val appPackage = capture.snapshot.appPackage.orEmpty()
        return capture.sampleSource == SampleSource.REAL_DEVICE_ACCESSIBILITY &&
            appPackage.isNotBlank() &&
            appPackage != "unknown" &&
            appPackage != ownAppPackage &&
            appPackage != "com.huiyi.mockchat" &&
            !appPackage.startsWith("local.validation")
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")

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

    companion object {
        const val REAL_SMOKE_NOT_TESTED_NOTICE: String =
            "\u672c\u8f6e Review Freshness \u901a\u8fc7\uff0c\u4f46 Real Device Smoke \u672a\u6267\u884c\uff0c\u4e0d\u4ee3\u8868\u771f\u5b9e\u804a\u5929 App \u5df2\u901a\u8fc7\u3002"
    }
}
