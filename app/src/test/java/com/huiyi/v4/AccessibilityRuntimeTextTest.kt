package com.huiyi.v4

import com.huiyi.v4.accessibility.AccessibilityRuntimeReader
import com.huiyi.v4.accessibility.AccessibilityRuntimeState
import com.huiyi.v4.accessibility.accessibilityRuntimeMessage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityRuntimeTextTest {
    @Test
    fun enabledButServiceDisconnectedTextIsNotNoPermission() {
        val message = accessibilityRuntimeMessage(
            state(systemEnabled = true, serviceConnected = false, rootAvailable = false)
        )

        assertFalse(message.contains("没有权限"))
        assertTrue(message.contains("系统开关是开的"))
        assertTrue(message.contains("服务还没真正连上"))
    }

    @Test
    fun rootUnavailableTextExplainsCurrentWindowUnreadable() {
        val message = accessibilityRuntimeMessage(
            state(systemEnabled = true, serviceConnected = true, rootAvailable = false)
        )

        assertFalse(message.contains("没有权限"))
        assertTrue(message.contains("无障碍已开启"))
        assertTrue(message.contains("当前窗口暂时不可读取"))
    }

    @Test
    fun enabledServiceSettingMatchesFullRelativeAndMixedHuiyiComponent() {
        val packageName = "com.huiyi.v4"
        val service = "com.huiyi.v4.accessibility.HuiyiAccessibilityService"
        val relative = ".accessibility.HuiyiAccessibilityService"

        assertTrue(AccessibilityRuntimeReader.isHuiyiServiceEnabledInSetting("$packageName/$service", packageName))
        assertTrue(AccessibilityRuntimeReader.isHuiyiServiceEnabledInSetting("$packageName/$relative", packageName))
        assertTrue(
            AccessibilityRuntimeReader.isHuiyiServiceEnabledInSetting(
                "com.other/.OtherService:$packageName/$relative",
                packageName
            )
        )
        assertFalse(AccessibilityRuntimeReader.isHuiyiServiceEnabledInSetting("com.other.app/$service", packageName))
        assertFalse(AccessibilityRuntimeReader.isHuiyiServiceEnabledInSetting("$packageName/com.other.Service", packageName))
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
