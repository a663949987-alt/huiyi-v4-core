package com.huiyi.v4.floating

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import com.huiyi.v4.accessibility.HuiyiAccessibilityService

class FloatingBubbleController(
    private val context: Context,
    private val onNextSentence: () -> Unit,
    private val onOneTapFeedback: () -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var rootView: LinearLayout? = null

    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun show() {
        if (rootView != null || !canDrawOverlays()) return
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
        }
        val bubble = Button(context).apply {
            text = "会意"
            setOnClickListener {
                val menuVisible = container.childCount > 1
                if (menuVisible) {
                    container.removeViews(1, container.childCount - 1)
                } else {
                    addMenu(container)
                }
            }
        }
        container.addView(bubble)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
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
        if (reason == "user_hide") OverlayStateStore.markUserHide() else OverlayStateStore.markBubbleVisible(false)
    }

    private fun addMenu(container: LinearLayout) {
        listOf("下一句", "这次不对，发给 GPT", "隐藏").forEach { label ->
            val button = Button(context).apply {
                text = label
                setOnClickListener {
                    when (label) {
                        "下一句" -> {
                            OverlayStateStore.markBubbleClick()
                            onNextSentence()
                        }
                        "这次不对，发给 GPT" -> onOneTapFeedback()
                        "隐藏" -> hide("user_hide")
                    }
                }
            }
            container.addView(button)
        }
    }
}
