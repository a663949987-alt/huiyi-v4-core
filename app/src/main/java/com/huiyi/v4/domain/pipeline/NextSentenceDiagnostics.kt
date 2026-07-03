package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.domain.model.Speaker

enum class NextSentenceErrorCode {
    NONE,
    ACCESSIBILITY_SYSTEM_DISABLED,
    ACCESSIBILITY_SERVICE_NOT_CONNECTED,
    ROOT_UNAVAILABLE,
    ROOT_IS_OWN_OVERLAY,
    ROOT_IS_SYSTEM_UI,
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
    PANEL_ADD_FAILED,
    PANEL_RENDER_FAILED,
    SESSION_CANCELLED,
    SESSION_TIMEOUT,
    UNKNOWN_EXCEPTION
}

enum class NextSentenceStage {
    IDLE,
    CLICK_RECEIVED,
    ACCESSIBILITY_STATE_CHECKED,
    ROOT_CAPTURE_STARTED,
    ROOT_CAPTURE_RETRYING,
    ROOT_CAPTURED,
    FALLBACK_SNAPSHOT_USED,
    NODE_TREE_EXTRACTED,
    CHAT_MESSAGES_PARSED,
    LAST_SPEAKER_DECIDED,
    ROUTES_GENERATED,
    PANEL_RENDERING,
    PANEL_RENDERED,
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
    val exceptionClass: String? = null,
    val exceptionMessageRedacted: String? = null,
    val bubbleVisibleBeforeClick: Boolean = false,
    val bubbleVisibleAfterClick: Boolean = false,
    val bubbleAttachedAfterClick: Boolean = false,
    val bubbleVisibleAfterFailure: Boolean = false,
    val systemAccessibilityEnabled: Boolean = false,
    val serviceConnected: Boolean = false,
    val permissionMissingMessageShown: Boolean = false,
    val activePackageBeforeClick: String? = null,
    val activePackageAtCaptureStart: String? = null,
    val activePackageAfterRootRetry: String? = null,
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
        NextSentenceErrorCode.ACCESSIBILITY_SERVICE_NOT_CONNECTED -> "系统已开启无障碍，但会意服务还没连接，请回到聊天页稍等后重试。"
        NextSentenceErrorCode.ROOT_UNAVAILABLE -> "当前窗口暂时不可读取，请回到聊天页再试一次。"
        NextSentenceErrorCode.ROOT_IS_OWN_OVERLAY -> "刚才焦点停在会意悬浮窗，已尝试使用聊天页快照。"
        NextSentenceErrorCode.ROOT_IS_SYSTEM_UI -> "当前焦点在系统界面，请回到聊天页再试一次。"
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
        NextSentenceErrorCode.ROUTE_SCHEMA_INVALID -> "路线结果格式异常，已保留诊断。"
        NextSentenceErrorCode.ROUTE_COUNT_INVALID -> "路线数量异常，已保留诊断。"
        NextSentenceErrorCode.PANEL_ADD_FAILED -> "结果面板显示失败，但悬浮球已恢复。"
        NextSentenceErrorCode.PANEL_RENDER_FAILED -> "结果面板渲染失败，但悬浮球已恢复。"
        NextSentenceErrorCode.SESSION_TIMEOUT -> "这次分析超时，请回到聊天页再试一次。"
        NextSentenceErrorCode.SESSION_CANCELLED -> "本次分析已取消。"
        NextSentenceErrorCode.UNKNOWN_EXCEPTION -> "这次分析失败，已保存诊断。"
        NextSentenceErrorCode.NONE -> ""
        NextSentenceErrorCode.DECISION_EMPTY -> "分析结果为空，已保留诊断。"
    }
}

fun Throwable.toNextSentenceException(
    fallbackTrace: NextSentenceSessionTrace,
    fallbackStage: NextSentenceStage
): NextSentenceException {
    return this as? NextSentenceException ?: NextSentenceException(
        code = NextSentenceErrorCode.UNKNOWN_EXCEPTION,
        failedStage = fallbackStage,
        trace = fallbackTrace.failed(NextSentenceErrorCode.UNKNOWN_EXCEPTION, fallbackStage, this),
        cause = this
    )
}

fun String.redactPrivateText(maxLength: Int = 120): String {
    return replace(Regex("(?i)(bearer\\s+)[A-Za-z0-9._\\-]+"), "$1[REDACTED]")
        .replace(Regex("sk-[A-Za-z0-9._\\-]+"), "sk-[REDACTED]")
        .take(maxLength)
}
