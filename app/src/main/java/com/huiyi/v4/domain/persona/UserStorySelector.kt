package com.huiyi.v4.domain.persona

import com.huiyi.v4.domain.model.ChatSceneContext
import com.huiyi.v4.domain.model.StoryDepth
import com.huiyi.v4.domain.model.TacticalDecision
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.UserPersonaCorpus
import com.huiyi.v4.domain.model.UserStorySelection

class UserStorySelector {
    fun select(
        context: ChatSceneContext,
        decision: TacticalDecision,
        corpus: UserPersonaCorpus
    ): UserStorySelection {
        if (!corpus.enabled) return none()
        if (decision.decisionType == TacticalDecisionType.BOUNDARY_RESPECT || decision.decisionType == TacticalDecisionType.COOL_DOWN) {
            return none("对方在收住时，不抢主角位置。")
        }

        val text = context.allMessages.takeLast(6).joinToString(" ") { it.normalizedText.orEmpty() }
        val safetyTest = listOf("靠谱不", "安全感", "认真", "会不会", "以后", "负责").any(text::contains)
        val asksExperience = listOf("你以前", "你经历", "部队", "转业", "为什么").any(text::contains)

        return when {
            safetyTest -> UserStorySelection(
                shouldUseUserStory = true,
                selectedStoryCardIds = listOf("army-responsibility"),
                depth = StoryDepth.MEDIUM,
                risk = "这条会加入你的经历，但别讲太长，不能抢她的主角位置。",
                fallback = "如果她收住，就改成具体关心。"
            )
            asksExperience -> UserStorySelection(
                shouldUseUserStory = true,
                selectedStoryCardIds = listOf("direct-style"),
                depth = StoryDepth.LIGHT,
                risk = "轻轻解释，不要变成自我汇报。",
                fallback = "问回她的感受。"
            )
            else -> none()
        }
    }

    private fun none(fallback: String? = null) = UserStorySelection(
        shouldUseUserStory = false,
        selectedStoryCardIds = emptyList(),
        depth = StoryDepth.LIGHT,
        risk = null,
        fallback = fallback
    )
}
