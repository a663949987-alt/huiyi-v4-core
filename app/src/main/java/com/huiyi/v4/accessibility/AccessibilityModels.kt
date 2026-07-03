package com.huiyi.v4.accessibility

import com.huiyi.v4.domain.model.VisualBounds

data class HuiyiAccessibilityState(
    val serviceConnected: Boolean = false,
    val currentPackage: String? = null,
    val currentWindowTitle: String? = null,
    val rootAvailable: Boolean = false,
    val lastCaptureAt: Long? = null,
    val lastError: String? = null,
    val lastServiceConnectedAt: Long? = null,
    val lastAccessibilityEventAt: Long? = null,
    val lastRootAvailableAt: Long? = null,
    val lastDisconnectAt: Long? = null,
    val lastInterruptAt: Long? = null,
    val lastDestroyAt: Long? = null,
    val activeServiceInstanceId: String? = null
)

data class AccessibilityRuntimeState(
    val systemAccessibilityEnabled: Boolean,
    val serviceConnected: Boolean,
    val rootAvailable: Boolean,
    val currentPackage: String?,
    val currentWindowTitle: String?,
    val lastServiceConnectedAt: Long?,
    val lastAccessibilityEventAt: Long?,
    val lastRootAvailableAt: Long?,
    val lastDisconnectAt: Long?,
    val lastInterruptAt: Long?,
    val lastDestroyAt: Long?,
    val lastError: String?,
    val activeServiceInstanceId: String?,
    val overlayVisible: Boolean,
    val floatingServiceRunning: Boolean
) {
    val category: AccessibilityRuntimeCategory
        get() = when {
            !systemAccessibilityEnabled -> AccessibilityRuntimeCategory.DISABLED_IN_SYSTEM
            !serviceConnected -> AccessibilityRuntimeCategory.ENABLED_BUT_SERVICE_NOT_CONNECTED
            !rootAvailable -> AccessibilityRuntimeCategory.CONNECTED_BUT_ROOT_UNAVAILABLE
            else -> AccessibilityRuntimeCategory.CONNECTED_AND_READY
        }
}

enum class AccessibilityRuntimeCategory {
    DISABLED_IN_SYSTEM,
    ENABLED_BUT_SERVICE_NOT_CONNECTED,
    CONNECTED_BUT_ROOT_UNAVAILABLE,
    CONNECTED_AND_READY,
    UNKNOWN
}

fun accessibilityRuntimeMessage(state: AccessibilityRuntimeState): String {
    return when (state.category) {
        AccessibilityRuntimeCategory.DISABLED_IN_SYSTEM -> "无障碍未开启，请前往系统设置开启。"
        AccessibilityRuntimeCategory.ENABLED_BUT_SERVICE_NOT_CONNECTED -> "系统无障碍已开启，但会意服务暂未连接。请返回聊天窗口等待几秒，或重新关闭/开启一次无障碍。"
        AccessibilityRuntimeCategory.CONNECTED_BUT_ROOT_UNAVAILABLE -> "无障碍已开启，但当前窗口暂时不可读取。请确认你停留在聊天页面。"
        AccessibilityRuntimeCategory.CONNECTED_AND_READY -> "无障碍已开启，当前窗口可读取。"
        AccessibilityRuntimeCategory.UNKNOWN -> "无障碍状态未知。"
    }
}

data class ScreenNodeSnapshot(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val viewIdResourceName: String?,
    val bounds: VisualBounds,
    val visibleToUser: Boolean,
    val depth: Int,
    val childCount: Int,
    val parentBounds: VisualBounds? = null,
    val ancestorBoundsChain: List<VisualBounds> = emptyList()
) {
    val readableText: String?
        get() = text?.takeIf { it.isNotBlank() } ?: contentDescription?.takeIf { it.isNotBlank() }
}

data class CurrentScreenSnapshot(
    val appPackage: String?,
    val windowTitle: String?,
    val screenWidth: Int,
    val screenHeight: Int,
    val nodes: List<ScreenNodeSnapshot>,
    val capturedAt: Long,
    val density: Float? = null,
    val scaledDensity: Float? = null,
    val fontScale: Float? = null,
    val smallestScreenWidthDp: Int? = null,
    val displaySizeCategory: String? = null
)

val CurrentScreenSnapshot.fontScaleEstimate: Float?
    get() {
        val densityValue = density ?: return fontScale
        val scaledDensityValue = scaledDensity ?: return fontScale
        if (densityValue <= 0f) return fontScale
        return scaledDensityValue / densityValue
    }

fun CurrentScreenSnapshot.displaySizeLabel(): String {
    val sw = smallestScreenWidthDp ?: return displaySizeCategory ?: "unknown"
    return displaySizeCategory ?: when {
        sw < 360 -> "compact"
        sw < 600 -> "phone"
        sw < 840 -> "large_phone_or_small_tablet"
        else -> "tablet_or_expanded"
    }
}
