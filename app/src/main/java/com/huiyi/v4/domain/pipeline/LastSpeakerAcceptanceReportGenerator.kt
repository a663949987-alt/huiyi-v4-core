package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.AccessibilityRuntimeState
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.VisualBounds
import java.security.MessageDigest

enum class RealDeviceTestIntent(
    val reportValue: String,
    val assertedSpeaker: Speaker?
) {
    USER_ASSERTED_LAST_ME("USER_ASSERTED_LAST_ME", Speaker.ME),
    USER_ASSERTED_LAST_OTHER("USER_ASSERTED_LAST_OTHER", Speaker.OTHER),
    AUTO_FROM_SCREEN("AUTO_FROM_SCREEN", null)
}

data class LastSpeakerAcceptanceReport(
    val markdown: String,
    val json: String,
    val result: String,
    val currentOverallResult: String,
    val failureCategory: String,
    val failureReason: String
)

class LastSpeakerAcceptanceReportGenerator {
    fun build(
        result: CurrentScreenPipelineResult?,
        trace: NextSentenceSessionTrace?,
        accessibilityState: AccessibilityRuntimeState,
        scenario: RealDeviceScenario,
        testIntent: RealDeviceTestIntent,
        generatedAt: Long,
        versionName: String,
        versionCode: Int
    ): LastSpeakerAcceptanceReport {
        val summary = summarize(result, trace, scenario, testIntent)
        val markdown = buildMarkdown(summary, result, trace, accessibilityState, generatedAt, versionName, versionCode)
        val json = buildJson(summary, result, trace, accessibilityState, generatedAt, versionName, versionCode)
        return LastSpeakerAcceptanceReport(
            markdown = markdown,
            json = json,
            result = summary.result,
            currentOverallResult = summary.currentOverallResult,
            failureCategory = summary.failureCategory,
            failureReason = summary.failureReason
        )
    }

