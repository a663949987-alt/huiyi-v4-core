package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.BuildConfig

class NextSentenceFailureReportGenerator {
    fun buildMarkdown(trace: NextSentenceSessionTrace): String {
        return buildString {
            appendLine("# Latest Next Sentence Failure")
            appendLine()
            appendLine("## User-visible result")
            appendLine("- shown message: ${trace.userFacingMessage ?: "none"}")
            appendLine("- bubble still visible: ${trace.bubbleVisibleAfterFailure}")
            appendLine("- permission warning shown: ${trace.permissionMissingMessageShown}")
            appendLine()
            appendLine("## Failure stage")
            appendLine("- stage: ${trace.failedStage ?: trace.stage}")
            appendLine("- errorCode: ${trace.errorCode ?: NextSentenceErrorCode.NONE}")
            appendLine("- secondaryErrorCode: ${trace.secondaryErrorCode ?: "none"}")
            appendLine("- pipelineExceptionClass: ${trace.pipelineExceptionClass ?: trace.exceptionClass ?: "none"}")
            appendLine("- pipelineExceptionMessageRedacted: ${trace.pipelineExceptionMessageRedacted ?: trace.exceptionMessageRedacted ?: "none"}")
            appendLine("- likely cause: ${likelyCause(trace)}")
            appendLine()
            appendLine("## Window / root")
            appendLine("- before click package: ${trace.activePackageBeforeClick ?: "unknown"}")
            appendLine("- capture package: ${trace.activePackageAtCaptureStart ?: "unknown"}")
            appendLine("- root package: ${trace.rootPackageName ?: "unknown"}")
            appendLine("- root package at capture start: ${trace.rootPackageAtCaptureStart ?: trace.activePackageAtCaptureStart ?: "unknown"}")
            appendLine("- root package before failure UI: ${trace.rootPackageBeforeFailureUi ?: "unknown"}")
            appendLine("- root package after failure UI: ${trace.rootPackageAfterFailureUi ?: "unknown"}")
            appendLine("- root title: ${trace.rootWindowTitle ?: "unknown"}")
            appendLine("- root retry count: ${trace.rootRetryCount}")
            appendLine("- root available after retry: ${trace.rootAvailableAfterRetry}")
            appendLine("- root is own overlay: ${trace.rootIsOwnOverlay}")
            appendLine("- root is system ui: ${trace.rootIsSystemUi}")
            appendLine()
            appendLine("## Capture")
            appendLine("- primaryCapturePath: ${trace.primaryCapturePath}")
            appendLine("- nodeTreeAttempted: ${trace.nodeTreeAttempted}")
            appendLine("- nodeTreeSuccess: ${trace.nodeTreeSuccess}")
            appendLine("- fallbackSnapshotAttempted: ${trace.fallbackSnapshotAttempted}")
            appendLine("- fallbackSnapshotSuccess: ${trace.fallbackSnapshotSuccess}")
            appendLine("- screenshotAttempted: ${trace.screenshotAttempted}")
            appendLine("- screenshotSuccess: ${trace.screenshotSuccess}")
            appendLine("- screenshotAvailable: ${trace.screenshotAvailable}")
            appendLine("- screenshotCapabilityDeclared: ${trace.screenshotCapabilityDeclared}")
            appendLine("- screenshotErrorCode: ${trace.screenshotErrorCode ?: "none"}")
            appendLine("- screenshotExceptionClass: ${trace.screenshotExceptionClass ?: "none"}")
            appendLine("- screenshotExceptionMessageRedacted: ${trace.screenshotExceptionMessageRedacted ?: "none"}")
            appendLine("- captureSource: ${trace.captureSource}")
            appendLine("- used fallback snapshot: ${trace.usedFallbackSnapshot}")
            appendLine("- last stable snapshot age: ${trace.lastStableSnapshotAgeMs ?: "none"}")
            appendLine("- last stable snapshot package: ${trace.lastStableSnapshotPackage ?: "none"}")
            appendLine("- raw node count: ${trace.rawNodeCount}")
            appendLine("- visible text count: ${trace.visibleTextCount}")
            appendLine("- parsed message count: ${trace.parsedMessageCount}")
            appendLine("- effective message count: ${trace.effectiveMessageCount}")
            appendLine()
            appendLine("## Decision")
            appendLine("- last effective speaker: ${trace.lastEffectiveSpeaker ?: "none"}")
            appendLine("- decision type: ${trace.decisionType ?: "none"}")
            appendLine("- route count: ${trace.routeCount}")
            appendLine("- api called: ${trace.apiCalled}")
            appendLine()
            appendLine("## Overlay")
            appendLine("- bubble attached after click: ${trace.bubbleAttachedAfterClick}")
            appendLine("- bubble visible after failure: ${trace.bubbleVisibleAfterFailure}")
            appendLine("- panel attached: ${trace.panelAttached}")
            appendLine("- panel render success: ${trace.panelRenderSuccess}")
            appendLine()
            appendLine("## Recommended next fix")
            appendLine("- ${recommendation(trace)}")
        }
    }

