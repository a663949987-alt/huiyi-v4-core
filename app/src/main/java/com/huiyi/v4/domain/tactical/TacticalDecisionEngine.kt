package com.huiyi.v4.domain.tactical

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.InfluenceProfile
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MissingContextType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.TranscriptStatus

class TacticalDecisionEngine {
    fun decide(context: ChatSceneContext): TacticalDecision {
        val last = context.lastMessage
        if (last == null) return contextRequired(context, "当前没有可判断消息。")

        if (last.speaker == Speaker.ME) {
            return TacticalDecision(
                decisionType = TacticalDecisionType.WAIT,
                situation = "最后一句是我。",
                coreInsight = "现在继续补话会稀释表达。",
                userLikelyMistake = "忍不住继续解释或加码承诺。",
                bestMove = "等对方回复，不要继续补话。",
                avoidMoves = listOf("不要追问", "不要追加保证", "不要解释太多"),
                coCreationOpportunity = context.coCreationOpportunity,
                shouldUseUserStory = false,
                selectedStoryCardIds = emptyList(),
                influenceProfile = InfluenceProfile(InfluenceIntensity.LOW, RiskLevel.LOW, null, "等对方回来再接。"),
                fallbackMove = "如果很久没回，再发一条轻生活关心。"
            )
        }

        if (last.content is MessageContent.Voice && last.content.transcriptStatus == TranscriptStatus.MISSING) {
            return TacticalDecision(
                decisionType = TacticalDecisionType.VOICE_SUMMARY_REQUIRED,
                situation = "关键最后一条是未转写语音。",
                coreInsight = "不知道语音内容时不能猜测她的意思。",
                userLikelyMistake = "根据气氛硬猜，生成高风险回复。",
                bestMove = "听完后补一句摘要，我再帮你判断怎么回。",
                avoidMoves = listOf("不要猜语音内容", "不要强行深度分析"),
                coCreationOpportunity = context.coCreationOpportunity,
                shouldUseUserStory = false,
                selectedStoryCardIds = emptyList(),
                influenceProfile = InfluenceProfile(InfluenceIntensity.LOW, RiskLevel.MEDIUM, "缺少语音内容，深度判断不可靠。", "我先听一下，听完认真回你。"),
                fallbackMove = "我先听一下，听完认真回你。"
            )
        }

        if (!context.contentCompleteness.canDeepAnalyze) {
            return contextRequired(context, context.contentCompleteness.reason)
        }

        val text = context.allMessages.takeLast(8).joinToString(" ") { it.normalizedText.orEmpty() }
        val lastText = last.normalizedText.orEmpty()
        val pullBack = listOf("没关系", "随便", "不提了", "不聊了", "拜拜", "去忙了", "算了", "忙", "晚点说").any(lastText::contains)
        val vulnerability = listOf("累", "难过", "委屈", "撑不住", "烦").any(text::contains)
        val safetyTest = listOf("安全感", "认真", "负责", "以后", "靠谱不").any(text::contains)

        return when {
            pullBack -> TacticalDecision(
                decisionType = TacticalDecisionType.BOUNDARY_RESPECT,
                situation = "对方在收住话题。",
                coreInsight = "继续追问会把压力推回给她。",
                userLikelyMistake = "急着解释、证明或继续承诺。",
                bestMove = "收住，给具体关心，留出空间。",
                avoidMoves = listOf("不要继续追问", "不要继续承诺", "不要把话题拉回自己"),
                coCreationOpportunity = context.coCreationOpportunity,
                shouldUseUserStory = false,
                selectedStoryCardIds = emptyList(),
                influenceProfile = InfluenceProfile(InfluenceIntensity.LOW, RiskLevel.MEDIUM, "对方已经后撤，强推会失分。", "收到，先不逼你说。"),
                fallbackMove = "你先忙，晚点我再看你状态。"
            )
            vulnerability -> TacticalDecision(
                decisionType = TacticalDecisionType.HUIYI_MOMENT,
                situation = "对方出现脆弱表达。",
                coreInsight = "这是共创理解的窗口，不是展示自己的窗口。",
                userLikelyMistake = "切回自己，或者立刻讲大道理。",
                bestMove = "先听，顺着她的意思，轻问一句。",
                avoidMoves = listOf("不要说教", "不要抢痛苦", "不要马上给方案"),
                coCreationOpportunity = context.coCreationOpportunity,
                shouldUseUserStory = false,
                selectedStoryCardIds = emptyList(),
                influenceProfile = InfluenceProfile(InfluenceIntensity.MEDIUM, RiskLevel.LOW, null, "先把她的话接住。"),
                fallbackMove = "我在，你慢慢说。"
            )
            safetyTest -> TacticalDecision(
                decisionType = TacticalDecisionType.EMPATHY_FIRST,
                situation = "对方在测试稳定感。",
                coreInsight = "少承诺，多给能兑现的稳定感。",
                userLikelyMistake = "承诺过重，让她感觉被压住。",
                bestMove = "先承认她的顾虑，再给一个具体、可执行的小稳定。",
                avoidMoves = listOf("不要发誓", "不要长篇规划", "不要自证"),
                coCreationOpportunity = context.coCreationOpportunity,
                shouldUseUserStory = true,
                selectedStoryCardIds = listOf("army-responsibility"),
                influenceProfile = InfluenceProfile(InfluenceIntensity.MEDIUM, RiskLevel.LOW, null, "把承诺落小。"),
                fallbackMove = "我不急着让你马上信，我先把小事做好。"
            )
            else -> TacticalDecision(
                decisionType = TacticalDecisionType.NORMAL_REPLY,
                situation = "普通聊天推进。",
                coreInsight = "保持稳定、接生活、轻轻推进。",
                userLikelyMistake = "回复太用力或太空。",
                bestMove = "接住她当前内容，给一个轻问题。",
                avoidMoves = listOf("不要突然升浓度", "不要连续讲自己"),
                coCreationOpportunity = context.coCreationOpportunity,
                shouldUseUserStory = false,
                selectedStoryCardIds = emptyList(),
                influenceProfile = InfluenceProfile(InfluenceIntensity.LOW, RiskLevel.LOW, null, "回到轻松节奏。"),
                fallbackMove = "那你先忙，我晚点再找你。"
            )
        }
    }

    private fun contextRequired(context: ChatSceneContext, reason: String): TacticalDecision {
        val missingVoice = MissingContextType.VOICE_WITHOUT_TRANSCRIPT in context.contentCompleteness.missingTypes
        return TacticalDecision(
            decisionType = if (missingVoice) TacticalDecisionType.VOICE_SUMMARY_REQUIRED else TacticalDecisionType.CONTEXT_REQUIRED,
            situation = "上下文不足。",
            coreInsight = reason,
            userLikelyMistake = "在信息不完整时强行判断。",
            bestMove = if (missingVoice) "先补语音摘要。" else "补一屏上下文或手动输入前文。",
            avoidMoves = listOf("不要猜", "不要强行深度分析"),
            coCreationOpportunity = context.coCreationOpportunity,
            shouldUseUserStory = false,
            selectedStoryCardIds = emptyList(),
            influenceProfile = InfluenceProfile(InfluenceIntensity.LOW, RiskLevel.MEDIUM, "上下文不足，判断置信度较低。", null),
            fallbackMove = "我先看一下前后文，再帮你判断。"
        )
    }
}
