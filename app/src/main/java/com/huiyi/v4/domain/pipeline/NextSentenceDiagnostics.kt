package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.domain.model.Speaker

enum class NextSentenceErrorCode {
    NONE,
    ACCESSIBILITY_SYSTEM_DISABLED,
    ACCESSIBILITY_SERVICE_NOT_CONNECTED,
    ROOT_UNAVAILABLE,
    ROOT_IS_OWN_OVERLAY,
    ROOT_IS_SYSTEM_UI,
    PACKAGE_TITLE_MISMATCH,
    FAILURE_PANEL_STATE_SAMPLED,
    CAPTURE_STATE_CONTAMINATED_BY_ERROR_UI,
    CHAT_WINDOW_NOT_FOUND,
    CHAT_WINDOW_STALE,
    NODE_TREE_EMPTY,
    RAW_TEXT_EMPTY,
    CHAT_MESSAGE_PARSE_EMPTY,
    ONLY_NON_CHAT_TEXT_FOUND,
    MESSAGE_ORDER_UNCERTAIN,
    LAST_SPEAKER_UNKNOWN,
    LAST_SPEAKER_IS_ME_SHOULD_WAIT,
    CONTEXT_REQUIRED,
    DECISION_EMPTY,
    ROUTE_SCHEMA_INVALID,
    ROUTE_COUNT_INVALID,
    API_DISABLED,
    API_KEY_MISSING,
    MODEL_REQUEST_FAILED,
    MODEL_RESPONSE_EMPTY,
    MODEL_RESPONSE_PARSE_FAILED,
    SCREENSHOT_CAPABILITY_MISSING,
    SCREENSHOT_NOT_ALLOWED,
    SCREENSHOT_SECURE_WINDOW,
    SCREENSHOT_RATE_LIMITED,
    SCREENSHOT_FAILED,
    PANEL_ADD_FAILED,
    PANEL_RENDER_FAILED,
    LAST_STABLE_SNAPSHOT_STALE_AFTER_USER_SEND,
    CURRENT_ROOT_NEWER_THAN_FALLBACK,
    FALLBACK_SNAPSHOT_CONFLICTS_WITH_CURRENT_ROOT,
    RECENT_USER_SEND_NEEDS_SETTLE,
    PANEL_SESSION_MISMATCH,
    STALE_ROUTES_REUSED,
    WAIT_DECISION_BUT_ROUTES_NOT_EMPTY,
    WAIT_DECISION_BUT_ROUTE_PANEL_SHOWN,
    LAST_ME_ANALYSIS_STUCK,
    LAST_ME_WAIT_PANEL_TIMEOUT,
    WAIT_DECISION_PANEL_RENDER_FAILED,
    WAIT_DECISION_BUT_STILL_ANALYZING,
    SESSION_TIMEOUT_NO_TERMINAL_STATE,
    ROUTE_DECISION_TIMEOUT,
    PANEL_RENDER_TIMEOUT,
    PHONE_LAST_ME_REPORT_NOT_GENERATED,
    LAST_ME_RULE_VIOLATION,
    PRE_ANALYSIS_SNAPSHOT_CONTAMINATED_BY_PANEL,
    REPORT_WINDOW_TITLE_CONTAMINATED_BY_PANEL,
    SESSION_CANCELLED,
    SESSION_TIMEOUT,
    NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT,
    UNKNOWN_EXCEPTION
}

enum class NextSentenceStage {
    IDLE,
    CLICK_RECEIVED,
    CLICK_ACK_SHOWN,
    ACCESSIBILITY_STATE_CHECKING,
    ACCESSIBILITY_STATE_CHECKED,
    CAPTURE_STARTING,
    CAPTURED,
    PARSING,
    DECIDING,
    CLOUD_STARTING,
    NODE_TREE_CAPTURE_STARTED,
    NODE_TREE_CAPTURE_RETRYING,
    NODE_TREE_CAPTURED,
    ROOT_CAPTURE_STARTED,
    ROOT_CAPTURE_RETRYING,
    ROOT_CAPTURED,
    FALLBACK_SNAPSHOT_CHECKED,
    FALLBACK_SNAPSHOT_USED,
    OPTIONAL_SCREENSHOT_DIAGNOSTIC_STARTED,
    OPTIONAL_SCREENSHOT_DIAGNOSTIC_FAILED,
    OPTIONAL_SCREENSHOT_DIAGNOSTIC_SUCCEEDED,
    NODE_TREE_EXTRACTED,
    CHAT_MESSAGES_PARSED,
    LAST_SPEAKER_DECIDED,
    ROUTES_GENERATED,
    PANEL_RENDERING,
    PANEL_RENDERED,
    CONTROLLED_FAIL,
    TIMEOUT,
    FAILED
}