    fun buildJson(trace: NextSentenceSessionTrace): String {
        val fields = linkedMapOf<String, Any?>(
            "versionName" to BuildConfig.VERSION_NAME,
            "versionCode" to BuildConfig.VERSION_CODE,
            "scenarioName" to "next_sentence_analysis_failure",
            "userVisibleMessage" to trace.userFacingMessage,
            "errorCode" to trace.errorCode?.name,
            "failedStage" to trace.failedStage?.name,
            "sessionId" to trace.sessionId,
            "primaryCapturePath" to trace.primaryCapturePath,
            "nodeTreeAttempted" to trace.nodeTreeAttempted,
            "nodeTreeSuccess" to trace.nodeTreeSuccess,
            "screenshotAttempted" to trace.screenshotAttempted,
            "screenshotSuccess" to trace.screenshotSuccess,
            "screenshotAvailable" to trace.screenshotAvailable,
            "screenshotCapabilityDeclared" to trace.screenshotCapabilityDeclared,
            "screenshotErrorCode" to trace.screenshotErrorCode?.name,
            "screenshotExceptionClass" to trace.screenshotExceptionClass,
            "screenshotExceptionMessageRedacted" to trace.screenshotExceptionMessageRedacted,
            "pipelineExceptionClass" to trace.pipelineExceptionClass,
            "pipelineExceptionMessageRedacted" to trace.pipelineExceptionMessageRedacted,
            "secondaryErrorCode" to trace.secondaryErrorCode?.name,
            "fallbackSnapshotAttempted" to trace.fallbackSnapshotAttempted,
            "fallbackSnapshotSuccess" to trace.fallbackSnapshotSuccess,
            "preFailureCurrentPackage" to trace.rootPackageBeforeFailureUi,
            "postFailureCurrentPackage" to (trace.currentPackageAfterFailurePanel ?: trace.rootPackageAfterFailureUi),
            "failurePanelAlreadyShownWhenSampled" to trace.failurePanelAlreadyShownWhenSampled,
            "captureSource" to trace.captureSource.name,
            "usedFallbackSnapshot" to trace.usedFallbackSnapshot,
            "lastStableSnapshotAgeMs" to trace.lastStableSnapshotAgeMs,
            "lastStableSnapshotPackage" to trace.lastStableSnapshotPackage,
            "activePackageBeforeClick" to trace.activePackageBeforeClick,
            "activePackageAtCaptureStart" to trace.activePackageAtCaptureStart,
            "activePackageAfterRootRetry" to trace.activePackageAfterRootRetry,
            "rootPackageAtCaptureStart" to trace.rootPackageAtCaptureStart,
            "rootPackageBeforeFailureUi" to trace.rootPackageBeforeFailureUi,
            "rootPackageAfterFailureUi" to trace.rootPackageAfterFailureUi,
            "currentPackageAfterFailurePanel" to trace.currentPackageAfterFailurePanel,
            "rootPackageName" to trace.rootPackageName,
            "rootClassName" to trace.rootClassName,
            "rootWindowTitle" to trace.rootWindowTitle,
            "rootIsOwnOverlay" to trace.rootIsOwnOverlay,
            "rootIsSystemUi" to trace.rootIsSystemUi,
            "rootIsTargetChatApp" to trace.rootIsTargetChatApp,
            "rootAvailableFirstTry" to trace.rootAvailableFirstTry,
            "rootRetryCount" to trace.rootRetryCount,
            "rootAvailableAfterRetry" to trace.rootAvailableAfterRetry,
            "rawNodeCount" to trace.rawNodeCount,
            "visibleTextCount" to trace.visibleTextCount,
            "parsedMessageCount" to trace.parsedMessageCount,
            "effectiveMessageCount" to trace.effectiveMessageCount,
            "lastEffectiveSpeaker" to trace.lastEffectiveSpeaker?.name,
            "decisionType" to trace.decisionType,
            "routeCount" to trace.routeCount,
            "apiCalled" to trace.apiCalled,
            "panelAttached" to trace.panelAttached,
            "panelRenderSuccess" to trace.panelRenderSuccess,
            "bubbleVisibleAfterFailure" to trace.bubbleVisibleAfterFailure,
            "bubbleAttachedAfterClick" to trace.bubbleAttachedAfterClick,
            "permissionMissingMessageShown" to trace.permissionMissingMessageShown,
            "exceptionClass" to trace.exceptionClass,
            "exceptionMessageRedacted" to trace.exceptionMessageRedacted
        )
        return fields.entries.joinToString(prefix = "{\n", postfix = "\n}", separator = ",\n") { (key, value) ->
            "  \"${escape(key)}\": ${jsonValue(value)}"
        }
    }

