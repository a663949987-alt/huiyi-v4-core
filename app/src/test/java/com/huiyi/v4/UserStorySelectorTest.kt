package com.huiyi.v4

import com.huiyi.v4.domain.context.ContextAssembler
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.persona.DefaultPersonaCorpus
import com.huiyi.v4.domain.persona.UserStorySelector
import com.huiyi.v4.domain.tactical.TacticalDecisionEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserStorySelectorTest {
    @Test
    fun safetyTestCanSelectResponsibilityStory() {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("1", Speaker.ME, "我会认真对待", 1),
                textNode("2", Speaker.OTHER, "你真的靠谱吗，我需要安全感", 2),
                textNode("3", Speaker.ME, "我明白", 3),
                textNode("4", Speaker.OTHER, "那你说说", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val selection = UserStorySelector().select(context, decision, DefaultPersonaCorpus.soldier())

        assertTrue(selection.shouldUseUserStory)
        assertTrue("army-responsibility" in selection.selectedStoryCardIds)
    }

    @Test
    fun storyNotUsedWhenOtherPullsBack() {
        val context = ContextAssembler().assemble(
            listOf(
                textNode("1", Speaker.ME, "以后我都陪你", 1),
                textNode("2", Speaker.OTHER, "算了，不聊了", 2),
                textNode("3", Speaker.ME, "我知道了", 3),
                textNode("4", Speaker.OTHER, "真的不提了", 4)
            )
        )
        val decision = TacticalDecisionEngine().decide(context)
        val selection = UserStorySelector().select(context, decision, DefaultPersonaCorpus.soldier())

        assertTrue(decision.decisionType == TacticalDecisionType.BOUNDARY_RESPECT || decision.decisionType == TacticalDecisionType.COOL_DOWN)
        assertFalse(selection.shouldUseUserStory)
    }
}
