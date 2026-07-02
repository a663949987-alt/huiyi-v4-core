package com.huiyi.v4.domain.tactical

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TacticalDecisionType

class ReplyRouteGenerator {
    fun generate(context: ChatSceneContext, decision: TacticalDecision): List<ReplyRoute> {
        val base = when (decision.decisionType) {
            TacticalDecisionType.WAIT -> waitRoutes()
            TacticalDecisionType.VOICE_SUMMARY_REQUIRED -> voiceRoutes()
            TacticalDecisionType.CONTEXT_REQUIRED -> contextRoutes()
            TacticalDecisionType.BOUNDARY_RESPECT, TacticalDecisionType.COOL_DOWN -> riskRoutes()
            TacticalDecisionType.HUIYI_MOMENT, TacticalDecisionType.EMPATHY_FIRST -> huiyiRoutes()
            TacticalDecisionType.WARM_UP, TacticalDecisionType.PUSH_LIGHTLY -> warmRoutes()
            else -> normalRoutes()
        }
        val withStory = if (decision.shouldUseUserStory && base.none { it.routeType == ReplyRouteType.SELF_STORY }) {
            base.dropLast(1) + storyRoute()
        } else base
        return withStory.take(5).mapIndexed { index, route -> route.copy(recommended = index == 0) }
    }

    private fun riskRoutes() = listOf(
        route("收住", ReplyRouteType.COOL_DOWN, "稳妥", "好，我先不追着你说。你先忙/先缓一缓，我晚点再看你状态。"),
        route("修复", ReplyRouteType.REPAIR, "修复", "刚才我可能有点急了，不想让你有压力。你不用马上回应我。"),
        route("降浓度", ReplyRouteType.STABLE, "稳妥", "我明白，你先把自己的节奏放前面。"),
        route("转生活", ReplyRouteType.STABLE, "轻生活", "那你先去忙，记得吃点东西，别一直硬撑。"),
        route("等待", ReplyRouteType.WAIT, "等待", "先不发，等她下一句。")
    )

    private fun huiyiRoutes() = listOf(
        route("认真听", ReplyRouteType.EMPATHY, "会意", "听起来你今天真的有点累，我先不讲道理，你慢慢说。"),
        route("顺着她", ReplyRouteType.EMPATHY, "会意", "嗯，我懂你不是想抱怨，是那一刻真的需要有人站在你这边。"),
        route("轻问一句", ReplyRouteType.CO_CREATION, "共创", "那现在最让你卡住的是事情本身，还是没人理解你？"),
        route("帮她说完整", ReplyRouteType.CO_CREATION, "共创", "你像是想把事情做好，但又不想一直一个人扛着。"),
        route("一起落结论", ReplyRouteType.STABLE, "稳妥", "今天先别急着解决所有事，我先陪你把这一段缓过去。")
    )

    private fun warmRoutes() = listOf(
        route("稳住", ReplyRouteType.STABLE, "稳妥", "这个我记住了，下次我会更早一点照顾到你的感受。"),
        route("轻暧昧", ReplyRouteType.WARM_UP, "升温", "你这么一说，我还挺想现在就过去看看你。", RiskLevel.MEDIUM),
        route("特别感", ReplyRouteType.WARM_UP, "特别感", "不是谁这样说我都会认真听，但你说我会放在心上。"),
        route("具体关心", ReplyRouteType.STABLE, "关心", "你现在先喝点水，别空着肚子硬扛。"),
        route("推进", ReplyRouteType.DIRECT, "主动", "等你忙完，我们把这个事好好说清楚。", RiskLevel.MEDIUM)
    )

    private fun normalRoutes() = listOf(
        route("稳妥", ReplyRouteType.STABLE, "稳妥", "嗯，我懂你的意思。那你现在是更想先休息，还是想继续说说？"),
        route("轻松", ReplyRouteType.STABLE, "轻松", "听起来今天不太省心，先给你记一笔辛苦分。"),
        route("反问", ReplyRouteType.CO_CREATION, "反问", "那你希望我现在怎么接你，会更舒服一点？"),
        route("接生活", ReplyRouteType.STABLE, "生活", "你先把手头的事弄完，别让自己一直绷着。"),
        route("升一点", ReplyRouteType.WARM_UP, "升温", "我还挺喜欢你愿意跟我说这些的。", RiskLevel.MEDIUM)
    )

    private fun waitRoutes() = listOf(
        route("等待", ReplyRouteType.WAIT, "稳妥", "先不发，等对方回复。"),
        route("轻收尾", ReplyRouteType.WAIT, "备用", "如果很久没回，再发：你先忙，我晚点再看你。"),
        route("转生活", ReplyRouteType.STABLE, "备用", "晚点再用生活关心接回。"),
        route("不解释", ReplyRouteType.WAIT, "别做", "不要继续解释。"),
        route("不加码", ReplyRouteType.WAIT, "别做", "不要继续承诺。")
    )

    private fun voiceRoutes() = listOf(
        route("先听", ReplyRouteType.WAIT, "安全", "我先听一下，听完认真回你。"),
        route("补摘要", ReplyRouteType.WAIT, "动作", "听完后补一句摘要，再判断。"),
        route("不猜", ReplyRouteType.WAIT, "边界", "不猜语音内容。"),
        route("轻确认", ReplyRouteType.STABLE, "安全", "我听完再回你，不敷衍。"),
        route("等待", ReplyRouteType.WAIT, "稳妥", "先不要生成深度回复。")
    )

    private fun contextRoutes() = listOf(
        route("补前文", ReplyRouteType.WAIT, "动作", "再补一屏前文，我再判断。"),
        route("手动摘要", ReplyRouteType.WAIT, "动作", "手动写一句前文摘要。"),
        route("低风险", ReplyRouteType.STABLE, "安全", "我先看一下前后文，别误判你的意思。"),
        route("不强推", ReplyRouteType.WAIT, "边界", "先不生成强路线。"),
        route("临时回复", ReplyRouteType.STABLE, "安全", "我怕理解偏了，等我看完整再认真回。")
    )

    private fun storyRoute() = route(
        "自我经历版",
        ReplyRouteType.SELF_STORY,
        "底色",
        "我不想用一堆保证让你马上相信我。部队里待久了，我更相信责任是平时一点点做出来的。",
        riskLevel = RiskLevel.MEDIUM,
        riskWarning = "这条会加入你的经历，但别讲太长，不能抢她的主角位置。",
        fallbackMove = "如果她收住，立刻改成具体关心。"
    )

    private fun route(
        name: String,
        type: ReplyRouteType,
        tag: String,
        message: String,
        riskLevel: RiskLevel = RiskLevel.LOW,
        riskWarning: String? = null,
        fallbackMove: String? = null
    ) = ReplyRoute(
        id = "route-${name.hashCode()}",
        name = name,
        routeType = type,
        tag = tag,
        message = message,
        intensity = if (riskLevel == RiskLevel.LOW) InfluenceIntensity.LOW else InfluenceIntensity.MEDIUM,
        riskLevel = riskLevel,
        riskWarning = riskWarning ?: if (riskLevel != RiskLevel.LOW) "这条更主动，注意观察对方是否后撤。" else null,
        expectedEffect = "降低误判，推动当前聊天更稳。",
        fallbackMove = fallbackMove,
        recommended = false
    )
}
