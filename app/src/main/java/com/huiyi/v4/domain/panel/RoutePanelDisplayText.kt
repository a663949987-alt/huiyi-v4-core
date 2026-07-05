package com.huiyi.v4.domain.panel

import com.huiyi.v4.domain.context.ArcProgressState
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType

object RoutePanelDisplayText {
    fun topActionLine(routes: List<ReplyRoute>): String? {
        val action = when {
            routes.any { it.routeType == ReplyRouteType.ARC_REVEAL } -> "\u8ba9\u5979\u770b\u89c1\u4f60"
            routes.firstOrNull()?.routeType == ReplyRouteType.CO_CREATION ||
                routes.any { it.routeType == ReplyRouteType.CO_CREATION && it.recommended } -> "\u5171\u521b"
            routes.firstOrNull()?.routeType == ReplyRouteType.EMPATHY -> "\u63a5\u4f4f\u5979"
            routes.firstOrNull()?.routeType in setOf(ReplyRouteType.COOL_DOWN, ReplyRouteType.WAIT) -> "\u64a4\u9000"
            routes.isNotEmpty() -> "\u8868\u8fbe\u6211"
            else -> return null
        }
        return "\u672c\u8f6e\u52a8\u4f5c\uff1a$action"
    }

    fun routeHeader(route: ReplyRoute, index: Int): String {
        val prefix = if (index == 0 || route.recommended) "\u63a8\u8350" else "\u5907\u9009${index + 1}"
        return "$prefix - ${route.panelRouteLabel}"
    }

    fun detailLines(route: ReplyRoute): List<String> {
        val lines = mutableListOf<String>()
        if (route.routeType == ReplyRouteType.ARC_REVEAL) {
            lines += "\u672c\u8f6e\u52a8\u4f5c\uff1a${route.panelNextAction}"
            route.panelPersonaFacet?.let { facet ->
                lines += "\u8fd9\u53e5\u8bdd\u5c55\u793a\u4e86\u4f60\u7684\u54ea\u4e00\u9762\uff1a$facet"
            }
            lines += "\u4e0d\u8981\u8bf4\u8fc7\u5934\uff1a${route.riskWarning ?: "\u4e0d\u8981\u628a\u8f7b\u8868\u8fbe\u8bb2\u6210\u957f\u7bc7\u81ea\u8bc1\u3002"}"
        }
        return lines
    }

    fun expressSelfSummaryLines(
        arcProgressState: ArcProgressState?,
        routes: List<ReplyRoute>
    ): List<String> {
        val actionLine = topActionLine(routes) ?: "\u672c\u8f6e\u52a8\u4f5c\uff1a\u8868\u8fbe\u6211"
        val window = arcProgressState?.currentExpressionWindow
        val windowLine = if (window?.exists == true) {
            val topics = window.triggerTopics
                .map { it.toTopicLabel() }
                .distinct()
                .ifEmpty { listOf("\u73b0\u5b9e / \u89c4\u5212 / \u7a33\u5b9a / \u672a\u6765") }
                .joinToString(" / ")
            "\u5979\u7ed9\u7684\u7a97\u53e3\uff1a$topics"
        } else {
            "\u5979\u7ed9\u7684\u7a97\u53e3\uff1a\u6682\u65f6\u4e0d\u660e\u663e\uff0c\u5148\u8f7b\u4e00\u70b9\u8868\u8fbe"
        }
        val arcRoute = routes.firstOrNull { it.routeType == ReplyRouteType.ARC_REVEAL }
        val facet = arcProgressState?.suggestedArcCard?.hiddenDepth
            ?: arcRoute?.panelPersonaFacet
            ?: routes.firstNotNullOfOrNull { it.panelPersonaFacet }
            ?: "\u7a33\u5b9a\u3001\u771f\u5b9e\u3001\u4f4e\u538b\u7684\u4e00\u9762"
        val suggestedLine = arcProgressState?.suggestedArcCard?.safeRevealLine
            ?: arcRoute?.message
            ?: routes.firstOrNull()?.message
            ?: "\u5148\u63a5\u4f4f\u5979\uff0c\u518d\u8f7b\u8f7b\u9732\u51fa\u4e00\u70b9\u4f60\u7684\u5e95\u8272\u3002"
        val overdoRisk = arcProgressState?.overdoRisk
            ?: arcRoute?.riskWarning
            ?: "\u4e0d\u8981\u628a\u8f7b\u8868\u8fbe\u8bb2\u6210\u4e00\u5927\u6bb5\u81ea\u6211\u8bc1\u660e\u3002"
        val metaRoute = routes.firstOrNull {
            it.panelExpressionMode != null ||
                it.panelArcTheme != null ||
                it.panelModeReason != null ||
                it.panelAvoidLine != null
        }
        val expressionLedgerLines = listOfNotNull(
            metaRoute?.panelExpressionMode?.let { "\u8868\u8fbe\u6a21\u5f0f\uff1a$it" },
            metaRoute?.panelArcTheme?.let { "\u5f53\u524d\u6bcd\u9898\uff1a$it" },
            metaRoute?.panelModeReason?.let { "\u4e3a\u4ec0\u4e48\u8fd9\u6b21\u53ef\u4ee5\u8bf4\uff1a$it" },
            metaRoute?.panelAvoidLine?.let { "\u8fd9\u6b21\u522b\u600e\u4e48\u8bf4\uff1a$it" }
        )
        return expressionLedgerLines + listOf(
            actionLine,
            "\u9002\u5408\u9732\u51fa\u7684\u4f60\uff1a$facet",
            windowLine,
            "\u5efa\u8bae\u53e5\uff1a$suggestedLine",
            "\u522b\u8bf4\u8fc7\u5934\uff1a$overdoRisk"
        )
    }

    private fun String.toTopicLabel(): String = when (lowercase()) {
        "planning", "plan" -> "\u89c4\u5212"
        "reality" -> "\u73b0\u5b9e"
        "stable", "stability" -> "\u7a33\u5b9a"
        "future" -> "\u672a\u6765"
        "past", "experience" -> "\u8fc7\u53bb\u7ecf\u5386"
        "responsibility", "responsible" -> "\u8d23\u4efb\u611f"
        "long term" -> "\u957f\u671f"
        else -> this
    }
}
