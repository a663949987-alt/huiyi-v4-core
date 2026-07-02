package com.huiyi.v4.domain.context

import com.huiyi.v4.domain.model.ChatSceneContext

class BackfillController(
    private val maxPages: Int = 3,
    private val enoughScore: Int = 75
) {
    fun shouldStop(context: ChatSceneContext, pagesRead: Int, userStopped: Boolean = false, rootAvailable: Boolean = true): Boolean {
        return userStopped ||
            !rootAvailable ||
            pagesRead >= maxPages ||
            context.contentCompleteness.score >= enoughScore ||
            context.allMessages.count { it.normalizedText?.isNotBlank() == true } >= 8
    }
}
