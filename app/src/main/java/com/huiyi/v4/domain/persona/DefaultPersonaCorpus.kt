package com.huiyi.v4.domain.persona

import com.huiyi.v4.domain.model.CharacterArcCard
import com.huiyi.v4.domain.model.IdentityCard
import com.huiyi.v4.domain.model.RiskRule
import com.huiyi.v4.domain.model.StoryCard
import com.huiyi.v4.domain.model.StyleRule
import com.huiyi.v4.domain.model.UserPersonaCorpus

object DefaultPersonaCorpus {
    fun soldier(enabled: Boolean = true): UserPersonaCorpus = UserPersonaCorpus(
        id = "soldier-transition-core",
        name = "军人 / 即将转业",
        enabled = enabled,
        identityCards = listOf(
            IdentityCard(
                id = "soldier",
                title = "军人",
                summary = "军旅背景、即将转业，生活规律，表达直接，重视责任。",
                values = listOf("责任感", "稳定", "纪律", "能吃苦", "长期主义"),
                bestFor = listOf("安全感测试", "责任话题", "未来规划", "现实稳定感"),
                avoidWhen = listOf("轻松闲聊", "对方刚刚脆弱打开", "对方已经收住话题"),
                risk = "容易显得说教、自证、压迫。"
            )
        ),
        storyCards = listOf(
            StoryCard("army-responsibility", "部队责任感", listOf("安全感测试", "认真关系", "稳定承诺"), "我不想用一堆保证让你马上相信我。部队里待久了，我更相信一件事：责任不是说给别人听的，是平时一点点做出来的。", "容易变成自我证明。"),
            StoryCard("night-check", "夜里查寝", listOf("具体稳定感"), "部队里有些事挺小，但要天天做到，比如晚上查寝。后来我觉得，责任感不是大话，是别人看不见的时候也不糊弄。", "容易显得说教。"),
            StoryCard("phone-discipline", "不能随便用手机", listOf("解释回复慢"), "我有时候不是不想回，是部队环境和纪律摆在那里。但我如果忙完看到，会尽量给你一个交代，不让你一直悬着。", "不要变成借口。"),
            StoryCard("morning-drill", "早起出操", listOf("轻松生活话题"), "我这边又是早起出操的一天，身体还没完全醒，嘴已经被军号叫醒了。", "只适合轻松场景。"),
            StoryCard("transition", "即将转业", listOf("未来规划", "现实稳定"), "我现在也在一个新阶段，转业以后会重新进入社会。但我不是怕重新开始，我更想把以后的生活一步步走稳。", "不要变成长篇规划汇报。"),
            StoryCard("direct-style", "说话直接", listOf("解释个人风格", "降低误解"), "我可能有点部队后遗症，说话比较直，不太会绕弯。但认真的事，我不会随便说。", "不要用“我就这样”逃避沟通。")
        ),
        styleRules = listOf(
            StyleRule("direct-but-soft", "直接但放软", "表达责任感时先接住对方，再讲自己的稳定。")
        ),
        riskRules = listOf(
            RiskRule("over-promise", "承诺过重", "避免一上来给长期承诺，优先给具体、可兑现的小稳定。")
        ),
        characterArcCards = listOf(
            CharacterArcCard(
                surfaceImpression = "不太会讲漂亮话，表达偏直接。",
                hiddenDepth = "稳定、负责、愿意把现实里的事一点点做到位。",
                contrastTension = "表面克制、不热闹，里面有长期感和责任感。",
                revealTrigger = "现实 规划 稳定 未来 责任感 过去经历",
                safeRevealLine = "我可能不是特别会讲漂亮话，但认真起来会把事情一点点做到位。",
                overdoRisk = "不要讲成长篇自我证明，也不要像汇报经历，露出一点就收住。",
                relatedPersonaCardIds = listOf("soldier", "transition", "army-responsibility")
            )
        )
    )
}
