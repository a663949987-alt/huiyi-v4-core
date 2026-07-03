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
