package com.huiyi.v4.domain.model

data class UserPersonaContext(
    val corpusId: String,
    val enabled: Boolean,
    val summary: String
)

data class UserPersonaCorpus(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val identityCards: List<IdentityCard>,
    val storyCards: List<StoryCard>,
    val styleRules: List<StyleRule>,
    val riskRules: List<RiskRule>
)

data class IdentityCard(
    val id: String,
    val title: String,
    val summary: String,
    val values: List<String>,
    val bestFor: List<String>,
    val avoidWhen: List<String>,
    val risk: String
)

data class StoryCard(
    val id: String,
    val title: String,
    val bestFor: List<String>,
    val expression: String,
    val risk: String
)

data class StyleRule(
    val id: String,
    val title: String,
    val description: String
)

data class RiskRule(
    val id: String,
    val title: String,
    val description: String
)

data class UserStorySelection(
    val shouldUseUserStory: Boolean,
    val selectedStoryCardIds: List<String>,
    val depth: StoryDepth,
    val risk: String?,
    val fallback: String?
)

enum class StoryDepth {
    LIGHT,
    MEDIUM,
    DEEP
}
