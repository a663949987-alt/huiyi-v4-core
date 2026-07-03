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

    fun showLoading() {
        Toast.makeText(context, "会意正在看当前聊天…", Toast.LENGTH_SHORT).show()
    }

    fun show(state: HuiyiRuntimeState) {
        hide()
        val result = state.latestPipelineResult
        if (state.lastError != null) {
            showSimplePanel(
                title = "没读到当前聊天",
                body = "请回到聊起聊天窗口，再点一次“下一句”。"
            )
            return
        }
        val decision = result?.tacticalDecision ?: state.demoState.decision
        val routes = result?.routes ?: state.demoState.routes
        val container = panelContainer()

        if (decision.decisionType == TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED ||
            decision.decisionType == TacticalDecisionType.CHAT_WINDOW_NOT_FOUND
        ) {
            container.addView(titleText("没读到聊天页"))
            container.addView(text("没读到聊天页，请点一下聊起聊天窗口后再试。"))
        } else if (decision.decisionType == TacticalDecisionType.WAIT) {
            container.addView(titleText("先等对方"))
            container.addView(text("你已经回过了，先等对方。"))
        } else {
            container.addView(titleText(resultTitle(state)))
            cloudStatusLine(result)?.let { container.addView(text(it)) }
            decision.coreInsight?.takeIf { it.isNotBlank() }?.let { container.addView(smallText("共创点：$it")) }
            decision.userLikelyMistake?.takeIf { it.isNotBlank() }?.let { container.addView(smallText("容易犯的错：$it")) }
            container.addView(smallText("强度：${decision.influenceProfile.intensity}"))
            decision.influenceProfile.riskWarning?.let { container.addView(smallText("风险：$it")) }
            decision.fallbackMove?.let { container.addView(smallText("撤退方案：$it")) }
            if (routes.isEmpty()) {
                container.addView(text("云端未就绪，已使用本地建议。请回到聊起聊天窗口再试一次。"))
            } else {
                routes.take(5).forEachIndexed { index, route ->
                    container.addView(routeTitle("${index + 1}. ${route.name}"))
                    container.addView(text(route.message))
                    route.riskWarning?.let { container.addView(smallText("风险：$it")) }
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
        }

        container.addView(Button(context).apply {
            text = "这次不对，发给 GPT"
            setOnClickListener { runtime.exportOneTapFeedback() }
        })
        container.addView(Button(context).apply {
            text = "隐藏"
            setOnClickListener { hide() }
        })
        attach(container, panelType = decision.decisionType.name)
        runtime.markOverlayPanelShown()
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

    private fun showSimplePanel(title: String, body: String) {
        hide()
        val container = panelContainer()
        container.addView(titleText(title))
        container.addView(text(body))
        container.addView(Button(context).apply {
            text = "这次不对，发给 GPT"
            setOnClickListener { runtime.exportOneTapFeedback() }
        })
        container.addView(Button(context).apply {
            text = "隐藏"
            setOnClickListener { hide() }
        })
        attach(container, panelType = "loading")
    }

    private fun panelContainer(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(22, 18, 22, 18)
        setBackgroundColor(0xF2FFFFFF.toInt())
    }

    private fun attach(container: LinearLayout, panelType: String) {
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
            OverlayStateStore.markPanelShown(panelType)
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

    private fun resultTitle(state: HuiyiRuntimeState): String {
        val cloud = state.latestPipelineResult?.cloudTrace
        return if (cloud?.decisionSource == "CLOUD") "会意云端分析" else "本地建议"
    }

    private fun cloudStatusLine(result: com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult?): String? {
        val cloud = result?.cloudTrace ?: return null
        if (cloud.cloudErrorCode == "NETWORK") return "云端连接失败，已使用本地建议。"
        return when {
            cloud.decisionSource == "CLOUD" -> "云端已就绪。"
            cloud.cloudFallbackUsed -> "云端暂不可用，已使用本地建议。"
            cloud.cloudSkippedReason == "CLOUD_NOT_CONFIGURED" ||
                cloud.cloudSkippedReason == "RELAY_API_KEY_MISSING" ||
                cloud.cloudSkippedReason == "RELAY_API_KEY_INSECURE_STORAGE" -> "云端未就绪，已使用本地建议。"
            else -> null
        }
    }

    private fun titleText(value: String): TextView = text(value).apply {
        textSize = 18f
        setTextColor(0xFF0F172A.toInt())
    }

    private fun routeTitle(value: String): TextView = text(value).apply {
        textSize = 16f
        setTextColor(0xFF111827.toInt())
    }

    private fun smallText(value: String): TextView = text(value).apply {
        textSize = 13f
        setTextColor(0xFF475569.toInt())
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
