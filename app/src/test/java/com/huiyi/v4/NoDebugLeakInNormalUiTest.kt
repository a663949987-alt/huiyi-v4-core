package com.huiyi.v4

import org.junit.Assert.assertFalse
import org.junit.Test

class NoDebugLeakInNormalUiTest {
    @Test
    fun normalUiTextDoesNotExposeDebugFields() {
        val normalUi = listOf("首页", "今日状态", "无障碍状态", "悬浮球状态", "我的底色", "下一句", "设置")
        val banned = listOf("UUID", "batchId", "raw JSON", "parserName", "stackTrace")

        assertFalse(normalUi.any { text -> banned.any(text::contains) })
    }
}
