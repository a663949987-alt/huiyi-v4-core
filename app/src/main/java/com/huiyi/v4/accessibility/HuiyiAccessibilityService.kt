package com.huiyi.v4.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.huiyi.v4.domain.model.VisualBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicLong
import java.util.UUID

class HuiyiAccessibilityService : AccessibilityService() {
    private val serviceInstanceId: String = UUID.randomUUID().toString()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val now = System.currentTimeMillis()
        updateState {
            it.copy(
                serviceConnected = true,
                rootAvailable = rootInActiveWindow != null,
                lastError = null,
                lastServiceConnectedAt = now,
                lastRootAvailableAt = if (rootInActiveWindow != null) now else it.lastRootAvailableAt,
                activeServiceInstanceId = serviceInstanceId
            )
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString()
        val title = event?.text?.joinToString(" ")?.takeIf { it.isNotBlank() }
        val rootReady = rootInActiveWindow != null
        val now = System.currentTimeMillis()
        updateState {
            it.copy(
                currentPackage = pkg ?: it.currentPackage,
                currentWindowTitle = title ?: it.currentWindowTitle,
                rootAvailable = rootReady,
                lastAccessibilityEventAt = now,
                lastRootAvailableAt = if (rootReady) now else it.lastRootAvailableAt,
                activeServiceInstanceId = serviceInstanceId
            )
        }
    }

    override fun onInterrupt() {
        updateState { it.copy(lastError = "无障碍服务被系统中断", lastInterruptAt = System.currentTimeMillis()) }
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        val now = System.currentTimeMillis()
        updateState {
            it.copy(
                serviceConnected = false,
                rootAvailable = false,
                lastDisconnectAt = now,
                lastDestroyAt = now,
                activeServiceInstanceId = null
            )
        }
        super.onDestroy()
    }

    fun captureCurrentScreen(): Result<CurrentScreenSnapshot> = runCatching {
        val root = rootInActiveWindow ?: error("rootInActiveWindow 为空")
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        val nodes = mutableListOf<ScreenNodeSnapshot>()
        collect(root, 0, emptyList(), nodes)
        val now = System.currentTimeMillis()
        updateState { it.copy(rootAvailable = true, lastCaptureAt = now, lastRootAvailableAt = now, lastError = null, activeServiceInstanceId = serviceInstanceId) }
        CurrentScreenSnapshot(
            appPackage = root.packageName?.toString() ?: state.value.currentPackage,
            windowTitle = state.value.currentWindowTitle,
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
            nodes = nodes,
            capturedAt = now,
            density = metrics.density,
            scaledDensity = metrics.scaledDensity,
            fontScale = configuration.fontScale,
            smallestScreenWidthDp = configuration.smallestScreenWidthDp,
            displaySizeCategory = configuration.screenLayout.toString()
        )
    }.onFailure { error ->
        updateState { it.copy(rootAvailable = false, lastError = error.message) }
    }

    private fun collect(
        node: AccessibilityNodeInfo,
        depth: Int,
        ancestorBounds: List<VisualBounds>,
        out: MutableList<ScreenNodeSnapshot>
    ) {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val bounds = VisualBounds(rect.left, rect.top, rect.right, rect.bottom)
        out += ScreenNodeSnapshot(
            id = "n${nodeCounter.incrementAndGet()}",
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            viewIdResourceName = node.viewIdResourceName,
            bounds = bounds,
            visibleToUser = node.isVisibleToUser,
            depth = depth,
            childCount = node.childCount,
            parentBounds = ancestorBounds.lastOrNull(),
            ancestorBoundsChain = ancestorBounds
        )
        val nextAncestors = (ancestorBounds + bounds).takeLast(8)
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            collect(child, depth + 1, nextAncestors, out)
            child.recycle()
        }
    }

    companion object {
        private val nodeCounter = AtomicLong(0)
        @Volatile
        var instance: HuiyiAccessibilityService? = null
            private set

        private val mutableState = MutableStateFlow(HuiyiAccessibilityState())
        val state: StateFlow<HuiyiAccessibilityState> = mutableState

        private fun updateState(block: (HuiyiAccessibilityState) -> HuiyiAccessibilityState) {
            mutableState.value = block(mutableState.value)
        }
    }
}