enum class NextSentenceCaptureSource {
    CURRENT_ROOT,
    LAST_STABLE_CHAT_SNAPSHOT,
    NONE
}

data class NextSentenceSessionTrace(
    val sessionId: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val trigger: String = "floating_bubble_next_sentence",
    val stage: NextSentenceStage = NextSentenceStage.IDLE,
    val failedStage: NextSentenceStage? = null,
    val errorCode: NextSentenceErrorCode? = null,
    val clickReceivedAt: Long? = null,
    val clickAckShownAt: Long? = null,
    val clickAckLatencyMs: Long? = null,
    val clickAckVisible: Boolean = false,
    val runNextSentenceEntered: Boolean = false,
    val sessionCreated: Boolean = false,
    val terminalState: String? = null,
    val panelShown: Boolean = false,
    val panelVisibleBeforeClick: Boolean = false,
    val panelVisibleAfterClick: Boolean = false,
    val activeWindowTitleAtClick: String? = null,
    val rootAvailableAtClick: Boolean = false,
    val exceptionClass: String? = null,
    val exceptionMessageRedacted: String? = null,
    val bubbleVisibleBeforeClick: Boolean = false,
    val bubbleVisibleAfterClick: Boolean = false,
    val bubbleAttachedAfterClick: Boolean = false,
    val bubbleVisibleAfterFailure: Boolean = false,
    val systemAccessibilityEnabled: Boolean = false,
    val serviceConnected: Boolean = false,
    val serviceReconnectAttempted: Boolean = false,
    val serviceReconnectWaitMs: Long = 0L,
    val serviceReconnectSucceeded: Boolean = false,
    val accessibilityRuntimeCategory: String? = null,
    val permissionMissingMessageShown: Boolean = false,
    val activePackageBeforeClick: String? = null,
    val activePackageAtCaptureStart: String? = null,
    val activePackageAfterRootRetry: String? = null,
    val rootPackageAtCaptureStart: String? = null,
    val rootPackageBeforeFailureUi: String? = null,
    val rootPackageAfterFailureUi: String? = null,
    val currentPackageAfterFailurePanel: String? = null,
    val rootAvailableFirstTry: Boolean = false,
    val rootRetryCount: Int = 0,
    val rootAvailableAfterRetry: Boolean = false,
    val rootPackageName: String? = null,
    val rootClassName: String? = null,
    val rootWindowTitle: String? = null,
    val rootIsOwnOverlay: Boolean = false,
    val rootIsSystemUi: Boolean = false,
    val rootIsTargetChatApp: Boolean = false,
    val captureSource: NextSentenceCaptureSource = NextSentenceCaptureSource.NONE,
    val primaryCapturePath: String = "NONE",
    val nodeTreeAttempted: Boolean = false,
    val nodeTreeSuccess: Boolean = false,
    val screenshotAttempted: Boolean = false,
    val screenshotSuccess: Boolean = false,
    val screenshotAvailable: Boolean = false,
    val screenshotCapabilityDeclared: Boolean = false,
    val screenshotErrorCode: NextSentenceErrorCode? = null,
    val screenshotExceptionClass: String? = null,
    val screenshotExceptionMessageRedacted: String? = null,
    val pipelineExceptionClass: String? = null,
    val pipelineExceptionMessageRedacted: String? = null,
    val secondaryErrorCode: NextSentenceErrorCode? = null,
    val fallbackSnapshotAttempted: Boolean = false,
    val fallbackSnapshotSuccess: Boolean = false,
    val failurePanelAlreadyShownWhenSampled: Boolean = false,
    val usedFallbackSnapshot: Boolean = false,
    val lastStableSnapshotAgeMs: Long? = null,
    val lastStableSnapshotPackage: String? = null,
    val rawNodeCount: Int = 0,
    val visibleTextCount: Int = 0,
    val parsedMessageCount: Int = 0,
    val effectiveMessageCount: Int = 0,
    val lastEffectiveSpeaker: Speaker? = null,
    val decisionType: String? = null,
    val routeCount: Int = 0,
    val routeTypesCsv: String = "",
    val expressSelfEligibilityEligible: Boolean? = null,
    val expressSelfEligibilityMode: String = "NONE",
    val expressSelfEligibilityBlockReason: String? = null,
    val apiCalled: Boolean = false,
    val panelAttached: Boolean = false,
    val panelRenderSuccess: Boolean = false,
    val userFacingMessage: String? = null
) {
    fun failed(
        code: NextSentenceErrorCode,
        stage: NextSentenceStage,
        error: Throwable? = null,
        now: Long = System.currentTimeMillis()
    ): NextSentenceSessionTrace = copy(
        endedAt = now,
        stage = NextSentenceStage.FAILED,
        failedStage = stage,
        errorCode = code,
        exceptionClass = error?.let { it::class.java.name },
        exceptionMessageRedacted = error?.message?.redactPrivateText(),
        userFacingMessage = userFacingMessageFor(code)
    )
}

