package com.huiyi.v4.floating

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.runtime.HuiyiRuntime
import com.huiyi.v4.runtime.HuiyiRuntimeState

class FloatingResultPanelController(
    private val context: Context,
    private val runtime: HuiyiRuntime
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var panelView: ScrollView? = null

    fun show(state: HuiyiRuntimeState) {
        hide()
        val result = state.latestPipelineResult
        if (state.lastError != null && result == null) {
            showError(state.lastError)
            return
        }
        val decision = result?.tacticalDecision ?: state.demoState.decision
        val routes = result?.routes ?: state.demoState.routes
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(0xF2FFFFFF.toInt())
        }
        container.addView(text("会意雷达"))
        container.addView(text("判断：${decision.situation}"))
        container.addView(text("打法：${decision.bestMove}"))
        container.addView(text("别做：${decision.avoidMoves.joinToString(" / ")}"))
        decision.influenceProfile.riskWarning?.let { container.addView(text("风险：$it")) }
        decision.fallbackMove?.let { container.addView(text("撤退：$it")) }

        if (decision.decisionType == TacticalDecisionType.WAIT) {
            container.addView(text("最后一句是你发的，先等她回，不要继续补话。"))
        } else if (routes.isEmpty()) {
            container.addView(text("当前信息不足，先不要强行生成高置信回复。"))
        } else {
            routes.forEachIndexed { index, route ->
                container.addView(text("${index + 1}. ${route.name}｜${route.tag}"))
                container.addView(text(route.message))
                route.riskWarning?.let { container.addView(text("风险：$it")) }
                val copyButton = Button(context).apply {
                    text = "复制"
                    setOnClickListener {
                        copy(route.message)
                        runtime.createCopiedAttempt(route)
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    }
                }
                container.addView(copyButton)
            }
        }

        val close = Button(context).apply {
            text = "收起"
            setOnClickListener { hide() }
        }
        container.addView(close)

        val scroll = ScrollView(context).apply { addView(container) }
        val params = WindowManager.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 36
        }
        try {
            windowManager.addView(scroll, params)
            panelView = scroll
            OverlayStateStore.markAddViewSuccess()
            OverlayStateStore.markPanelShown(decision.decisionType.name)
            runtime.markOverlayPanelShown()
        } catch (error: Throwable) {
            OverlayStateStore.recordWindowManagerException(
                error = error,
                operation = "addView",
                windowType = params.type,
                overlayPermissionState = Settings.canDrawOverlays(context),
                currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                targetPackage = context.packageName
            )
        }
    }

    fun hide() {
        panelView?.let {
            runCatching { windowManager.removeView(it) }.onFailure { error ->
                OverlayStateStore.recordWindowManagerException(
                    error = error,
                    operation = "removeView",
                    windowType = 0,
                    overlayPermissionState = Settings.canDrawOverlays(context),
                    currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                    targetPackage = context.packageName
                )
            }
        }
        panelView = null
        OverlayStateStore.markPanelDismissed("panel_hide")
    }

    private fun showError(errorMessage: String) {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 18, 22, 18)
            setBackgroundColor(0xF2FFFFFF.toInt())
        }
        container.addView(text("这次分析失败，但悬浮球仍在。"))
        container.addView(text(errorMessage))
        container.addView(Button(context).apply {
            text = "重试"
            setOnClickListener { runtime.runNextSentence() }
        })
        container.addView(Button(context).apply {
            text = "导出诊断"
            setOnClickListener { runtime.exportClickDiagnosticReports() }
        })
        container.addView(Button(context).apply {
            text = "打开无障碍设置"
            setOnClickListener {
                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        })
        container.addView(Button(context).apply {
            text = "隐藏悬浮球"
            setOnClickListener { hide() }
        })
        val scroll = ScrollView(context).apply { addView(container) }
        val params = WindowManager.LayoutParams(
            (context.resources.displayMetrics.widthPixels * 0.92f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 36
        }
        try {
            windowManager.addView(scroll, params)
            panelView = scroll
            OverlayStateStore.markPanelShown("error")
        } catch (error: Throwable) {
            OverlayStateStore.recordWindowManagerException(
                error = error,
                operation = "addView",
                windowType = params.type,
                overlayPermissionState = Settings.canDrawOverlays(context),
                currentForegroundPackage = HuiyiAccessibilityService.state.value.currentPackage,
                targetPackage = context.packageName
            )
        }
    }

    private fun text(value: String): TextView = TextView(context).apply {
        text = value
        textSize = 15f
        setTextColor(0xFF111827.toInt())
        setPadding(0, 6, 0, 6)
    }

    private fun copy(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("会意回复", value))
    }
}
