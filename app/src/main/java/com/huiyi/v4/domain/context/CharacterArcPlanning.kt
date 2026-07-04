package com.huiyi.v4.domain.context

import com.huiyi.v4.domain.model.CharacterArcCard
import com.huiyi.v4.domain.model.IdentityCard
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.StoryCard
import com.huiyi.v4.domain.model.UserPersonaCorpus

typealias LightMessage = LightChatMessageSummary

enum class ArcRevealDepth {
    LOW,
    MEDIUM
}

data class CurrentExpressionWindow(
    val exists: Boolean,
    val triggerTopics: List<String>,
    val lastOtherMessageId: String?,
    val reason: String,
    val nextMoveType: NextMoveType
)

data class ArcProgressState(
    val seenPersonaFacets: List<String>,
    val unseenPersonaFacets: List<String>,
    val recentlyExpressedFacets: List<String>,
    val currentExpressionWindow: CurrentExpressionWindow,
    val suggestedArcCard: CharacterArcCard?,
    val suggestedDepth: ArcRevealDepth,
    val overdoRisk: String?
)

data class ConversationStateCompression(
    val recentMessages: List<LightMessage>,
    val lastUserMessage: LightMessage?,
    val lastOtherMessage: LightMessage?,
    val currentTopics: List<String>,
    val seenPersonaFacets: List<String>,
    val recentlyExpressedFacets: List<String>,
    val expressionTriggerTopics: List<String>
)

class ConversationStateCompressor(
    private val maxMessages: Int = 12
) {
    fun compress(
        recentMessages: List<LightMessage>,
        lastUserMessage: LightMessage?,
        lastOtherMessage: LightMessage?,
        currentTopics: List<String>,
        personaCorpus: UserPersonaCorpus,
        characterArcCards: List<CharacterArcCard> = personaCorpus.characterArcCards
    ): ConversationStateCompression {
        val boundedMessages = recentMessages.takeLast(maxMessages.coerceIn(6, 12))
        val facets = personaFacets(personaCorpus, characterArcCards)
        val userMessages = boundedMessages.filter { it.speaker == Speaker.ME }
        val recentUserMessages = userMessages.takeLast(2)

        return ConversationStateCompression(
            recentMessages = boundedMessages,
            lastUserMessage = lastUserMessage,
            lastOtherMessage = lastOtherMessage,
            currentTopics = currentTopics.normalized(),
            seenPersonaFacets = matchingFacetIds(userMessages, facets),
            recentlyExpressedFacets = matchingFacetIds(recentUserMessages, facets),
            expressionTriggerTopics = expressionTriggerTopics(currentTopics, lastOtherMessage)
        )
    }

    private fun personaFacets(
        corpus: UserPersonaCorpus,
        characterArcCards: List<CharacterArcCard>
    ): List<PersonaFacet> {
        val identityFacets = corpus.identityCards.map { it.toFacet() }
        val storyFacets = corpus.storyCards.map { it.toFacet() }
        val arcOnlyFacets = characterArcCards
            .flatMap { it.relatedPersonaCardIds }
            .distinct()
            .filter { id ->
                (identityFacets + storyFacets).none { facet -> facet.id == id }
            }
            .map { id -> PersonaFacet(id = id, terms = listOf(id)) }
        return (identityFacets + storyFacets + arcOnlyFacets).distinctBy { it.id }
    }

    private fun IdentityCard.toFacet(): PersonaFacet = PersonaFacet(
        id = id,
        terms = listOf(id, title, summary) + values + bestFor
    )

    private fun StoryCard.toFacet(): PersonaFacet = PersonaFacet(
        id = id,
        terms = listOf(id, title, expression) + bestFor
    )

    private fun matchingFacetIds(
        messages: List<LightMessage>,
        facets: List<PersonaFacet>
    ): List<String> {
        val text = messages.joinToString(" ") { it.text.orEmpty() }.normalize()
        if (text.isBlank()) return emptyList()
        return facets
            .filter { facet ->
                facet.terms.any { term ->
                    val normalizedTerm = term.normalize()
                    normalizedTerm.isNotBlank() && text.contains(normalizedTerm)
                }
            }
            .map { it.id }
            .distinct()
    }

    private fun expressionTriggerTopics(
        currentTopics: List<String>,
        lastOtherMessage: LightMessage?
    ): List<String> {
        val topicText = currentTopics.joinToString(" ").normalize()
        val otherText = lastOtherMessage?.text.orEmpty().normalize()
        return expressionTopics
            .filter { topic ->
                val normalized = topic.normalize()
                topicText.contains(normalized) || otherText.contains(normalized)
            }
            .distinct()
    }

    private fun List<String>.normalized(): List<String> =
        map { it.trim().lowercase() }.filter { it.isNotBlank() }.distinct()
}