    private fun summarize(
        result: CurrentScreenPipelineResult?,
        trace: NextSentenceSessionTrace?,
        scenario: RealDeviceScenario,
        testIntent: RealDeviceTestIntent
    ): Summary {
        if (result == null) {
            val stuck = trace?.errorCode == NextSentenceErrorCode.LAST_ME_ANALYSIS_STUCK ||
                trace?.errorCode == NextSentenceErrorCode.SESSION_TIMEOUT_NO_TERMINAL_STATE
            return Summary(
                result = if (stuck) "STUCK_ANALYZING" else "NOT_TESTED",
                currentOverallResult = if (stuck) "CONTROLLED_FAIL_WITH_LAST_ME_STUCK_EVIDENCE" else "NOT_TESTED",
                failureCategory = if (stuck) "last_me_stuck_analyzing" else "not_tested",
                failureReason = if (stuck) "analysis_timeout_without_terminal_state" else "NOT_GENERATED_ON_PHONE",
                expectedSpeaker = testIntent.assertedSpeaker?.name ?: scenario.expectedLastSpeaker?.name ?: "AUTO",
                actualSpeaker = "NOT_TESTED",
                decisionType = "NOT_TESTED",
                routeCount = 0,
                waitPanelShown = false,
                routePanelShown = false,
                chosenCaptureSource = "NONE",
                chosenCaptureReason = "NO_PIPELINE_RESULT",
                currentRootLastSpeaker = "NOT_CAPTURED",
                fallbackSnapshotLastSpeaker = "NOT_CAPTURED",
                staleRoutesReused = false,
                panelContentFromCurrentSession = false,
                sessionId = trace?.sessionId.orEmpty(),
                sessionTerminalState = if (stuck) "TIMEOUT" else "NO_RESULT",
                analysisStartedAt = trace?.startedAt ?: 0L,
                analysisEndedAt = trace?.endedAt ?: 0L,
                analysisDurationMs = ((trace?.endedAt ?: 0L) - trace?.startedAt.orZero()).coerceAtLeast(0L),
                loadingStillVisibleAfterTimeout = stuck,
                lastObservedStageBeforeTimeout = trace?.stage?.name ?: "NONE",
                timeoutErrorCode = trace?.errorCode?.name ?: "NONE"
            )
        }

        val actualSpeaker = result.lastSpeakerDecision.lastSpeaker
        val decisionType = result.tacticalDecision.decisionType
        val routeCount = result.routes.size
        val waitPanelShown = result.waitPanelShown ||
            (decisionType == TacticalDecisionType.WAIT && routeCount == 0)
        val routePanelShown = result.routePanelShown || routeCount > 0
        val expectedSpeaker = testIntent.assertedSpeaker ?: scenario.expectedLastSpeaker
        val assertedMismatch = expectedSpeaker != null && actualSpeaker != null && actualSpeaker != expectedSpeaker
        val lastMeRuleViolation = expectedSpeaker == Speaker.ME &&
            actualSpeaker == Speaker.ME &&
            (decisionType != TacticalDecisionType.WAIT || routeCount != 0 || result.apiCalled || routePanelShown || !waitPanelShown)
        val waitRuleViolation = decisionType == TacticalDecisionType.WAIT && (routeCount != 0 || routePanelShown)
        val lastOtherPass = expectedSpeaker == Speaker.OTHER &&
            actualSpeaker == Speaker.OTHER &&
            !waitPanelShown &&
            !result.mainActivityOpened &&
            result.overlayShownInTargetApp &&
            (
                (decisionType == TacticalDecisionType.NORMAL_REPLY && routeCount == 5 && routePanelShown) ||
                    (decisionType == TacticalDecisionType.EMPATHY_FIRST && routeCount == 5 && routePanelShown) ||
                    (decisionType == TacticalDecisionType.CONTEXT_REQUIRED && routeCount == 0)
                )
        val lastMePass = expectedSpeaker == Speaker.ME &&
            actualSpeaker == Speaker.ME &&
            decisionType == TacticalDecisionType.WAIT &&
            routeCount == 0 &&
            !result.apiCalled &&
            waitPanelShown &&
            !routePanelShown

        val resultValue = when {
            assertedMismatch && expectedSpeaker == Speaker.ME -> "USER_ASSERTION_MISMATCH"
            assertedMismatch -> "FAIL"
            lastMeRuleViolation || waitRuleViolation -> "FAIL"
            expectedSpeaker == Speaker.ME && lastMePass -> "PASS"
            expectedSpeaker == Speaker.OTHER && lastOtherPass -> "PASS"
            expectedSpeaker == Speaker.OTHER -> "FAIL"
            else -> "NOT_TESTED"
        }
        val failureCategory = when {
            resultValue == "PASS" -> "none"
            resultValue == "USER_ASSERTION_MISMATCH" -> "user_assertion_vs_accessibility_mismatch"
            lastMeRuleViolation || waitRuleViolation -> "last_me_decision_rule_violation"
            expectedSpeaker == Speaker.OTHER -> "last_other_regression"
            else -> "not_tested"
        }
        val failureReason = when {
            resultValue == "PASS" -> "none"
            resultValue == "USER_ASSERTION_MISMATCH" -> "actual_${actualSpeaker?.name ?: "UNKNOWN"}_while_user_asserted_${expectedSpeaker?.name}"
            lastMeRuleViolation -> "last_me_should_wait_with_zero_routes"
            waitRuleViolation -> "wait_decision_has_route_panel_or_routes"
            expectedSpeaker == Speaker.OTHER && !result.overlayShownInTargetApp -> "overlay_not_in_target_app"
            expectedSpeaker == Speaker.OTHER && result.mainActivityOpened -> "main_activity_opened"
            expectedSpeaker == Speaker.OTHER -> "last_other_expected_routes_or_context"
            else -> "NOT_GENERATED_ON_PHONE"
        }
        val overall = when (resultValue) {
            "PASS" -> "PASS"
            "USER_ASSERTION_MISMATCH" -> "CONTROLLED_FAIL_WITH_LAST_ME_EVIDENCE"
            "NOT_TESTED" -> "NOT_TESTED"
            else -> "FAIL"
        }
        val capture = result.captureResult
        val captureSource = capture?.captureSource ?: NextSentenceCaptureSource.NONE
        val sourceSpeaker = actualSpeaker?.name ?: "UNKNOWN"
        return Summary(
            result = resultValue,
            currentOverallResult = overall,
            failureCategory = failureCategory,
            failureReason = failureReason,
            expectedSpeaker = expectedSpeaker?.name ?: "AUTO",
            actualSpeaker = sourceSpeaker,
            decisionType = decisionType.name,
            routeCount = routeCount,
            waitPanelShown = waitPanelShown,
            routePanelShown = routePanelShown,
            chosenCaptureSource = captureSource.name,
            chosenCaptureReason = if (capture?.usedFallbackSnapshot == true) "LAST_STABLE_CHAT_SNAPSHOT_USED" else "CURRENT_ROOT_PREFERRED",
            currentRootLastSpeaker = if (captureSource == NextSentenceCaptureSource.CURRENT_ROOT) sourceSpeaker else "NOT_CAPTURED",
            fallbackSnapshotLastSpeaker = if (captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT) sourceSpeaker else "NOT_CAPTURED",
            staleRoutesReused = result.staleRoutesReused,
            panelContentFromCurrentSession = result.panelContentFromCurrentSession,
            sessionId = result.sessionId ?: trace?.sessionId.orEmpty(),
            previousSessionId = result.previousSessionId.orEmpty(),
            panelSessionId = result.panelSessionId.orEmpty(),
            sessionTerminalState = result.sessionTerminalState,
            analysisStartedAt = result.analysisStartedAt,
            analysisEndedAt = result.analysisEndedAt,
            analysisDurationMs = result.analysisDurationMs,
            loadingStillVisibleAfterTimeout = result.loadingStillVisibleAfterTimeout,
            lastObservedStageBeforeTimeout = result.lastObservedStageBeforeTimeout,
            timeoutErrorCode = result.timeoutErrorCode,
            waitDecisionReached = result.waitDecisionReached,
            waitPanelRenderAttempted = result.waitPanelRenderAttempted,
            waitPanelRenderSuccess = result.waitPanelRenderSuccess,
            decisionTypeFamily = result.decisionTypeFamily
        )
    }