class NextSentenceException(
    val code: NextSentenceErrorCode,
    val failedStage: NextSentenceStage,
    val trace: NextSentenceSessionTrace,
    cause: Throwable? = null
) : RuntimeException(userFacingMessageFor(code), cause)

fun userFacingMessageFor(code: NextSentenceErrorCode): String {
    return when (code) {
        NextSentenceErrorCode.ACCESSIBILITY_SYSTEM_DISABLED -> "需要先开启会意无障碍服务。"
        NextSentenceErrorCode.ACCESSIBILITY_SERVICE_NOT_CONNECTED -> "系统开关是开的，但会意服务还没真正连上。请回到聊天页等 2 秒再点；如果一直这样，把会意无障碍关掉再打开一次。"
        NextSentenceErrorCode.ROOT_UNAVAILABLE -> "当前窗口暂时不可读取，请回到聊天页再试一次。"
        NextSentenceErrorCode.ROOT_IS_OWN_OVERLAY -> "刚才焦点停在会意悬浮窗，已尝试使用聊天页快照。"
        NextSentenceErrorCode.ROOT_IS_SYSTEM_UI -> "当前焦点在系统界面，请回到聊天页再试一次。"
        NextSentenceErrorCode.PACKAGE_TITLE_MISMATCH -> "当前窗口来源不一致，已尝试使用聊天页快照。"
        NextSentenceErrorCode.FAILURE_PANEL_STATE_SAMPLED -> "采样到了失败面板状态，已保留诊断。"
        NextSentenceErrorCode.CAPTURE_STATE_CONTAMINATED_BY_ERROR_UI -> "当前采样被错误面板污染，请回到聊天页再试一次。"
        NextSentenceErrorCode.CHAT_WINDOW_NOT_FOUND -> "没识别到聊天窗口，请先打开聊天页面。"
        NextSentenceErrorCode.CHAT_WINDOW_STALE -> "聊天页快照已过期，请回到聊天页再试一次。"
        NextSentenceErrorCode.NODE_TREE_EMPTY -> "当前窗口节点为空，请回到聊天页再试一次。"
        NextSentenceErrorCode.RAW_TEXT_EMPTY -> "当前窗口没有可读文字，请回到聊天页再试一次。"
        NextSentenceErrorCode.CHAT_MESSAGE_PARSE_EMPTY -> "看到了当前窗口，但没识别出聊天气泡。"
        NextSentenceErrorCode.ONLY_NON_CHAT_TEXT_FOUND -> "看到了窗口文字，但不像聊天内容，请回到聊天页再试一次。"
        NextSentenceErrorCode.MESSAGE_ORDER_UNCERTAIN -> "看到了聊天内容，但消息顺序不确定，已保存诊断。"
        NextSentenceErrorCode.LAST_SPEAKER_UNKNOWN -> "看到了聊天内容，但暂时无法判断最后一句是谁发的。"
        NextSentenceErrorCode.LAST_SPEAKER_IS_ME_SHOULD_WAIT -> "你已经回过了，先等对方。"
        NextSentenceErrorCode.CONTEXT_REQUIRED -> "当前屏幕信息不足，需要更多上下文。"
        NextSentenceErrorCode.API_DISABLED -> "当前是本地调试包，模型接口未启用。"
        NextSentenceErrorCode.API_KEY_MISSING -> "模型配置缺失，无法生成路线。"
        NextSentenceErrorCode.MODEL_REQUEST_FAILED -> "模型请求失败，已保留当前屏幕诊断。"
        NextSentenceErrorCode.MODEL_RESPONSE_EMPTY -> "模型返回为空，已保留诊断。"
        NextSentenceErrorCode.MODEL_RESPONSE_PARSE_FAILED -> "模型返回格式解析失败，已保留诊断。"
        NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING -> "当前版本没有截图能力，已改用无障碍节点读取聊天内容。"
        NextSentenceErrorCode.SCREENSHOT_NOT_ALLOWED -> "系统不允许当前服务截图，已改用无障碍节点读取聊天内容。"
        NextSentenceErrorCode.SCREENSHOT_SECURE_WINDOW -> "当前窗口禁止截图，已改用无障碍节点读取聊天内容。"
        NextSentenceErrorCode.SCREENSHOT_RATE_LIMITED -> "截图调用过快，已改用无障碍节点读取聊天内容。"
        NextSentenceErrorCode.SCREENSHOT_FAILED -> "截图失败，已改用无障碍节点读取聊天内容。"
        NextSentenceErrorCode.ROUTE_SCHEMA_INVALID -> "路线结果格式异常，已保留诊断。"
        NextSentenceErrorCode.ROUTE_COUNT_INVALID -> "路线数量异常，已保留诊断。"
        NextSentenceErrorCode.PANEL_ADD_FAILED -> "结果面板显示失败，但悬浮球已恢复。"
        NextSentenceErrorCode.PANEL_RENDER_FAILED -> "结果面板渲染失败，但悬浮球已恢复。"
        NextSentenceErrorCode.SESSION_TIMEOUT -> "这次分析超时，请回到聊天页再试一次。"
        NextSentenceErrorCode.SESSION_CANCELLED -> "本次分析已取消。"
        NextSentenceErrorCode.NEXT_SENTENCE_TIMEOUT_NO_VISIBLE_RESULT -> "这次没有跑完，已保存诊断。"
        NextSentenceErrorCode.UNKNOWN_EXCEPTION -> "这次没有跑完，已保存诊断。"
        NextSentenceErrorCode.NONE -> ""
        NextSentenceErrorCode.DECISION_EMPTY -> "分析结果为空，已保留诊断。"
        else -> "Next sentence diagnostics were saved."
    }
}

