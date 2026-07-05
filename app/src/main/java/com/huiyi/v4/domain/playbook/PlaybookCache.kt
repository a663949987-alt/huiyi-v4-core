package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.model.ReplyRoute

data class PlaybookCacheKey(
    val chatKey: String,
    val stage: RelationshipStage,
    val topicHash: String
)

class PlaybookCache {
    private val cache = linkedMapOf<PlaybookCacheKey, RelationshipPlaybook>()

    fun put(key: PlaybookCacheKey, playbook: RelationshipPlaybook) {
        cache[key] = playbook
    }

    fun get(key: PlaybookCacheKey, nowMillis: Long = System.currentTimeMillis()): RelationshipPlaybook? {
        val playbook = cache[key] ?: return null
        return if (playbook.expiresWhen.isExpired(nowMillis)) {
            cache.remove(key)
            null
        } else {
            playbook
        }
    }

    fun passiveNext(key: PlaybookCacheKey, nowMillis: Long = System.currentTimeMillis()): List<ReplyRoute> =
        get(key, nowMillis)?.passiveNext.orEmpty()

    fun activeExpression(key: PlaybookCacheKey, nowMillis: Long = System.currentTimeMillis()): List<ReplyRoute> =
        get(key, nowMillis)?.activeExpression.orEmpty()

    fun needsRefresh(
        key: PlaybookCacheKey,
        stage: RelationshipStage,
        topicHash: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val playbook = get(key, nowMillis) ?: return true
        return key.stage != stage || key.topicHash != topicHash || playbook.expiresWhen.isExpired(nowMillis)
    }

    fun clear() {
        cache.clear()
    }
}