    private fun buildMarkdown(
        summary: Summary,
        result: CurrentScreenPipelineResult?,
        trace: NextSentenceSessionTrace?,
        accessibilityState: AccessibilityRuntimeState,
        generatedAt: Long,
        versionName: String,
        versionCode: Int
    ): String {
        val capture = result?.captureResult
        val last = result?.lastSpeakerDecision?.lastEffectiveMessage
        val previous = result?.captureResult?.messages.orEmpty()
            .filter { it.isEffectiveChatMessage && it.speaker != Speaker.SYSTEM }
            .takeLast(5)
            .joinToString("\n") { "- ${it.id}: [${it.speaker.name}] ${textOf(it).redactPrivateText(80)}" }
            .ifBlank { "- none" }
        val currentRootCapturedAt = if (capture?.captureSource == NextSentenceCaptureSource.CURRENT_ROOT) capture.snapshot.capturedAt else 0L
        val fallbackCapturedAt = if (capture?.captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT) capture.snapshot.capturedAt else 0L
        return buildString {
            appendLine("# Last ${summary.expectedSpeaker} Real Device Report")
            appendLine()
            appendLine("- versionName: $versionName")
            appendLine("- versionCode: $versionCode")
            appendLine("- taskName: last_me_stuck_analyzing_and_phone_bundle_real_reports_fix")
            appendLine("- generatedAt: $generatedAt")
            appendLine("- currentOverallResult: ${summary.currentOverallResult}")
            appendLine("- scenarioResult: ${summary.result}")
            appendLine("- failureCategory: ${summary.failureCategory}")
            appendLine("- failureReason: ${summary.failureReason}")
            appendLine()
            appendLine("## User intent")
            appendLine("- testIntent: ${if (summary.expectedSpeaker == "ME") RealDeviceTestIntent.USER_ASSERTED_LAST_ME.reportValue else if (summary.expectedSpeaker == "OTHER") RealDeviceTestIntent.USER_ASSERTED_LAST_OTHER.reportValue else RealDeviceTestIntent.AUTO_FROM_SCREEN.reportValue}")
            appendLine("- userAssertedLastSpeaker: ${summary.expectedSpeaker}")
            appendLine()
            appendLine("## Capture source")
            appendLine("- chosenCaptureSource: ${summary.chosenCaptureSource}")
            appendLine("- chosenCaptureReason: ${summary.chosenCaptureReason}")
            appendLine("- currentRootCapturedAt: $currentRootCapturedAt")
            appendLine("- fallbackSnapshotCapturedAt: $fallbackCapturedAt")
            appendLine("- fallbackSnapshotAgeMs: ${capture?.lastStableSnapshotAgeMs ?: "null"}")
            appendLine("- currentRootLastSpeaker: ${summary.currentRootLastSpeaker}")
            appendLine("- fallbackSnapshotLastSpeaker: ${summary.fallbackSnapshotLastSpeaker}")
            appendLine("- currentRootNodesHash: ${nodesHash(capture, NextSentenceCaptureSource.CURRENT_ROOT)}")
            appendLine("- fallbackSnapshotNodesHash: ${nodesHash(capture, NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT)}")
            appendLine()
            appendLine("## Post-send settle")
            appendLine("- attempted: false")
            appendLine("- reason: NONE")
            appendLine("- retryCount: 0")
            appendLine("- lastSpeakerBeforeSettle: ${summary.actualSpeaker}")
            appendLine("- lastSpeakerAfterSettle: ${summary.actualSpeaker}")
            appendLine()
            appendLine("## Last effective message")
            appendLine("- id: ${last?.id ?: "none"}")
            appendLine("- text redacted: ${last?.let { textOf(it).redactPrivateText(120) } ?: "none"}")
            appendLine("- speaker: ${summary.actualSpeaker}")
            appendLine("- bounds: ${bounds(last?.bubbleBounds ?: last?.bounds)}")
            appendLine("- previous 5 effective messages redacted:")
            appendLine(previous)
            appendLine()
            appendLine("## Decision")
            appendLine("- actualLastSpeaker: ${summary.actualSpeaker}")
            appendLine("- decisionType: ${summary.decisionType}")
            appendLine("- routeCount: ${summary.routeCount}")
            appendLine("- apiCalled: ${result?.apiCalled ?: false}")
            appendLine("- modelCalled: false")
            appendLine("- waitPanelShown: ${summary.waitPanelShown}")
            appendLine("- routePanelShown: ${summary.routePanelShown}")
            appendLine()
            appendLine("## Panel session")
            appendLine("- sessionId: ${summary.sessionId}")
            appendLine("- previousSessionId: ${summary.previousSessionId}")
            appendLine("- panelSessionId: ${summary.panelSessionId}")
            appendLine("- panelContentFromCurrentSession: ${summary.panelContentFromCurrentSession}")
            appendLine("- staleRoutesClearedAtSessionStart: ${result?.staleRoutesClearedAtSessionStart ?: true}")
            appendLine("- staleRoutesReused: ${summary.staleRoutesReused}")
            appendLine("- sessionTerminalState: ${summary.sessionTerminalState}")
            appendLine("- analysisStartedAt: ${summary.analysisStartedAt}")
            appendLine("- analysisEndedAt: ${summary.analysisEndedAt}")
            appendLine("- analysisDurationMs: ${summary.analysisDurationMs}")
            appendLine("- loadingStillVisibleAfterTimeout: ${summary.loadingStillVisibleAfterTimeout}")
            appendLine("- lastObservedStageBeforeTimeout: ${summary.lastObservedStageBeforeTimeout}")
            appendLine("- timeoutErrorCode: ${summary.timeoutErrorCode}")
            appendLine("- waitDecisionReached: ${summary.waitDecisionReached}")
            appendLine("- waitPanelRenderAttempted: ${summary.waitPanelRenderAttempted}")
            appendLine("- waitPanelRenderSuccess: ${summary.waitPanelRenderSuccess}")
            appendLine("- decisionTypeFamily: ${summary.decisionTypeFamily}")
            appendLine()
            appendLine("## Runtime")
            appendLine("- serviceConnected: ${accessibilityState.serviceConnected}")
            appendLine("- permissionMissingMessageShown: ${trace?.permissionMissingMessageShown ?: false}")
            appendLine("- mainActivityOpened: ${result?.mainActivityOpened ?: false}")
            appendLine("- overlayShownInTargetApp: ${result?.overlayShownInTargetApp ?: false}")
            appendLine("- resultShownAsOverlay: ${result?.resultShownAsOverlay ?: false}")
            appendLine()
            appendLine("## Result")
            appendLine("- lastMeResult: ${if (summary.expectedSpeaker == "ME") summary.result else "NOT_APPLICABLE"}")
            appendLine("- lastOtherRealDeviceResult: ${if (summary.expectedSpeaker == "OTHER") summary.result else "NOT_APPLICABLE"}")
            appendLine("- currentOverallResult: ${summary.currentOverallResult}")
            appendLine("- failureCategory: ${summary.failureCategory}")
            appendLine("- failureReason: ${summary.failureReason}")
        }
    }

