package com.huiyi.v4.accessibility

import com.huiyi.v4.domain.model.VisualBounds

data class HuiyiAccessibilityState(
    val serviceConnected: Boolean = false,
    val currentPackage: String? = null,
    val currentWindowTitle: String? = null,
    val rootAvailable: Boolean = false,
    val lastCaptureAt: Long? = null,
    val lastError: String? = null
)

data class ScreenNodeSnapshot(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val viewIdResourceName: String?,
    val bounds: VisualBounds,
    val visibleToUser: Boolean,
    val depth: Int,
    val childCount: Int
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
    val capturedAt: Long
)
