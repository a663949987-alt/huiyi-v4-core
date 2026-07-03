package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.MetadataType

class MetadataMessageFilter {
    fun classify(text: String?): MetadataType {
        val value = text?.trim().orEmpty()
        if (value.isBlank()) return MetadataType.UNKNOWN_METADATA

        if (timeRegex.matches(value) || dateTimeRegex.matches(value)) {
            return MetadataType.TIME
        }
        if (dateRegex.matches(value) || monthDayRegex.matches(value) || dateWordRegex.matches(value) || dayRegex.matches(value)) {
            return MetadataType.DATE
        }
        if (onlineStatusValues.any { value == it || value.startsWith(it) } || value.startsWith("涓婃")) {
            return MetadataType.ONLINE_STATUS
        }
        if (uiControls.contains(value) || scenarioRegex.matches(value) || profileScenarioRegex.matches(value)) {
            return MetadataType.UI_CONTROL
        }
        if (systemNoticeKeywords.any { value.contains(it) }) {
            return MetadataType.SYSTEM_NOTICE
        }
        if (readReceiptValues.any { value.equals(it, ignoreCase = true) || value.contains(it, ignoreCase = true) }) {
            return MetadataType.READ_RECEIPT
        }
        if (sendFailedValues.any { value.equals(it, ignoreCase = true) || value.contains(it, ignoreCase = true) }) {
            return MetadataType.SEND_STATUS
        }
        if (deliveryStatusValues.any { value.equals(it, ignoreCase = true) || value.contains(it, ignoreCase = true) }) {
            return MetadataType.DELIVERY_STATUS
        }
        if (messageStatusIconValues.contains(value)) {
            return MetadataType.MESSAGE_STATUS_ICON
        }
        if (looksLikeHeader(value)) {
            return MetadataType.HEADER
        }
        return MetadataType.NONE
    }

    fun isMetadata(text: String?): Boolean = classify(text) != MetadataType.NONE

    private fun looksLikeHeader(value: String): Boolean {
        return value in headers || value.contains('\u3049')
    }

    private companion object {
        val timeRegex = Regex("""^\d{1,2}:\d{2}$""")
        val dateTimeRegex = Regex("""^(\u4eca\u5929|\u6628\u5929)?\s*\d{1,2}:\d{2}$|^\d{1,2}-\d{1,2}\s+\d{1,2}:\d{2}$|^\d{2}-\d{2}\s+\d{1,2}:\d{2}$""")
        val dateRegex = Regex("""^(\d{4}-)?\d{1,2}-\d{1,2}$""")
        val monthDayRegex = Regex("""^\d{1,2}\u6708\d{1,2}\u65e5.*""")
        val dateWordRegex = Regex("""^(\u4eca\u5929|\u6628\u5929)$""")
        val dayRegex = Regex("""^\u661f\u671f[\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u65e5\u5929]$""")
        val headers = setOf(
            "\u767d\u4e91\u84dd\u5929",
            "\u84dd\u6865",
            "\u5c0f\u9e7f\u540c\u5b66",
            "\u6797\u590f",
            "\u5bf9\u8bdd"
        )
        val onlineStatusValues = setOf(
            "\u4e0a\u6b21\u5728\u7ebf\u65f6\u95f4",
            "\u5728\u7ebf",
            "\u5bf9\u65b9\u6b63\u5728\u8f93\u5165",
            "\u521a\u521a\u5728\u7ebf",
            "\u624b\u673a\u5728\u7ebf",
            "\u8d44\u6599\u5b8c\u6574\u5ea6"
        )
        val uiControls = setOf(
            "\u8fd4\u56de",
            "\u53d1\u9001",
            "\u8bed\u97f3",
            "\u8868\u60c5",
            "\u66f4\u591a",
            "\u56fe\u7247",
            "\u62cd\u6444",
            "\u4f4d\u7f6e",
            "\u7ea2\u5305",
            "\u8f6c\u8d26",
            "\u573a\u666f",
            "\u6a21\u677f",
            "\u5bfc\u51fa\u622a\u56fe",
            "\u8f93\u5165\u6846",
            "杩斿洖",
            "鍙戦€?",
            "璇煶",
            "琛ㄦ儏",
            "鍦烘櫙",
            "瀵煎嚭鎴浘",
            "杈撳叆妗?"
        )
        val scenarioRegex = Regex("""^[A-J]\s+(last_me|last_other|metadata_trap|voice_last_other|image_or_sticker|low_expression|long_multiline|quoted_reply|unknown_bounds|time_at_bottom)$""")
        val profileScenarioRegex = Regex("""^(WECHAT_LIKE|QQ_LIKE|REDBOOK_DM_LIKE|DATING_APP_LIKE|MINIMAL_CHAT_LIKE)\s*/\s*[A-J]\s+.*$""")
        val systemNoticeKeywords = listOf(
            "\u7cfb\u7edf\u63d0\u793a",
            "\u7cfb\u7edf\u63a8\u8350",
            "\u4f60\u4eec\u5df2\u6210\u4e3a\u597d\u53cb",
            "\u6253\u62db\u547c\u63d0\u793a",
            "\u8d44\u6599\u5361\u63d0\u793a",
            "鎴睆",
            "鎾ゅ洖浜嗕竴鏉℃秷鎭?",
            "浠ヤ笂鏄墦鎷涘懠鍐呭",
            "宸茶",
            "鏈"
        )
        val readReceiptValues = listOf(
            "\u5df2\u8bfb",
            "\u5bf9\u65b9\u5df2\u8bfb",
            "read",
            "seen"
        )
        val deliveryStatusValues = listOf(
            "\u672a\u8bfb",
            "\u672a\u770b",
            "\u5df2\u9001\u8fbe",
            "\u5df2\u53d1\u9001",
            "\u53d1\u9001\u6210\u529f",
            "delivered",
            "sent",
            "unread"
        )
        val sendFailedValues = listOf(
            "\u53d1\u9001\u5931\u8d25",
            "\u672a\u53d1\u51fa",
            "\u91cd\u53d1",
            "failed"
        )
        val messageStatusIconValues = setOf(
            "\u2713",
            "\u2713\u2713",
            "\u2714",
            "\u2714\u2714",
            "\u25cf",
            "\u2022"
        )
    }
}