    private fun buildJson(
        summary: Summary,
        result: CurrentScreenPipelineResult?,
        trace: NextSentenceSessionTrace?,
        accessibilityState: AccessibilityRuntimeState,
        generatedAt: Long,
        versionName: String,
        versionCode: Int
    ): String {
        val capture = result?.captureResult
        val last = result?.lastSpeakerDecision?.lastEffectiveMessage
        val lastMeValue = if (summary.expectedSpeaker == "ME") summary.result else "NOT_APPLICABLE"
        val lastOtherValue = if (summary.expectedSpeaker == "OTHER") summary.result else "NOT_APPLICABLE"
        val testIntent = if (summary.expectedSpeaker == "ME") {
            RealDeviceTestIntent.USER_ASSERTED_LAST_ME.reportValue
        } else if (summary.expectedSpeaker == "OTHER") {
            RealDeviceTestIntent.USER_ASSERTED_LAST_OTHER.reportValue
        } else {
            RealDeviceTestIntent.AUTO_FROM_SCREEN.reportValue
        }
        return """
            {
              "versionName": "${escape(versionName)}",
              "versionCode": $versionCode,
              "taskName": "last_me_stuck_analyzing_and_phone_bundle_real_reports_fix",
              "generatedAt": $generatedAt,
              "testIntent": "$testIntent",
              "currentOverallResult": "${summary.currentOverallResult}",
              "lastMeResult": "$lastMeValue",
              "lastOtherRealDeviceResult": "$lastOtherValue",
              "userAssertedLastSpeaker": "${summary.expectedSpeaker}",
              "actualLastSpeaker": "${summary.actualSpeaker}",
              "actualLastSpeakerFromCurrentRoot": "${summary.currentRootLastSpeaker}",
              "actualLastSpeakerFromFallbackSnapshot": "${summary.fallbackSnapshotLastSpeaker}",
              "chosenCaptureSource": "${summary.chosenCaptureSource}",
              "chosenCaptureReason": "${summary.chosenCaptureReason}",
              "currentRootCapturedAt": ${if (capture?.captureSource == NextSentenceCaptureSource.CURRENT_ROOT) capture.snapshot.capturedAt else 0},
              "lastStableSnapshotCapturedAt": ${if (capture?.captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT) capture.snapshot.capturedAt else 0},
              "fallbackSnapshotAgeMs": ${capture?.lastStableSnapshotAgeMs ?: "null"},
              "currentRootAgeMs": null,
              "currentRootNodesHash": "${nodesHash(capture, NextSentenceCaptureSource.CURRENT_ROOT)}",
              "fallbackSnapshotNodesHash": "${nodesHash(capture, NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT)}",
              "currentRootMessageCount": ${if (capture?.captureSource == NextSentenceCaptureSource.CURRENT_ROOT) capture.messages.size else 0},
              "fallbackSnapshotMessageCount": ${if (capture?.captureSource == NextSentenceCaptureSource.LAST_STABLE_CHAT_SNAPSHOT) capture.messages.size else 0},
              "postSendSettleAttempted": false,
              "postSendSettleReason": "NONE",
              "postSendSettleRetryCount": 0,
              "lastSpeakerBeforeSettle": "${summary.actualSpeaker}",
              "lastSpeakerAfterSettle": "${summary.actualSpeaker}",
              "lastEffectiveMessageId": "${escape(last?.id.orEmpty())}",
              "lastEffectiveMessageTextRedacted": "${escape(last?.let { textOf(it).redactPrivateText(120) }.orEmpty())}",
              "lastEffectiveMessageBounds": "${escape(bounds(last?.bubbleBounds ?: last?.bounds))}",
              "lastEffectiveSpeaker": "${summary.actualSpeaker}",
              "decisionType": "${summary.decisionType}",
              "apiCalled": ${result?.apiCalled ?: false},
              "modelCalled": false,
              "routeCount": ${summary.routeCount},
              "waitPanelShown": ${summary.waitPanelShown},
              "routePanelShown": ${summary.routePanelShown},
              "sessionId": "${escape(summary.sessionId)}",
              "previousSessionId": "${escape(summary.previousSessionId)}",
              "panelSessionId": "${escape(summary.panelSessionId)}",
              "panelContentFromCurrentSession": ${summary.panelContentFromCurrentSession},
              "staleRoutesClearedAtSessionStart": ${result?.staleRoutesClearedAtSessionStart ?: true},
              "staleRoutesReused": ${summary.staleRoutesReused},
              "sessionTerminalState": "${summary.sessionTerminalState}",
              "analysisStartedAt": ${summary.analysisStartedAt},
              "analysisEndedAt": ${summary.analysisEndedAt},
              "analysisDurationMs": ${summary.analysisDurationMs},
              "loadingStillVisibleAfterTimeout": ${summary.loadingStillVisibleAfterTimeout},
              "lastObservedStageBeforeTimeout": "${summary.lastObservedStageBeforeTimeout}",
              "timeoutErrorCode": "${summary.timeoutErrorCode}",
              "waitDecisionReached": ${summary.waitDecisionReached},
              "waitPanelRenderAttempted": ${summary.waitPanelRenderAttempted},
              "waitPanelRenderSuccess": ${summary.waitPanelRenderSuccess},
              "decisionTypeFamily": "${summary.decisionTypeFamily}",
              "permissionMissingMessageShown": ${trace?.permissionMissingMessageShown ?: false},
              "mainActivityOpened": ${result?.mainActivityOpened ?: false},
              "overlayShownInTargetApp": ${result?.overlayShownInTargetApp ?: false},
              "resultShownAsOverlay": ${result?.resultShownAsOverlay ?: false},
              "serviceConnected": ${accessibilityState.serviceConnected},
              "failureCategory": "${summary.failureCategory}",
              "failureReason": "${summary.failureReason}"
            }
        """.trimIndent()
    }

