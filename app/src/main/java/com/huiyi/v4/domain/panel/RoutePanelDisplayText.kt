package com.huiyi.v4.domain.panel

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
}
