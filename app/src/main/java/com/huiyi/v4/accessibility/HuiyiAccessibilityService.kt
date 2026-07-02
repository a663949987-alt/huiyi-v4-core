package com.huiyi.v4.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.huiyi.v4.domain.model.VisualBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicLong

class HuiyiAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        updateState { it.copy(serviceConnected = true, rootAvailable = rootInActiveWindow != null, lastError = null) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString()
        val title = event?.text?.joinToString(" ")?.takeIf { it.isNotBlank() }
        updateState {
            it.copy(
                currentPackage = pkg ?: it.currentPackage,
                currentWindowTitle = title ?: it.currentWindowTitle,
                rootAvailable = rootInActiveWindow != null
            )
        }
    }

    override fun onInterrupt() {
        updateState { it.copy(lastError = "无障碍服务被系统中断") }
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        updateState { it.copy(serviceConnected = false, rootAvailable = false) }
        super.onDestroy()
    }

    fun captureCurrentScreen(): Result<CurrentScreenSnapshot> = runCatching {
        val root = rootInActiveWindow ?: error("rootInActiveWindow 为空")
        val metrics = resources.displayMetrics
        val nodes = mutableListOf<ScreenNodeSnapshot>()
        collect(root, 0, nodes)
        val now = System.currentTimeMillis()
        updateState { it.copy(rootAvailable = true, lastCaptureAt = now, lastError = null) }
        CurrentScreenSnapshot(
            appPackage = root.packageName?.toString() ?: state.value.currentPackage,
            windowTitle = state.value.currentWindowTitle,
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
            nodes = nodes,
            capturedAt = now
        )
    }.onFailure { error ->
        updateState { it.copy(rootAvailable = false, lastError = error.message) }
    }

    private fun collect(node: AccessibilityNodeInfo, depth: Int, out: MutableList<ScreenNodeSnapshot>) {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        out += ScreenNodeSnapshot(
            id = "n${nodeCounter.incrementAndGet()}",
            text = node.text?.toString(),
            contentDescription = node.contentDescription?.toString(),
            className = node.className?.toString(),
            viewIdResourceName = node.viewIdResourceName,
            bounds = VisualBounds(rect.left, rect.top, rect.right, rect.bottom),
            visibleToUser = node.isVisibleToUser,
            depth = depth,
            childCount = node.childCount
        )
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            collect(child, depth + 1, out)
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
