package com.huiyi.v4.floating

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
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
        windowManager.addView(scroll, params)
        panelView = scroll
        runtime.markOverlayPanelShown()
    }

    fun hide() {
        panelView?.let { runCatching { windowManager.removeView(it) } }
        panelView = null
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