class CharacterArcPlanner(
    private val compressor: ConversationStateCompressor = ConversationStateCompressor()
) {
    fun plan(
        recentMessages: List<LightMessage>,
        lastUserMessage: LightMessage?,
        lastOtherMessage: LightMessage?,
        currentTopics: List<String>,
        personaCorpus: UserPersonaCorpus,
        characterArcCards: List<CharacterArcCard> = personaCorpus.characterArcCards
    ): ArcProgressState {
        val compression = compressor.compress(
            recentMessages = recentMessages,
            lastUserMessage = lastUserMessage,
            lastOtherMessage = lastOtherMessage,
            currentTopics = currentTopics,
            personaCorpus = personaCorpus,
            characterArcCards = characterArcCards
        )
        val allFacetIds = personaFacetIds(personaCorpus, characterArcCards)
        val unseen = allFacetIds.filterNot { it in compression.seenPersonaFacets }
        val suggestedCard = selectArcCard(characterArcCards, compression, unseen)
        val window = expressionWindow(compression, suggestedCard)
        val depth = suggestedDepth(window, suggestedCard, compression.recentlyExpressedFacets)

        return ArcProgressState(
            seenPersonaFacets = compression.seenPersonaFacets,
            unseenPersonaFacets = unseen,
            recentlyExpressedFacets = compression.recentlyExpressedFacets,
            currentExpressionWindow = window,
            suggestedArcCard = if (window.exists) suggestedCard else null,
            suggestedDepth = depth,
            overdoRisk = if (window.exists) suggestedCard?.overdoRisk ?: GENERIC_OVERDO_RISK else null
        )
    }

    private fun expressionWindow(
        compression: ConversationStateCompression,
        suggestedCard: CharacterArcCard?
    ): CurrentExpressionWindow {
        val lastOther = compression.lastOtherMessage
            ?: return CurrentExpressionWindow(
                exists = false,
                triggerTopics = emptyList(),
                lastOtherMessageId = null,
                reason = "no_last_other_message",
                nextMoveType = NextMoveType.WAIT
            )
        val lastUser = compression.lastUserMessage
        if (lastUser != null && lastUser.localSequence > lastOther.localSequence) {
            return CurrentExpressionWindow(
                exists = false,
                triggerTopics = emptyList(),
                lastOtherMessageId = lastOther.id,
                reason = "last_user_message_after_last_other",
                nextMoveType = NextMoveType.WAIT
            )
        }
        if (compression.expressionTriggerTopics.isEmpty()) {
            return CurrentExpressionWindow(
                exists = false,
                triggerTopics = emptyList(),
                lastOtherMessageId = lastOther.id,
                reason = "no_reality_planning_stability_future_trigger",
                nextMoveType = NextMoveType.RECEIVE_OTHER
            )
        }

        return CurrentExpressionWindow(
            exists = true,
            triggerTopics = compression.expressionTriggerTopics,
            lastOtherMessageId = lastOther.id,
            reason = "last_other_opens_self_expression_window",
            nextMoveType = if (suggestedCard != null) NextMoveType.ARC_REVEAL else NextMoveType.EXPRESS_SELF
        )
    }

    private fun selectArcCard(
        cards: List<CharacterArcCard>,
        compression: ConversationStateCompression,
        unseenFacetIds: List<String>
    ): CharacterArcCard? {
        if (cards.isEmpty()) return null
        val topicText = (compression.currentTopics + compression.expressionTriggerTopics).joinToString(" ").normalize()
        return cards.firstOrNull { card ->
            card.relatedPersonaCardIds.any { it in unseenFacetIds } &&
                (card.revealTrigger.normalize().hasOverlap(topicText) ||
                    topicText.hasOverlap(card.revealTrigger.normalize()))
        } ?: cards.firstOrNull { card ->
            card.relatedPersonaCardIds.any { it in unseenFacetIds }
        } ?: cards.first()
    }

    private fun suggestedDepth(
        window: CurrentExpressionWindow,
        card: CharacterArcCard?,
        recentlyExpressedFacets: List<String>
    ): ArcRevealDepth {
        if (!window.exists || card == null) return ArcRevealDepth.LOW
        val recentlyUsedSuggestedFacet = card.relatedPersonaCardIds.any { it in recentlyExpressedFacets }
        return if (recentlyUsedSuggestedFacet) ArcRevealDepth.LOW else ArcRevealDepth.MEDIUM
    }

    private fun personaFacetIds(
        corpus: UserPersonaCorpus,
        characterArcCards: List<CharacterArcCard>
    ): List<String> = (
        corpus.identityCards.map { it.id } +
            corpus.storyCards.map { it.id } +
            characterArcCards.flatMap { it.relatedPersonaCardIds }
        ).filter { it.isNotBlank() }.distinct()

    private fun String.hasOverlap(other: String): Boolean {
        if (isBlank() || other.isBlank()) return false
        return split(Regex("\\s+")).any { token -> token.length >= 3 && other.contains(token) }
    }
}

private data class PersonaFacet(
    val id: String,
    val terms: List<String>
)

private fun String.normalize(): String =
    lowercase().replace(Regex("\\s+"), " ").trim()

private const val GENERIC_OVERDO_RISK = "self_report_or_over_explaining"

private val expressionTopics = listOf(
    "planning",
    "reality",
    "stable",
    "stability",
    "future",
    "plan",
    "past",
    "experience",
    "responsibility",
    "responsible",
    "long term",
    "\u89c4\u5212",
    "\u73b0\u5b9e",
    "\u7a33\u5b9a",
    "\u672a\u6765",
    "\u4ee5\u540e",
    "\u5b89\u6392",
    "\u957f\u671f",
    "\u8fc7\u53bb",
    "\u7ecf\u5386",
    "\u8d23\u4efb",
    "\u8d23\u4efb\u611f"
)