    private data class Summary(
        val result: String,
        val currentOverallResult: String,
        val failureCategory: String,
        val failureReason: String,
        val expectedSpeaker: String,
        val actualSpeaker: String,
        val decisionType: String,
        val routeCount: Int,
        val waitPanelShown: Boolean,
        val routePanelShown: Boolean,
        val chosenCaptureSource: String,
        val chosenCaptureReason: String,
        val currentRootLastSpeaker: String,
        val fallbackSnapshotLastSpeaker: String,
        val staleRoutesReused: Boolean,
        val panelContentFromCurrentSession: Boolean,
        val sessionId: String = "",
        val previousSessionId: String = "",
        val panelSessionId: String = "",
        val sessionTerminalState: String = "UNKNOWN",
        val analysisStartedAt: Long = 0L,
        val analysisEndedAt: Long = 0L,
        val analysisDurationMs: Long = 0L,
        val loadingStillVisibleAfterTimeout: Boolean = false,
        val lastObservedStageBeforeTimeout: String = "NONE",
        val timeoutErrorCode: String = "NONE",
        val waitDecisionReached: Boolean = false,
        val waitPanelRenderAttempted: Boolean = false,
        val waitPanelRenderSuccess: Boolean = false,
        val decisionTypeFamily: String = "UNKNOWN"
    )

    private fun Long?.orZero(): Long = this ?: 0L

    private fun textOf(message: MessageNode): String = when (val content = message.content) {
        is MessageContent.Text -> content.text
        is MessageContent.Voice -> "[voice:${content.transcriptStatus}]"
        is MessageContent.Image -> "[image:${content.descriptionStatus}]"
        is MessageContent.Video -> "[video:${content.descriptionStatus}]"
        is MessageContent.Sticker -> "[sticker:${content.meaningStatus}]"
    }

    private fun bounds(bounds: VisualBounds?): String = bounds?.let {
        "${it.left},${it.top},${it.right},${it.bottom}"
    } ?: "none"

    private fun nodesHash(capture: CurrentScreenCaptureResult?, source: NextSentenceCaptureSource): String {
        if (capture == null || capture.captureSource != source) return ""
        val raw = capture.snapshot.nodes.joinToString("|") { "${it.id}:${it.readableText}:${bounds(it.bounds)}" }
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(16)
    }

    private fun escape(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")
}
