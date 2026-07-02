package com.huiyi.v4.domain.pipeline

import com.huiyi.v4.domain.model.ReplyAttempt
import com.huiyi.v4.domain.model.ReplyAttemptStatus
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.UserAction
import java.util.UUID

class ReplyAttemptFactory {
    fun copied(route: ReplyRoute, sceneId: String, contactId: String?, now: Long = System.currentTimeMillis()): ReplyAttempt {
        return ReplyAttempt(
            id = UUID.randomUUID().toString(),
            contactId = contactId,
            sceneId = sceneId,
            routeId = route.id,
            routeType = route.routeType,
            suggestedText = route.message,
            userAction = UserAction.COPIED,
            status = ReplyAttemptStatus.PENDING,
            selectedAt = now,
            confirmedSentAt = null,
            finalSentText = null
        )
    }
}
