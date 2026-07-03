package com.huiyi.v4

import com.huiyi.v4.accessibility.AccessibilityRuntimeState
import com.huiyi.v4.accessibility.accessibilityRuntimeMessage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityRuntimeTextTest {
    @Test
    fun enabledButServiceDisconnectedTextIsNotNoPermission() {
        val message = accessibilityRuntimeMessage(state(systemEnabled = true, serviceConnected = false, rootAvailable = false))

        assertFalse(message.contains("没有权限"))
        assertTrue(message.contains("系统无障碍已开启，但会意服务暂未连接"))
    }

    @Test
    fun rootUnavailableTextExplainsCurrentWindowUnreadable() {
        val message = accessibilityRuntimeMessage(state(systemEnabled = true, serviceConnected = true, rootAvailable = false))

        assertFalse(message.contains("没有权限"))
        assertTrue(message.contains("无障碍已开启，但当前窗口暂时不可读取"))
    }

    private fun state(
        systemEnabled: Boolean,
        serviceConnected: Boolean,
        rootAvailable: Boolean
    ) = AccessibilityRuntimeState(
        systemAccessibilityEnabled = systemEnabled,
        serviceConnected = serviceConnected,
        rootAvailable = rootAvailable,
        currentPackage = null,
        currentWindowTitle = null,
        lastServiceConnectedAt = null,
        lastAccessibilityEventAt = null,
        lastRootAvailableAt = null,
        lastDisconnectAt = null,
        lastInterruptAt = null,
        lastDestroyAt = null,
        lastError = null,
        activeServiceInstanceId = null,
        overlayVisible = false,
        floatingServiceRunning = false
    )
}
