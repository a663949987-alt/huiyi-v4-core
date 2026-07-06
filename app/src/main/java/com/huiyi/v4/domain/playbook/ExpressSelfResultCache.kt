package com.huiyi.v4.domain.playbook

data class ExpressSelfResultCacheKey(
    val chatKey: String,
    val snapshotHash: String,
    val expressionWindowHash: String,
    val selectedThemeId: String,
    val eligibilityMode: String
) {
    fun sameSceneKey(): ExpressSelfSameSceneKey = ExpressSelfSameSceneKey(
        chatKey = chatKey,
        snapshotHash = snapshotHash,
        expressionWindowHash = expressionWindowHash,
        selectedThemeId = selectedThemeId
    )

    companion object {
        fun from(
            request: DynamicPlaybookRequest,
            playbook: RelationshipPlaybook,
            eligibilityMode: String?
        ): ExpressSelfResultCacheKey {
            val selection = playbook.expressionModeSelection
            val expressionTopics = (playbook.characterArcPlan.triggerTopics + playbook.currentFrame)
                .joinToString("|")
                .ifBlank { "ordinary" }
            return ExpressSelfResultCacheKey(
                chatKey = playbook.chatKey.orEmpty().ifBlank {
                    "${request.appPackage.orEmpty()}|${request.windowTitle.orEmpty()}"
                },
                snapshotHash = request.chatWindowHash.orEmpty().ifBlank {
                    request.messages.joinToString("|") { "${it.speaker}:${it.normalizedText.orEmpty()}" }.hashCode().toString()
                },
                expressionWindowHash = expressionTopics.hashCode().toString(),
                selectedThemeId = selection?.selectedTheme?.themeId ?: "default_theme",
                eligibilityMode = eligibilityMode ?: "ANY"
            )
        }
    }
}

data class ExpressSelfSameSceneKey(
    val chatKey: String,
    val snapshotHash: String,
    val expressionWindowHash: String,
    val selectedThemeId: String
)

data class ExpressSelfResultCacheEntry(
    val key: ExpressSelfResultCacheKey,
    val result: DynamicPlaybookResult,
    val savedAtMillis: Long,
    val hitCount: Int = 0
) {
    val repeatClickCount: Int get() = hitCount + 1
}

class ExpressSelfResultCache(
    private val ttlMillis: Long = 10 * 60 * 1000L
) {
    private val entries = linkedMapOf<ExpressSelfResultCacheKey, ExpressSelfResultCacheEntry>()

    fun put(key: ExpressSelfResultCacheKey, result: DynamicPlaybookResult, nowMillis: Long) {
        entries[key] = ExpressSelfResultCacheEntry(
            key = key,
            result = result,
            savedAtMillis = nowMillis
        )
    }

    fun getSameScene(key: ExpressSelfResultCacheKey, nowMillis: Long): ExpressSelfResultCacheEntry? {
        val sceneKey = key.sameSceneKey()
        val match = entries.entries.firstOrNull { (_, entry) ->
            entry.key.sameSceneKey() == sceneKey && nowMillis - entry.savedAtMillis in 0..ttlMillis
        } ?: return null
        val updated = match.value.copy(hitCount = match.value.hitCount + 1)
        entries[match.key] = updated
        return updated
    }

    fun clear() {
        entries.clear()
    }
}
