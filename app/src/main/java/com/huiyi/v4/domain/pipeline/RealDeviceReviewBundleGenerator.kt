package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.accessibility.HuiyiAccessibilityState

data class RealDeviceReviewBundleContent(
    val reviewMarkdown: String,
    val smokeMarkdown: String,
    val currentScreenMarkdown: String,
    val currentScreenJson: String,
    val realDeviceSmokeResult: String,
    val overallResult: String,
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
        ownAppPackage: String
    ): RealDeviceReviewBundleContent {
        val realResult = latestResult?.takeIf { it.isRealChatAppCapture(ownAppPackage) }
        val currentScreenMarkdown = if (realResult == null) {
            buildNotTestedCurrentScreenReport(latestResult, accessibilityState, generatedAt, ownAppPackage)
        } else {
            evidenceGenerator.buildMarkdown(realResult, accessibilityState, generatedAt)
        }
        val currentScreenJson = if (realResult == null) {
            buildNotTestedCurrentScreenJson(latestResult, accessibilityState, generatedAt, ownAppPackage)
        } else {
            evidenceGenerator.buildJson(realResult, accessibilityState, generatedAt)
        }

        val realDeviceSmokeResult = realResult?.let { evidenceGenerator.overallResult(it) } ?: "NOT_TESTED"
        val overallResult = when (realDeviceSmokeResult) {
            "PASS" -> "PASS"
            "FAIL" -> "FAIL"
            else -> "PARTIAL"
        }
        val failReason = when (realDeviceSmokeResult) {
            "PASS" -> "none"
            "FAIL" -> "Real Device Smoke failed. See real-device-current-screen-report-for-gpt.md."
            else -> REAL_SMOKE_NOT_TESTED_NOTICE
        }
        val smokeMarkdown = buildSmokeReport(
            latestResult = latestResult,
            realResult = realResult,
            generatedAt = generatedAt,
            realDeviceSmokeResult = realDeviceSmokeResult,
            failReason = failReason,
            ownAppPackage = ownAppPackage
        )
        val reviewMarkdown = buildReviewMarkdown(
            latestResult = latestResult,
            realResult = realResult,
            generatedAt = generatedAt,
            versionName = versionName,
            versionCode = versionCode,
            realDeviceSmokeResult = realDeviceSmokeResult,
            overallResult = overallResult,
            failReason = failReason
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
        failReason: String
    ): String {
        val capture = realResult?.captureResult
        val latestCapture = latestResult?.captureResult
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
            appendLine("- taskName: Export Real Device Acceptance Bundle")
            appendLine("- review_freshness_result: PASS")
            appendLine("- mockchat_result: NOT_INCLUDED_IN_PHONE_EXPORT")
            appendLine("- real_device_smoke_result: $realDeviceSmokeResult")
            appendLine("- overall_result: $overallResult")
            appendLine("- failReason: $failReason")
            appendLine("- realDeviceSmoke: $realDeviceSmokeResult")
            if (realDeviceSmokeResult == "NOT_TESTED") appendLine("- smokeDisclaimer: $REAL_SMOKE_NOT_TESTED_NOTICE")
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
            appendLine()
            appendLine("## Acceptance")
            appendLine()
            appendLine("- overlayShownInTargetApp: ${realResult?.overlayShownInTargetApp ?: "NOT_TESTED"}")
            appendLine("- mainActivityOpened: ${realResult?.mainActivityOpened ?: "NOT_TESTED"}")
            appendLine("- parsedMessageCount: ${capture?.messages?.size ?: "NOT_TESTED"}")
            appendLine("- metadataFilteredCount: ${capture?.messages?.count { !it.isEffectiveChatMessage || it.metadataType != com.huiyi.v4.domain.model.MetadataType.NONE } ?: "NOT_TESTED"}")
            appendLine("- effectiveMessageCount: ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != com.huiyi.v4.domain.model.Speaker.SYSTEM } ?: "NOT_TESTED"}")
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
        ownAppPackage: String
    ): String {
        val capture = realResult?.captureResult
        val latestCapture = latestResult?.captureResult
        return buildString {
            appendLine("# v4.1.4 Real Device Smoke Report")
            appendLine()
            appendLine("## Basic")
            appendLine()
            appendLine("- generatedAt: $generatedAt")
            appendLine("- overall_result: $realDeviceSmokeResult")
            appendLine("- realDeviceSmoke: $realDeviceSmokeResult")
            appendLine("- failReason: $failReason")
            appendLine("- sample_source: ${capture?.sampleSource?.reportValue ?: "NOT_TESTED"}")
            appendLine("- appPackage: ${capture?.snapshot?.appPackage ?: "NOT_TESTED"}")
            appendLine("- windowTitle: ${capture?.snapshot?.windowTitle ?: "NOT_TESTED"}")
            appendLine("- lastObservedSampleSource: ${latestCapture?.sampleSource?.reportValue ?: "none"}")
            appendLine("- lastObservedAppPackage: ${latestCapture?.snapshot?.appPackage ?: "none"}")
            appendLine("- ownAppPackage: $ownAppPackage")
            appendLine("- apiCalled: ${realResult?.apiCalled ?: false}")
            if (realDeviceSmokeResult == "NOT_TESTED") appendLine("- disclaimer: $REAL_SMOKE_NOT_TESTED_NOTICE")
            appendLine()
            appendLine("## Required Fields")
            appendLine()
            appendLine("- parsedMessageCount: ${capture?.messages?.size ?: "NOT_TESTED"}")
            appendLine("- metadataFilteredCount: ${capture?.messages?.count { !it.isEffectiveChatMessage || it.metadataType != com.huiyi.v4.domain.model.MetadataType.NONE } ?: "NOT_TESTED"}")
            appendLine("- effectiveMessageCount: ${capture?.messages?.count { it.isEffectiveChatMessage && it.speaker != com.huiyi.v4.domain.model.Speaker.SYSTEM } ?: "NOT_TESTED"}")
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
        ownAppPackage: String
    ): String {
        val latestCapture = latestResult?.captureResult
        return buildString {
            appendLine("# Real Device Current Screen Evidence Pack")
            appendLine()
            appendLine("- overall_result: NOT_TESTED")
            appendLine("- realDeviceSmoke: NOT_TESTED")
            appendLine("- generatedAt: $generatedAt")
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
        ownAppPackage: String
    ): String {
        val latestCapture = latestResult?.captureResult
        return """
            {
              "overall_result": "NOT_TESTED",
              "realDeviceSmoke": "NOT_TESTED",
              "generatedAt": $generatedAt,
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

    companion object {
        const val REAL_SMOKE_NOT_TESTED_NOTICE: String =
            "\u672c\u8f6e Review Freshness \u901a\u8fc7\uff0c\u4f46 Real Device Smoke \u672a\u6267\u884c\uff0c\u4e0d\u4ee3\u8868\u771f\u5b9e\u804a\u5929 App \u5df2\u901a\u8fc7\u3002"
    }
}
