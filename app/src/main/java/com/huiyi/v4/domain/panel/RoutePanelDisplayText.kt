package com.huiyi.v4.domain.panel

import com.huiyi.v4.domain.context.ArcProgressState
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType

object RoutePanelDisplayText {
    fun topActionLine(routes: List<ReplyRoute>): String? {
        val action = when {
            routes.any { it.routeType == ReplyRouteType.ARC_REVEAL } -> "让她看见你"
            routes.firstOrNull()?.routeType == ReplyRouteType.CO_CREATION ||
                routes.any { it.routeType == ReplyRouteType.CO_CREATION && it.recommended } -> "共创"
            routes.firstOrNull()?.routeType == ReplyRouteType.EMPATHY -> "接住她"
            routes.firstOrNull()?.routeType in setOf(ReplyRouteType.COOL_DOWN, ReplyRouteType.WAIT) -> "撤退"
            routes.isNotEmpty() -> "表达我"
            else -> return null
        }
        return "本轮动作：$action"
    }

    fun routeHeader(route: ReplyRoute, index: Int): String {
        val prefix = if (index == 0 || route.recommended) "推荐" else "备选${index + 1}"
        return "$prefix - ${route.panelRouteLabel}"
    }

    fun detailLines(route: ReplyRoute): List<String> {
        val lines = mutableListOf<String>()
        if (route.routeType == ReplyRouteType.ARC_REVEAL) {
            lines += "本轮动作：${route.panelNextAction}"
            route.panelPersonaFacet?.let { facet ->
                lines += "这句话展示了你的哪一面：$facet"
            }
            lines += "不要说过头：${route.riskWarning ?: "不要把轻表达讲成长篇自证。"}"
        }
        return lines
    }

    fun expressSelfSummaryLines(
        arcProgressState: ArcProgressState?,
        routes: List<ReplyRoute>
    ): List<String> {
        val actionLine = topActionLine(routes) ?: "本轮动作：表达我"
        val window = arcProgressState?.currentExpressionWindow
        val windowLine = if (window?.exists == true) {
            val topics = window.triggerTopics
                .map { it.toTopicLabel() }
                .distinct()
                .ifEmpty { listOf("现实 / 规划 / 稳定 / 未来") }
                .joinToString(" / ")
            "她给的窗口：$topics"
        } else {
            "她给的窗口：暂时不明显，先轻一点表达"
        }
        val arcRoute = routes.firstOrNull { it.routeType == ReplyRouteType.ARC_REVEAL }
        val facet = arcProgressState?.suggestedArcCard?.hiddenDepth
            ?: arcRoute?.panelPersonaFacet
            ?: routes.firstNotNullOfOrNull { it.panelPersonaFacet }
            ?: "稳定、真实、低压的一面"
        val suggestedLine = arcProgressState?.suggestedArcCard?.safeRevealLine
            ?: arcRoute?.message
            ?: routes.firstOrNull()?.message
            ?: "先接住她，再轻轻露出一点你的底色。"
        val overdoRisk = arcProgressState?.overdoRisk
            ?: arcRoute?.riskWarning
            ?: "不要把轻表达讲成一大段自我证明。"
        return listOf(
            actionLine,
            "适合露出的你：$facet",
            windowLine,
            "建议句：$suggestedLine",
            "别说过头：$overdoRisk"
        )
    }

    private fun String.toTopicLabel(): String = when (lowercase()) {
        "planning", "plan" -> "规划"
        "reality" -> "现实"
        "stable", "stability" -> "稳定"
        "future" -> "未来"
        "past", "experience" -> "过去经历"
        "responsibility", "responsible" -> "责任感"
        "long term" -> "长期"
        else -> this
    }
}
