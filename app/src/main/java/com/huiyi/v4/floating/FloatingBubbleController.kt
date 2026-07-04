package com.huiyi.v4.floating

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.runtime.NextSentenceClickAck

class FloatingBubbleController(
    private val context: Context,
    private val onNextSentence: (NextSentenceClickAck) -> Unit,
    private val onExpressSelf: (NextSentenceClickAck) -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var rootView: LinearLayout? = null
    private var bubbleButton: Button? = null

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun show() {
        if (rootView != null || !canDrawOverlays()) return
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        }
        val bubble = Button(context).apply {
            text = "会意"
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setOnClickListener {
                val menuVisible = container.childCount > 1
                if (menuVisible) {
                    container.removeViews(1, container.childCount - 1)
                } else {
                    addMenu(container)
                }
            }
        }
        bubbleButton = bubble
        container.addView(bubble)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 20
            y = 0
        }
        try {
            windowManager.addView(container, params)
            rootView = container
            OverlayStateStore.markAddViewSuccess()
            OverlayStateStore.markBubbleVisible(true)
        } catch (error: Throwable) {
            OverlayStateStore.recordWindowManagerException(
                error = error,
                operation = "addView",
                windowType = params.type,
                overlayPermissionState = canDrawOverlays(),
                currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                targetPackage = context.packageName
            )
        }
    }

    fun markLoadingAck(
        toastText: String = "会意正在看当前聊天…",
        bubbleText: String = "会意正在看…"
    ): NextSentenceClickAck {
        val clickAt = System.currentTimeMillis()
        val overlayBefore = OverlayStateStore.state.value
        rootView?.let { container ->
            if (container.childCount > 1) {
                container.removeViews(1, container.childCount - 1)
            }
        }
        bubbleButton?.text = bubbleText
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
        val ackAt = System.currentTimeMillis()
        return NextSentenceClickAck(
            clickReceivedAt = clickAt,
            clickAckShownAt = ackAt,
            clickAckVisible = true,
            panelVisibleBeforeClick = overlayBefore.resultPanelVisible || overlayBefore.errorPanelVisible,
            panelVisibleAfterClick = false,
            bubbleVisibleAfterClick = rootView != null
        )
    }

    fun markIdle() {
        bubbleButton?.text = "会意"
    }

    fun hide(reason: String = "user_hide") {
        rootView?.let {
            try {
                windowManager.removeView(it)
            } catch (error: Throwable) {
                OverlayStateStore.recordWindowManagerException(
                    error = error,
                    operation = "removeView",
                    windowType = 0,
                    overlayPermissionState = canDrawOverlays(),
                    currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                    targetPackage = context.packageName
                )
            }
        }
        rootView = null
        bubbleButton = null
        if (reason == "user_hide") OverlayStateStore.markUserHide() else OverlayStateStore.markBubbleVisible(false)
    }

    private fun addMenu(container: LinearLayout) {
        FloatingPanelSplitPolicy.mainMenuLabels.forEach { label ->
            val button = Button(context).apply {
                text = label
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setOnClickListener {
                    when (label) {
                        FloatingPanelSplitPolicy.NEXT_SENTENCE_LABEL -> {
                            OverlayStateStore.markBubbleClick()
                            onNextSentence(markLoadingAck())
                        }
                        FloatingPanelSplitPolicy.EXPRESS_SELF_LABEL -> {
                            OverlayStateStore.markBubbleClick()
                            onExpressSelf(
                                markLoadingAck(
                                    toastText = "会意正在整理表达…",
                                    bubbleText = "会意正在整理…"
                                )
                            )
                        }
                        FloatingPanelSplitPolicy.HIDE_LABEL -> hide("user_hide")
                    }
                }
            }
            container.addView(button)
        }
    }

    private companion object {
        const val NEXT = "下一句"
        const val EXPRESS_SELF = "表达我"
        const val HIDE = "隐藏"
    }
}
