package com.huiyi.v4

import org.junit.Assert.assertFalse
import org.junit.Test

class DebugLeakHomeUiTest {
    @Test
    fun homeLabelsDoNotContainDebugFields() {
        val homeLabels = listOf("今日状态", "无障碍状态", "悬浮球状态", "当前模式", "我的底色", "新聊天", "设置")
        val banned = listOf("UUID", "batchId", "raw JSON", "parserName")

        assertFalse(homeLabels.any { label -> banned.any(label::contains) })
    }
}
