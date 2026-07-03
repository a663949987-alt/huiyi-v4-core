package com.huiyi.v4.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.huiyi.v4.domain.capture.GenericVisualBubbleParser
import com.huiyi.v4.domain.capture.LiaoqiRealParser
import com.huiyi.v4.domain.capture.VisualBubble
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageSource
import com.huiyi.v4.domain.model.VisualBounds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.absoluteValue
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
        val root = rootInActiveWindow
        val rootReady = root != null
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
        refreshLastStableSnapshot(root, now)
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
        val now = System.currentTimeMillis()
        val snapshot = buildSnapshot(root, now)
        updateState { it.copy(rootAvailable = true, lastCaptureAt = now, lastRootAvailableAt = now, lastError = null, activeServiceInstanceId = serviceInstanceId) }
        snapshot
    }.onFailure { error ->
        updateState { it.copy(rootAvailable = false, lastError = error.message) }
    }

    fun currentRootPackageName(): String? = rootInActiveWindow?.packageName?.toString()

    fun currentRootClassName(): String? = rootInActiveWindow?.className?.toString()

    fun lastStableChatSnapshot(): LastStableForeignWindowSnapshot? = lastStableSnapshot

    private fun buildSnapshot(root: AccessibilityNodeInfo, now: Long): CurrentScreenSnapshot {
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        val nodes = mutableListOf<ScreenNodeSnapshot>()
        collect(root, 0, emptyList(), nodes)
        return CurrentScreenSnapshot(
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
    }

    private fun refreshLastStableSnapshot(root: AccessibilityNodeInfo?, now: Long) {
        if (root == null) return
        if (now - lastStableSnapshotRefreshAt < 700) return
        val packageName = root.packageName?.toString().orEmpty()
        if (!isStableForeignPackage(packageName)) return
        runCatching {
            val snapshot = buildSnapshot(root, now)
            val visibleTexts = snapshot.nodes.mapNotNull { it.readableText?.takeIf(String::isNotBlank) }
            if (snapshot.nodes.isEmpty() || visibleTexts.isEmpty()) return
            val messages = parseMessages(snapshot)
            if (messages.isEmpty() && visibleTexts.size < 3) return
            val stable = LastStableForeignWindowSnapshot(
                capturedAt = snapshot.capturedAt,
                packageName = packageName,
                windowTitle = snapshot.windowTitle,
                rootClassName = root.className?.toString(),
                nodeCount = snapshot.nodes.size,
                visibleTextCount = visibleTexts.size,
                rawTextPreviewRedacted = visibleTexts.take(12).map { it.take(24) },
                normalizedMessages = messages,
                source = "accessibility_event_stable_foreign_window",
                nodesHash = visibleTexts.joinToString("|").hashCode().absoluteValue.toString(16),
                snapshot = snapshot
            )
            lastStableSnapshot = stable
            lastStableSnapshotRefreshAt = now
            updateState {
                it.copy(
                    lastStableSnapshotAt = stable.capturedAt,
                    lastStableSnapshotPackage = stable.packageName,
                    lastStableSnapshotVisibleTextCount = stable.visibleTextCount
                )
            }
        }
    }

    private fun isStableForeignPackage(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        if (packageName == applicationContext.packageName) return false
        if (packageName == "com.android.systemui") return false
        if (packageName.contains("launcher", ignoreCase = true)) return false
        if (packageName.contains("settings", ignoreCase = true)) return false
        return true
    }

    private fun parseMessages(snapshot: CurrentScreenSnapshot) = parseForApp(
        appPackage = snapshot.appPackage,
        screenWidth = snapshot.screenWidth,
        bubbles = snapshot.nodes.toVisualBubbles()
    ).filter { it.normalizedText?.isNotBlank() == true || it.content is MessageContent.Voice }

    private fun parseForApp(appPackage: String?, screenWidth: Int, bubbles: List<VisualBubble>) =
        if (appPackage == "com.bajiao.im.liaoqi") {
            val liaoqi = LiaoqiRealParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
            if (liaoqi.any { it.isEffectiveChatMessage }) liaoqi else GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
        } else {
            GenericVisualBubbleParser(screenWidth = screenWidth).parse(bubbles, MessageSource.ACCESSIBILITY_CURRENT_SCREEN)
        }

    private fun List<ScreenNodeSnapshot>.toVisualBubbles(): List<VisualBubble> {
        return asSequence()
            .filter { it.visibleToUser }
            .filter { it.bounds.right > it.bounds.left && it.bounds.bottom > it.bounds.top }
            .filter { it.readableText?.isNotBlank() == true }
            .filterNot { it.bounds.bottom - it.bounds.top < 4 }
            .map { node ->
                VisualBubble(
                    id = node.id,
                    text = node.readableText,
                    rowBounds = node.parentBounds ?: node.bounds,
                    bubbleBounds = node.ancestorBoundsChain.asReversed()
                        .firstOrNull { bounds -> bounds != node.bounds && bounds.right > bounds.left && bounds.bottom > bounds.top }
                        ?: node.parentBounds
                        ?: node.bounds,
                    textBounds = node.bounds,
                    parentBounds = node.parentBounds,
                    ancestorBoundsChain = node.ancestorBoundsChain,
                    confidence = if (node.className?.contains("TextView") == true) 88 else 72
                )
            }
            .toList()
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

        @Volatile
        private var lastStableSnapshot: LastStableForeignWindowSnapshot? = null

        @Volatile
        private var lastStableSnapshotRefreshAt: Long = 0

        private val mutableState = MutableStateFlow(HuiyiAccessibilityState())
        val state: StateFlow<HuiyiAccessibilityState> = mutableState

        private fun updateState(block: (HuiyiAccessibilityState) -> HuiyiAccessibilityState) {
            mutableState.value = block(mutableState.value)
        }
    }
}
