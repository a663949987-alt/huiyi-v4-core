package com.huiyi.v4.domain.capture

import com.huiyi.v4.domain.model.MetadataType

class MetadataMessageFilter {
    fun classify(text: String?): MetadataType {
        val value = text?.trim().orEmpty()
        if (value.isBlank()) return MetadataType.UNKNOWN_METADATA

        if (timeRegex.matches(value) || dateTimeRegex.matches(value) || dayRegex.matches(value)) {
            return MetadataType.TIME
        }
        if (dateRegex.matches(value) || monthDayRegex.matches(value)) {
            return MetadataType.DATE
        }
        if (value.startsWith("上次在线时间") || value == "在线" || value == "对方正在输入") {
            return MetadataType.ONLINE_STATUS
        }
        if (uiControls.contains(value) || scenarioRegex.matches(value)) {
            return MetadataType.UI_CONTROL
        }
        if (systemNoticeKeywords.any { value.contains(it) }) {
            return MetadataType.SYSTEM_NOTICE
        }
        if (looksLikeHeader(value)) {
            return MetadataType.HEADER
        }
        return MetadataType.NONE
    }

    fun isMetadata(text: String?): Boolean = classify(text) != MetadataType.NONE

    private fun looksLikeHeader(value: String): Boolean {
        return value == "白云蓝天"
    }

    private companion object {
        val timeRegex = Regex("""^\d{1,2}:\d{2}$""")
        val dateTimeRegex = Regex("""^(今天|昨天)?\s*\d{1,2}:\d{2}$|^\d{1,2}-\d{1,2}\s+\d{1,2}:\d{2}$|^\d{2}-\d{2}\s+\d{1,2}:\d{2}$""")
        val dateRegex = Regex("""^\d{4}-\d{1,2}-\d{1,2}$""")
        val monthDayRegex = Regex("""^\d{1,2}月\d{1,2}日$""")
        val dayRegex = Regex("""^星期[一二三四五六日天]$""")
        val uiControls = setOf("返回", "发送", "语音", "表情", "更多", "图片", "拍摄", "位置", "红包", "转账", "场景", "导出截图", "输入框")
        val scenarioRegex = Regex("""^[A-F]\s+(last_me|last_other|metadata_trap|voice_last_other|unknown_bounds|low_expression)$""")
        val systemNoticeKeywords = listOf("截屏", "撤回了一条消息", "以上是打招呼内容", "已读", "未读")
    }
}
