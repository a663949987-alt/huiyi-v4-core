package com.huiyi.v4.domain.modelprovider

import com.huiyi.v4.domain.tactical.ReplyRouteGenerator

class FakeModelProvider(
    private val routeGenerator: ReplyRouteGenerator = ReplyRouteGenerator()
) : TacticalModelProvider {
    override suspend fun generateTacticalReply(input: TacticalPromptInput): TacticalReplyResult {
        return TacticalReplyResult(
            decision = input.decision,
            routes = routeGenerator.generate(input.context, input.decision),
            providerName = "FakeModelProvider",
            apiCalled = false
        )
    }
}
