package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType

data class HuiyiOutputQualityGateResult(
    val pass: Boolean,
    val rejectReason: String = if (pass) "PASS" else "UNKNOWN",
    val severity: String = if (pass) "NONE" else "BLOCK"
)

class HuiyiOutputQualityGate {
    fun assessRoute(
        route: ReplyRoute,
        routeSource: String = route.routeSource,
        requestPurpose: CloudRequestPurpose,
        sceneTags: List<String> = emptyList()
    ): HuiyiOutputQualityGateResult {
        val source = routeSource.ifBlank { route.routeSource }
        val text = route.message.trim()
        if (source.isBlank()) return reject("ROUTE_SOURCE_MISSING")
        if (source in blockedSources) return reject("LEGACY_ROUTE_SOURCE")
        if (source !in allowedSources) return reject("ROUTE_SOURCE_NOT_ALLOWED")
        if (legacyTemplatePhrases.any { text.contains(it) }) return reject("LEGACY_TEMPLATE_PHRASE")
        if (legacyMojibakeFragments.any { text.contains(it) }) return reject("LEGACY_TEMPLATE_PHRASE")
        if (text.isBlank()) return reject("EMPTY_ROUTE")
        if (tooOilyFragments.any { text.contains(it) }) return reject("TOO_OILY")
        if (tooHeavyFragments.any { text.contains(it) }) return reject("TOO_HEAVY")
        if (overPromiseFragments.any { text.contains(it) }) return reject("OVERPROMISE")
        if (requestPurpose == CloudRequestPurpose.PASSIVE_PLAYBOOK && route.routeType in activeOnlyRouteTypes) {
            return reject("ACTIVE_ROUTE_IN_PASSIVE_PANEL")
        }
        if (requiresPlanningGrounding(sceneTags) && requestPurpose == CloudRequestPurpose.ACTIVE_EXPRESSION) {
            val grounded = planningGroundingTokens.any { text.contains(it, ignoreCase = true) } ||
                route.routeType in setOf(
                    ReplyRouteType.ARC_REVEAL,
                    ReplyRouteType.CO_CREATION,
                    ReplyRouteType.SELF_STORY,
                    ReplyRouteType.WARM_UP,
                    ReplyRouteType.COOL_DOWN
                )
            if (!grounded) return reject("NOT_TIED_TO_CURRENT_TOPIC")
        }
        return HuiyiOutputQualityGateResult(pass = true)
    }

    fun assessRouteSet(
        routes: List<ReplyRoute>,
        requestPurpose: CloudRequestPurpose,
        sceneTags: List<String> = emptyList()
    ): HuiyiOutputQualityGateResult {
        if (routes.isEmpty()) return HuiyiOutputQualityGateResult(pass = true)
        val routeResults = routes.map { assessRoute(it, requestPurpose = requestPurpose, sceneTags = sceneTags) }
        routeResults.firstOrNull { !it.pass }?.let { return it }
        if (routes.all { isQuestionOnly(it.message) }) return reject("ALL_QUESTION_ROUTES")
        if (requestPurpose == CloudRequestPurpose.ACTIVE_EXPRESSION) {
            if (routes.all { it.routeType == ReplyRouteType.EMPATHY }) return reject("ALL_RECEIVE_ROUTES")
            if (requiresPlanningGrounding(sceneTags)) {
                if (routes.none { it.routeType == ReplyRouteType.ARC_REVEAL }) return reject("ARC_REQUIRED_MISSING")
                if (routes.none { it.routeType == ReplyRouteType.SELF_STORY }) return reject("EXPRESS_SELF_REQUIRED_MISSING")
                if (routes.none { it.routeType == ReplyRouteType.CO_CREATION }) return reject("CO_CREATE_REQUIRED_MISSING")
            }
        }
        return HuiyiOutputQualityGateResult(pass = true)
    }

    fun visibleRoutes(
        routes: List<ReplyRoute>,
        requestPurpose: CloudRequestPurpose,
        sceneTags: List<String> = emptyList()
    ): List<ReplyRoute> = routes.map { route ->
        val result = assessRoute(route, requestPurpose = requestPurpose, sceneTags = sceneTags)
        route.copy(
            qualityGatePass = result.pass,
            qualityGateRejectReason = if (result.pass) "" else result.rejectReason
        )
    }.filter { it.qualityGatePass }

