package com.huiyi.v4.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
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
        return isEnabledFromAccessibilityManager(context) || isEnabledFromSecureSettings(context)
    }

    internal fun isHuiyiServiceEnabledInSetting(
        enabledServices: String,
        packageName: String
    ): Boolean {
        if (enabledServices.isBlank()) return false
        val serviceClass = HuiyiAccessibilityService::class.java.name
        val relativeServiceClass = ".accessibility.HuiyiAccessibilityService"
        val accepted = setOf(
            "$packageName/$serviceClass",
            "$packageName/$relativeServiceClass"
        )
        return enabledServices
            .split(':')
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .any { flattened ->
                accepted.any { acceptedName -> flattened.equals(acceptedName, ignoreCase = true) }
            }
    }

    private fun isEnabledFromSecureSettings(context: Context): Boolean {
        val enabled = Settings.Secure.getInt(context.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1
        if (!enabled) return false
        val services = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        return isHuiyiServiceEnabledInSetting(services, context.packageName)
    }

    private fun isEnabledFromAccessibilityManager(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val expected = ComponentName(context, HuiyiAccessibilityService::class.java)
        return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any { info ->
                val serviceInfo = info.resolveInfo?.serviceInfo
                serviceInfo?.packageName == expected.packageName &&
                    serviceInfo.name == expected.className
            }
    }
}
