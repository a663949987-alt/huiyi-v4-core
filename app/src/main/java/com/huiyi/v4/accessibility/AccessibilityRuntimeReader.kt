package com.huiyi.v4.accessibility

import android.content.Context
import android.provider.Settings
import com.huiyi.v4.floating.OverlayStateStore

object AccessibilityRuntimeReader {
    fun read(context: Context): AccessibilityRuntimeState {
        val service = HuiyiAccessibilityService.state.value
        val overlay = OverlayStateStore.state.value
        return AccessibilityRuntimeState(
            systemAccessibilityEnabled = isSystemAccessibilityEnabled(context),
            serviceConnected = service.serviceConnected,
            rootAvailable = service.rootAvailable,
            currentPackage = service.currentPackage,
            currentWindowTitle = service.currentWindowTitle,
            lastServiceConnectedAt = service.lastServiceConnectedAt,
            lastAccessibilityEventAt = service.lastAccessibilityEventAt,
            lastRootAvailableAt = service.lastRootAvailableAt,
            lastDisconnectAt = service.lastDisconnectAt,
            lastInterruptAt = service.lastInterruptAt,
            lastDestroyAt = service.lastDestroyAt,
            lastError = service.lastError,
            activeServiceInstanceId = service.activeServiceInstanceId,
            overlayVisible = overlay.bubbleVisible || overlay.resultPanelVisible || overlay.errorPanelVisible,
            floatingServiceRunning = overlay.floatingServiceRunning
        )
    }

    fun isSystemAccessibilityEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
        if (!enabled) return false
        val services = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty()
        return services.contains(context.packageName)
    }
}