    private fun likelyCause(trace: NextSentenceSessionTrace): String {
        return when (trace.errorCode) {
            NextSentenceErrorCode.ROOT_IS_OWN_OVERLAY -> "点击悬浮窗后 active root 暂时变成会意自己的 overlay。"
            NextSentenceErrorCode.ROOT_UNAVAILABLE -> "系统暂未提供可读 active root，但无障碍权限不一定有问题。"
            NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY -> "无障碍节点存在，但当前 parser 没抽出聊天气泡。"
            NextSentenceErrorCode.ACCESSIBILITY_SERVICE_NOT_CONNECTED -> "系统已开启无障碍，但服务实例尚未连接或被系统回收。"
            NextSentenceErrorCode.API_DISABLED -> "当前本地调试包未调用真实模型。"
            NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING -> "无障碍截图 capability 缺失；截图已降级为可选诊断，主链路应继续节点树。"
            NextSentenceErrorCode.SCREENSHOT_NOT_ALLOWED,
            NextSentenceErrorCode.SCREENSHOT_SECURE_WINDOW,
            NextSentenceErrorCode.SCREENSHOT_RATE_LIMITED,
            NextSentenceErrorCode.SCREENSHOT_FAILED -> "截图诊断失败；不应中断节点树主链路。"
            NextSentenceErrorCode.UNKNOWN_EXCEPTION -> "异常未被已知分类覆盖，需要看 exceptionClass。"
            else -> trace.errorCode?.name ?: "none"
        }
    }

    private fun recommendation(trace: NextSentenceSessionTrace): String {
        return when (trace.errorCode) {
            NextSentenceErrorCode.ROOT_IS_OWN_OVERLAY,
            NextSentenceErrorCode.ROOT_UNAVAILABLE,
            NextSentenceErrorCode.ROOT_IS_SYSTEM_UI -> "优先确认 lastStableChatSnapshot 是否及时生成并被使用。"
            NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY -> "查看 parser-empty-diagnostics，补具体 App 的 row/bubble bounds 规则。"
            NextSentenceErrorCode.LAST_SPEAKER_UNKNOWN -> "查看 visual order 和 bounds，校准左右侧/头像/气泡规则。"
            NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING,
            NextSentenceErrorCode.SCREENSHOT_NOT_ALLOWED,
            NextSentenceErrorCode.SCREENSHOT_SECURE_WINDOW,
            NextSentenceErrorCode.SCREENSHOT_RATE_LIMITED,
            NextSentenceErrorCode.SCREENSHOT_FAILED -> "确认节点树 capture 是否成功；截图只保留为 optional diagnostic。"
            else -> "按 errorCode 对应阶段继续收窄。"
        }
    }

    private fun jsonValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is Boolean -> value.toString()
            is Number -> value.toString()
            else -> "\"${escape(value.toString())}\""
        }
    }

    private fun escape(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }
}
