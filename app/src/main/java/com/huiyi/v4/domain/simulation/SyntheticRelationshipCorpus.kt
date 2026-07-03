package com.huiyi.v4.domain.simulation

import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.TacticalDecisionType

data class SyntheticRelationshipSample(
    val id: String,
    val category: SyntheticScenarioCategory,
    val messages: List<SyntheticTurn>,
    val expectedDecisionType: TacticalDecisionType,
    val coCreationPoint: String,
    val userLikelyMistake: String,
    val intensity: InfluenceIntensity,
    val risk: RiskLevel,
    val fallback: String
)

data class SyntheticTurn(
    val speaker: String,
    val text: String
)

enum class SyntheticScenarioCategory(val id: String) {
    BLIND_DATE("blind_date"),
    AMBIGUOUS_FLIRTING("ambiguous_flirting"),
    COLD_REPLY("cold_reply"),
    PRESSURE("pressure"),
    LIFE_SHARE("life_share"),
    READ_NO_REPLY("read_no_reply"),
    USER_MULTI_SEND("user_multi_send")
}

object SyntheticRelationshipCorpusGenerator {
    fun generate(minCount: Int = 200): List<SyntheticRelationshipSample> {
        val categories = SyntheticScenarioCategory.entries
        val perCategory = (minCount + categories.size - 1) / categories.size
        return categories.flatMap { category ->
            (0 until perCategory).map { index -> sample(category, index) }
        }.take(minCount.coerceAtLeast(200))
    }

    private fun sample(category: SyntheticScenarioCategory, index: Int): SyntheticRelationshipSample {
        return when (category) {
            SyntheticScenarioCategory.BLIND_DATE -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("OTHER", "I usually need some time before meeting again."),
                    SyntheticTurn("ME", "That is fine, no rush."),
                    SyntheticTurn("OTHER", "I just do not want things to feel forced.")
                ),
                expectedDecisionType = TacticalDecisionType.EMPATHY_FIRST,
                coCreationPoint = "Make the pace feel jointly chosen instead of demanded.",
                userLikelyMistake = "Trying to lock the next date too early.",
                intensity = InfluenceIntensity.LOW,
                risk = RiskLevel.LOW,
                fallback = "Keep it light and leave an easy exit."
            )
            SyntheticScenarioCategory.AMBIGUOUS_FLIRTING -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("OTHER", "You disappeared again."),
                    SyntheticTurn("ME", "I was caught at work."),
                    SyntheticTurn("OTHER", "Hmm, sounds like an excuse.")
                ),
                expectedDecisionType = TacticalDecisionType.REPAIR,
                coCreationPoint = "Turn the tease into a small shared rhythm.",
                userLikelyMistake = "Over-explaining and killing the playful signal.",
                intensity = InfluenceIntensity.MEDIUM,
                risk = RiskLevel.LOW,
                fallback = "Acknowledge the miss, then add one light concrete detail."
            )
            SyntheticScenarioCategory.COLD_REPLY -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("OTHER", "ok"),
                    SyntheticTurn("OTHER", "busy"),
                    SyntheticTurn("OTHER", "later")
                ),
                expectedDecisionType = TacticalDecisionType.BOUNDARY_RESPECT,
                coCreationPoint = "Respect the low-energy boundary.",
                userLikelyMistake = "Forcing depth from minimal expression.",
                intensity = InfluenceIntensity.LOW,
                risk = RiskLevel.MEDIUM,
                fallback = "Low-pressure care, no interrogation."
            )
            SyntheticScenarioCategory.PRESSURE -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("OTHER", "I feel like you want an answer right now."),
                    SyntheticTurn("ME", "I do care about this."),
                    SyntheticTurn("OTHER", "That is exactly the pressure I mean.")
                ),
                expectedDecisionType = TacticalDecisionType.COOL_DOWN,
                coCreationPoint = "Reduce pressure before proposing a next step.",
                userLikelyMistake = "Defending intent instead of lowering intensity.",
                intensity = InfluenceIntensity.LOW,
                risk = RiskLevel.HIGH,
                fallback = "Name the pressure and pause."
            )
            SyntheticScenarioCategory.LIFE_SHARE -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("OTHER", "I finally cooked something decent tonight."),
                    SyntheticTurn("ME", "That sounds nice."),
                    SyntheticTurn("OTHER", "It was simple, but I felt relaxed.")
                ),
                expectedDecisionType = TacticalDecisionType.NORMAL_REPLY,
                coCreationPoint = "Build a small shared daily-life lane.",
                userLikelyMistake = "Skipping the life detail and jumping to flirting.",
                intensity = InfluenceIntensity.LOW,
                risk = RiskLevel.LOW,
                fallback = "Ask one grounded sensory or routine question."
            )
            SyntheticScenarioCategory.READ_NO_REPLY -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("ME", "I sent a long thought."),
                    SyntheticTurn("SYSTEM", "read"),
                    SyntheticTurn("ME", "Are you there?")
                ),
                expectedDecisionType = TacticalDecisionType.WAIT,
                coCreationPoint = "Do not turn read status into pressure.",
                userLikelyMistake = "Chasing after a read receipt.",
                intensity = InfluenceIntensity.LOW,
                risk = RiskLevel.MEDIUM,
                fallback = "Wait; if needed later, send one calm practical check-in."
            )
            SyntheticScenarioCategory.USER_MULTI_SEND -> SyntheticRelationshipSample(
                id = "${category.id}-$index",
                category = category,
                messages = listOf(
                    SyntheticTurn("ME", "I may have said too much."),
                    SyntheticTurn("ME", "Forget that."),
                    SyntheticTurn("ME", "Actually I still want to explain.")
                ),
                expectedDecisionType = TacticalDecisionType.WAIT,
                coCreationPoint = "Stop adding pressure after user consecutive sends.",
                userLikelyMistake = "Stacking explanations to repair anxiety.",
                intensity = InfluenceIntensity.LOW,
                risk = RiskLevel.HIGH,
                fallback = "Pause until the other side replies."
            )
        }
    }
}
