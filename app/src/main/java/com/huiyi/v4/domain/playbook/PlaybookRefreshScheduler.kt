package com.huiyi.v4.domain.playbook

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class PlaybookRefreshTrigger {
    NEW_CHAT_PAGE,
    MESSAGES_CHANGED,
    THREE_TO_FIVE_NEW_MESSAGES,
    TOPIC_CHANGED,
    ARC_TRIGGER_TOPIC,
    EXPRESS_SELF_CLICK,
    THIS_WRONG_FEEDBACK,
    PLAYBOOK_EXPIRED,
    VALIDATOR_FAIL,
    CACHE_MISSING
}

data class PlaybookRefreshOutcome(
    val refreshStarted: Boolean,
    val localFallbackReady: Boolean,
    val cloudAttempted: Boolean,
    val cloudSuccess: Boolean,
    val cacheReplaced: Boolean,
    val staleRefreshDiscarded: Boolean,
    val discardedReason: String?,
    val triggers: List<PlaybookRefreshTrigger>,
    val cacheKey: PlaybookCacheKey?,
    val cloudModelTrace: CloudModelTrace = CloudModelTrace.local()
)

fun interface CloudPlaybookRefresher {
    suspend fun refresh(request: DynamicPlaybookRequest, localPlaybook: RelationshipPlaybook): Result<RelationshipPlaybook>
}

class PlaybookRefreshScheduler(
    private val engine: DynamicPlaybookEngine,
    private val cloudRefresher: CloudPlaybookRefresher? = null
) {
    fun refreshInBackground(
        scope: CoroutineScope,
        request: DynamicPlaybookRequest,
        currentChatKeyProvider: () -> String?
    ): Job = scope.launch {
        refreshNow(request, currentChatKeyProvider)
    }

    suspend fun refreshNow(
        request: DynamicPlaybookRequest,
        currentChatKeyProvider: () -> String? = { null }
    ): PlaybookRefreshOutcome {
        val local = when (request.mode) {
            DynamicPlaybookMode.NEXT_SENTENCE -> engine.nextSentence(request)
            DynamicPlaybookMode.EXPRESS_SELF -> engine.expressSelf(request)
        }
        val refresher = cloudRefresher
            ?: return PlaybookRefreshOutcome(
                refreshStarted = true,
                localFallbackReady = true,
                cloudAttempted = false,
                cloudSuccess = false,
                cacheReplaced = false,
                staleRefreshDiscarded = false,
                discardedReason = null,
                triggers = local.refreshTriggers,
                cacheKey = local.cacheKey,
                cloudModelTrace = CloudModelTrace.local("NO_CLOUD_REFRESHER")
            )

        val cloud = refresher.refresh(request, local.playbook)
        val cloudPlaybook = cloud.getOrNull()
        if (cloudPlaybook == null) {
            val error = cloud.exceptionOrNull()
            val trace = (error as? CloudPlaybookRefreshException)?.modelTrace
                ?: CloudModelTrace.local(error?.message ?: "CLOUD_REFRESH_FAILED")
            return PlaybookRefreshOutcome(
                refreshStarted = true,
                localFallbackReady = true,
                cloudAttempted = true,
                cloudSuccess = false,
                cacheReplaced = false,
                staleRefreshDiscarded = false,
                discardedReason = error?.message,
                triggers = local.refreshTriggers,
                cacheKey = local.cacheKey,
                cloudModelTrace = trace
            )
        }
        val trace = cloudPlaybook.cloudModelTrace
        val currentChatKey = currentChatKeyProvider() ?: local.snapshot.chatKey
        if (cloudPlaybook.chatKey != null && cloudPlaybook.chatKey != currentChatKey) {
            return PlaybookRefreshOutcome(
                refreshStarted = true,
                localFallbackReady = true,
                cloudAttempted = true,
                cloudSuccess = true,
                cacheReplaced = false,
                staleRefreshDiscarded = true,
                discardedReason = "CHAT_KEY_CHANGED",
                triggers = local.refreshTriggers,
                cacheKey = local.cacheKey,
                cloudModelTrace = trace.blocked("CHAT_KEY_CHANGED")
            )
        }
        if (!trace.playbookCacheWriteAllowed) {
            return PlaybookRefreshOutcome(
                refreshStarted = true,
                localFallbackReady = true,
                cloudAttempted = true,
                cloudSuccess = false,
                cacheReplaced = false,
                staleRefreshDiscarded = false,
                discardedReason = trace.playbookCacheWriteBlockedReason.ifBlank { "CACHE_WRITE_BLOCKED" },
                triggers = local.refreshTriggers,
                cacheKey = local.cacheKey,
                cloudModelTrace = trace
            )
        }
        val key = engine.cache(cloudPlaybook.copy(source = RelationshipPlaybookSource.CLOUD_ENHANCED))
        return PlaybookRefreshOutcome(
            refreshStarted = true,
            localFallbackReady = true,
            cloudAttempted = true,
            cloudSuccess = true,
            cacheReplaced = true,
            staleRefreshDiscarded = false,
            discardedReason = null,
            triggers = local.refreshTriggers,
            cacheKey = key,
            cloudModelTrace = trace
        )
    }
}