    fun requiresPlanningGrounding(sceneTags: List<String>): Boolean =
        sceneTags.any { tag ->
            val normalized = tag.lowercase()
            normalized in planningSceneTags || planningGroundingTokens.any { token ->
                normalized.contains(token.lowercase())
            }
        }

    private fun reject(reason: String): HuiyiOutputQualityGateResult =
        HuiyiOutputQualityGateResult(pass = false, rejectReason = reason)

    private fun isQuestionOnly(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false
        val questionMarks = trimmed.count { it == '?' || it == '？' }
        val statementSignals = listOf("我", "我们", "先", "可以", "不用", "不急", "认同", "愿意")
        return questionMarks > 0 && statementSignals.none { trimmed.contains(it) }
    }

    companion object {
        const val SOURCE_CLOUD_VERIFIED_PASSIVE_NEXT = "CLOUD_VERIFIED_PASSIVE_NEXT"
        const val SOURCE_USER_APPROVED_CACHE = "USER_APPROVED_CACHE"
        const val SOURCE_EXPRESS_SELF_ARC_PLANNER = "EXPRESS_SELF_ARC_PLANNER"
        const val SOURCE_CLOUD_ENHANCED_PLAYBOOK = "CLOUD_ENHANCED_PLAYBOOK"
        const val SOURCE_LOCAL_PLAYBOOK_FALLBACK_PASSIVE_NEXT = "LOCAL_PLAYBOOK_FALLBACK_PASSIVE_NEXT"
        const val SOURCE_LEGACY_REPLY_GENERATOR = "LEGACY_REPLY_GENERATOR"
        const val SOURCE_LOCAL_RULE_TEMPLATE_PASSIVE = "LOCAL_RULE_TEMPLATE_PASSIVE"
        const val SOURCE_GENERIC_NORMAL_REPLY_TEMPLATE = "GENERIC_NORMAL_REPLY_TEMPLATE"

        private val allowedSources = setOf(
            SOURCE_CLOUD_VERIFIED_PASSIVE_NEXT,
            SOURCE_USER_APPROVED_CACHE,
            SOURCE_EXPRESS_SELF_ARC_PLANNER,
            SOURCE_CLOUD_ENHANCED_PLAYBOOK
        )
        private val blockedSources = setOf(
            SOURCE_LEGACY_REPLY_GENERATOR,
            SOURCE_LOCAL_PLAYBOOK_FALLBACK_PASSIVE_NEXT,
            SOURCE_LOCAL_RULE_TEMPLATE_PASSIVE,
            SOURCE_GENERIC_NORMAL_REPLY_TEMPLATE
        )
        private val activeOnlyRouteTypes = setOf(
            ReplyRouteType.ARC_REVEAL,
            ReplyRouteType.SELF_STORY,
            ReplyRouteType.CO_CREATION
        )
        private val planningSceneTags = setOf(
            "planning",
            "reality",
            "stability",
            "future",
            "responsibility",
            "army",
            "transfer",
            "past"
        )
        private val planningGroundingTokens = listOf(
            "规划",
            "现实",
            "稳定",
            "未来",
            "责任",
            "转业",
            "部队",
            "老班长",
            "一步一步",
            "走稳",
            "长期",
            "创业",
            "planning",
            "reality",
            "stable",
            "future",
            "responsibility",
            "army",
            "transfer"
        )
        private val legacyTemplatePhrases = listOf(
            "那你希望我现在怎么接你",
            "那你现在是更想先休息",
            "先给你记一笔辛苦分",
            "我还挺喜欢你愿意跟我说这些",
            "嗯，我懂你的意思。那你现在是"
        )
        private val legacyMojibakeFragments = listOf(
            "閭ｄ綘甯屾湜鎴戠幇鍦",
            "閭ｄ綘鐜板湪鏄",
            "鍏堢粰浣犺",
            "鎴戣繕鎸哄枩娆",
            "鍡紝鎴戞噦浣犵殑鎰忔€濄€傞偅浣犵幇鍦",
            "鏇存兂鍏堜紤鎭",
            "鏇存兂鍏堢紦"
        )
        private val tooOilyFragments = listOf("宝贝", "心肝", "命中注定", "我会一直宠你", "离不开你")
        private val tooHeavyFragments = listOf("一辈子", "结婚", "必须", "永远", "负责到底")
        private val overPromiseFragments = listOf("我保证", "一定给你", "全部交给我", "绝对不会")
    }
}