fun Throwable.toNextSentenceException(
    fallbackTrace: NextSentenceSessionTrace,
    fallbackStage: NextSentenceStage
): NextSentenceException {
    if (this is NextSentenceException) return this
    val screenshotCode = mapScreenshotException(this)
    if (screenshotCode != null) {
        val trace = fallbackTrace.failed(
            screenshotCode,
            NextSentenceStage.OPTIONAL_SCREENSHOT_DIAGNOSTIC_FAILED,
            this
        ).copy(
            primaryCapturePath = fallbackTrace.primaryCapturePath,
            nodeTreeAttempted = fallbackTrace.nodeTreeAttempted,
            nodeTreeSuccess = fallbackTrace.nodeTreeSuccess,
            screenshotAttempted = true,
            screenshotSuccess = false,
            screenshotAvailable = false,
            screenshotErrorCode = screenshotCode,
            screenshotExceptionClass = this::class.java.name,
            screenshotExceptionMessageRedacted = message?.redactPrivateText(),
            pipelineExceptionClass = this::class.java.name,
            pipelineExceptionMessageRedacted = message?.redactPrivateText()
        )
        return NextSentenceException(screenshotCode, NextSentenceStage.OPTIONAL_SCREENSHOT_DIAGNOSTIC_FAILED, trace, this)
    }
    return NextSentenceException(
        code = NextSentenceErrorCode.UNKNOWN_EXCEPTION,
        failedStage = fallbackStage,
        trace = fallbackTrace.failed(NextSentenceErrorCode.UNKNOWN_EXCEPTION, fallbackStage, this)
            .copy(
                pipelineExceptionClass = this::class.java.name,
                pipelineExceptionMessageRedacted = message?.redactPrivateText()
            ),
        cause = this
    )
}

fun mapScreenshotException(error: Throwable): NextSentenceErrorCode? {
    val text = "${error::class.java.name}: ${error.message.orEmpty()}"
    if (error is SecurityException && text.contains("Services don't have the capability of taking the screenshot", ignoreCase = true)) {
        return NextSentenceErrorCode.SCREENSHOT_CAPABILITY_MISSING
    }
    return when {
        text.contains("ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS", ignoreCase = true) ||
            text.contains("takeScreenshot_failed_1", ignoreCase = true) -> NextSentenceErrorCode.SCREENSHOT_NOT_ALLOWED
        text.contains("ERROR_TAKE_SCREENSHOT_SECURE_WINDOW", ignoreCase = true) ||
            text.contains("takeScreenshot_failed_2", ignoreCase = true) -> NextSentenceErrorCode.SCREENSHOT_SECURE_WINDOW
        text.contains("ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT", ignoreCase = true) ||
            text.contains("takeScreenshot_failed_3", ignoreCase = true) -> NextSentenceErrorCode.SCREENSHOT_RATE_LIMITED
        text.contains("takeScreenshot", ignoreCase = true) ||
            text.contains("screenshot", ignoreCase = true) -> NextSentenceErrorCode.SCREENSHOT_FAILED
        else -> null
    }
}

fun String.redactPrivateText(maxLength: Int = 120): String {
    return replace(Regex("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+"), "$1[REDACTED]")
        .replace(Regex("sk-[A-Za-z0-9._\\-]+"), "sk-[REDACTED]")
        .take(maxLength)
}
