package com.huiyi.v4.domain.playbook

import com.huiyi.v4.domain.context.NextMoveType
import com.huiyi.v4.domain.model.DescriptionStatus
import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.MessageContent
import com.huiyi.v4.domain.model.MessageNode
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TranscriptStatus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class CloudRelationshipPlaybookMapper {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun buildInput(
        request: DynamicPlaybookRequest,
        localPlaybook: RelationshipPlaybook,
        lastSpeaker: Speaker?
    ): NormalizedConversationJson {
        val messages = request.messages
            .filter { it.isEffectiveChatMessage && it.speaker in setOf(Speaker.ME, Speaker.OTHER) }
            .sortedWith(compareBy<MessageNode> { it.finalVisualOrder ?: Int.MAX_VALUE }.thenBy { it.localSequence })
            .takeLast(8)
        val payload = buildJsonObject {
            put("schemaVersion", "HuiyiRelationshipPlaybookInput-v1")
            put("sessionId", request.sessionId.orEmpty())
            put("mode", request.mode.name)
            put("appPackage", request.appPackage.orEmpty())
            put("windowTitle", request.windowTitle.orEmpty())
            put("chatWindowHash", request.chatWindowHash.orEmpty())
            put("lastSpeaker", lastSpeaker?.name ?: "UNKNOWN")
            put("expectedOutput", "RelationshipPlaybook")
            put("conversation", buildJsonObject {
                put("messages", buildJsonArray {
                    messages.forEach { message ->
                        add(
                            buildJsonObject {
                                put("id", message.id)
                                put("speaker", message.speaker.name)
                                put("text", message.textForPlaybook())
                                put("contentType", message.content::class.simpleName.orEmpty())
                                put("source", message.source.name)
                                put("visualOrder", message.finalVisualOrder ?: message.localSequence.toInt())
                            }
                        )
                    }
                })
                put("lastOtherMessage", messages.lastOrNull { it.speaker == Speaker.OTHER }?.textForPlaybook().orEmpty())
                put("lastUserMessage", messages.lastOrNull { it.speaker == Speaker.ME }?.textForPlaybook().orEmpty())
            })
            put("localPlaybook", buildJsonObject {
                put("stage", localPlaybook.stage.name)
                put("currentFrame", localPlaybook.currentFrame)
                put("risk", localPlaybook.risk.name)
                put("fallback", localPlaybook.fallback)
                put("passiveRouteCount", localPlaybook.passiveNext.size)
                put("activeRouteCount", localPlaybook.activeExpression.size)
                put("activeHasArcReveal", localPlaybook.activeExpression.any { it.routeType == ReplyRouteType.ARC_REVEAL })
                put("arcPlan", buildJsonObject {
                    put("exists", localPlaybook.characterArcPlan.exists)
                    put("nextMoveType", localPlaybook.characterArcPlan.nextMoveType.name)
                    put("suggestedFacet", localPlaybook.characterArcPlan.suggestedFacet.orEmpty())
                    put("suggestedLine", localPlaybook.characterArcPlan.suggestedLine.orEmpty())
                    put("overdoRisk", localPlaybook.characterArcPlan.overdoRisk.orEmpty())
                })
            })
            put("requirements", buildJsonObject {
                put("passiveNextCount", "3-5")
                put("activeExpressionCount", "3-5")
                put("routeMessagesLanguage", "Chinese")
                put("copyableDirectly", true)
                put("noRawPrivateChatStorage", true)
                put("mustIncludeArcRevealWhenPlanningRealityStabilityFuture", true)
            })
        }
        return NormalizedConversationJson(payload.toString())
    }

    fun parseResponse(
        responseBody: String,
        localPlaybook: RelationshipPlaybook,
        nowMillis: Long = System.currentTimeMillis()
    ): RelationshipPlaybook {
        val root = runCatching { extractPlaybookJson(responseBody) }
            .getOrElse { throw IllegalStateException("CLOUD_SCHEMA_INVALID", it) }
        validateRequiredFields(root)

        val passiveRoutes = root.array("passiveNext")
            .mapIndexed { index, element -> element.toRoute(index, "passive") }
            .map { route ->
                if (route.routeType in activeOnlyRouteTypes) route.copy(routeType = ReplyRouteType.STABLE) else route
            }
            .take(5)
            .mapIndexed { index, route -> route.copy(recommended = index == 0) }
        validateRoutes(passiveRoutes, "passiveNext")

        val cloudActiveRoutes = root.array("activeExpression")
            .mapIndexed { index, element -> element.toRoute(index, "active") }
            .take(5)
        val activeRoutes = ensureActiveArcRoutes(cloudActiveRoutes, localPlaybook.activeExpression)
            .take(5)
            .mapIndexed { index, route -> route.copy(recommended = index == 0) }
        validateRoutes(activeRoutes, "activeExpression")

        val arcPlan = root["characterArcPlan"]?.jsonObjectOrNull()?.toArcPlan(localPlaybook.characterArcPlan)
            ?: localPlaybook.characterArcPlan

        return localPlaybook.copy(
            stage = root.string("stage").toStage(localPlaybook.stage),
            currentFrame = root.string("currentFrame").ifBlank { localPlaybook.currentFrame },
            passiveNext = passiveRoutes,
            activeExpression = activeRoutes,
            characterArcPlan = arcPlan,
            risk = root.string("risk").toRisk(localPlaybook.risk),
            fallback = root.string("fallback").ifBlank { localPlaybook.fallback },
            generatedAtMillis = nowMillis,
            source = RelationshipPlaybookSource.CLOUD_ENHANCED
        )
    }

    fun extractPlaybookJson(responseBody: String): JsonObject {
        val parsed = json.parseToJsonElement(responseBody).jsonObject
        val completionContent = parsed["choices"]?.jsonArrayOrNull()
            ?.firstOrNull()
            ?.jsonObjectOrNull()
            ?.get("message")
            ?.jsonObjectOrNull()
            ?.get("content")
            ?.jsonPrimitiveOrNull()
            ?.contentOrNull
        val content = completionContent?.takeIf { it.isNotBlank() } ?: responseBody
        return json.parseToJsonElement(stripJsonFence(content).extractJsonObjectText()).jsonObject.unwrapPlaybookRoot()
    }

    private fun JsonObject.unwrapPlaybookRoot(): JsonObject {
        if (containsPlaybookFields()) return this
        val nestedKeys = listOf(
            "playbook",
            "relationshipPlaybook",
            "RelationshipPlaybook",
            "data",
            "result",
            "output"
        )
        nestedKeys.forEach { key ->
            val nestedObject = this[key]?.jsonObjectOrNull()
            if (nestedObject != null) return nestedObject.unwrapPlaybookRoot()
            val nestedString = this[key]?.jsonPrimitiveOrNull()?.contentOrNull
            if (!nestedString.isNullOrBlank() && nestedString.trim().startsWith("{")) {
                return json.parseToJsonElement(nestedString.extractJsonObjectText()).jsonObject.unwrapPlaybookRoot()
            }
        }
        return this
    }

    private fun JsonObject.containsPlaybookFields(): Boolean =
        this["passiveNext"] != null || this["activeExpression"] != null || requiredFields.all { this[it] != null }

    private fun validateRequiredFields(root: JsonObject) {
        val missing = requiredFields.filter { root[it] == null }
        require(missing.isEmpty()) { "CLOUD_SCHEMA_INVALID:${missing.joinToString("|")}" }
    }

    private fun validateRoutes(routes: List<ReplyRoute>, name: String) {
        require(routes.size in 3..5) { "CLOUD_ROUTE_COUNT_INVALID:$name" }
        val messages = routes.map { it.message.trim() }
        require(messages.all { it.isNotBlank() }) { "CLOUD_ROUTE_EMPTY:$name" }
        require(messages.distinct().size == messages.size) { "CLOUD_ROUTE_DUPLICATE:$name" }
        require(messages.all { it.contains(cjkRegex) }) { "CLOUD_ROUTE_NOT_CHINESE:$name" }
    }

    private fun ensureActiveArcRoutes(
        cloudActiveRoutes: List<ReplyRoute>,
        localActiveRoutes: List<ReplyRoute>
    ): List<ReplyRoute> {
        val cloudWithArc = if (cloudActiveRoutes.any { it.routeType == ReplyRouteType.ARC_REVEAL }) {
            cloudActiveRoutes
        } else {
            val localArc = localActiveRoutes.firstOrNull { it.routeType == ReplyRouteType.ARC_REVEAL }
            if (localArc == null) cloudActiveRoutes else listOf(localArc.copy(tag = "cloud-playbook-refresh-local-arc")) + cloudActiveRoutes
        }
        val cloudWithCoCreate = if (cloudWithArc.any { it.routeType == ReplyRouteType.CO_CREATION }) {
            cloudWithArc
        } else {
            val localCoCreate = localActiveRoutes.firstOrNull { it.routeType == ReplyRouteType.CO_CREATION }
            if (localCoCreate == null) cloudWithArc else cloudWithArc + localCoCreate.copy(tag = "cloud-playbook-refresh-local-cocreate")
        }
        return cloudWithCoCreate.distinctBy { it.message.trim() }
    }

    private fun JsonElement.toRoute(index: Int, tag: String): ReplyRoute {
        val obj = jsonObject
        val typeText = firstNonBlank(
            obj.string("routeFamily"),
            obj.string("routeType"),
            obj.string("type"),
            obj.string("family"),
            obj.string("slot"),
            obj.string("name")
        )
        val routeType = typeText.toRouteType(index, tag)
        val risk = obj.string("riskLevel").toRisk(RiskLevel.LOW)
        val message = firstNonBlank(obj.string("message"), obj.string("text"), obj.string("reply")).trim()
        return ReplyRoute(
            id = obj.string("id").ifBlank { "cloud-playbook-$tag-${index + 1}" },
            name = firstNonBlank(obj.string("slot"), obj.string("name"), routeType.defaultLabel()),
            routeType = routeType,
            tag = "cloud-playbook-refresh",
            message = message,
            intensity = obj.string("intensity").toIntensity(risk),
            riskLevel = risk,
            riskWarning = obj.string("riskWarning").ifBlank { null },
            expectedEffect = firstNonBlank(obj.string("why"), obj.string("reason")),
            fallbackMove = obj.string("fallbackMove").ifBlank { "如果对方没接住，就先降压收口。" },
            recommended = index == 0,
            routeSource = HuiyiOutputQualityGate.SOURCE_CLOUD_ENHANCED_PLAYBOOK,
            generatorName = "CloudRelationshipPlaybookMapper",
            promptVersion = "relationship-playbook-cloud-v1",
            cacheSource = "CLOUD_REFRESH"
        )
    }

    private fun JsonObject.toArcPlan(local: PlaybookCharacterArcPlan): PlaybookCharacterArcPlan {
        val exists = boolean("exists") ?: boolean("suitable") ?: boolean("arcRevealSuitable") ?: local.exists
        val nextMove = string("nextMoveType").ifBlank {
            if (exists) NextMoveType.ARC_REVEAL.name else local.nextMoveType.name
        }.toNextMoveType(local.nextMoveType)
        return local.copy(
            exists = exists,
            nextMoveType = nextMove,
            suggestedFacet = firstNonBlank(
                string("suggestedFacet"),
                string("facet"),
                string("hiddenDepth"),
                local.suggestedFacet.orEmpty()
            ),
            suggestedLine = firstNonBlank(
                string("suggestedLine"),
                string("safeRevealLine"),
                local.suggestedLine.orEmpty()
            ),
            overdoRisk = firstNonBlank(string("overdoRisk"), local.overdoRisk.orEmpty()),
            triggerTopics = stringList("triggerTopics").ifEmpty { local.triggerTopics }
        )
    }

    private fun MessageNode.textForPlaybook(): String = when (val value = content) {
        is MessageContent.Text -> value.text
        is MessageContent.Voice -> when (value.transcriptStatus) {
            TranscriptStatus.APP_TRANSCRIBED,
            TranscriptStatus.ASR_TRANSCRIBED -> value.transcriptText.orEmpty()
            TranscriptStatus.USER_SUMMARY -> value.userSummary.orEmpty()
            TranscriptStatus.MISSING -> "[语音]"
        }
        is MessageContent.Image -> when (value.descriptionStatus) {
            DescriptionStatus.MODEL_DESCRIBED,
            DescriptionStatus.USER_SUMMARY -> value.descriptionText.orEmpty()
            DescriptionStatus.MISSING -> "[图片]"
        }
        is MessageContent.Video -> when (value.descriptionStatus) {
            DescriptionStatus.MODEL_DESCRIBED,
            DescriptionStatus.USER_SUMMARY -> value.descriptionText.orEmpty()
            DescriptionStatus.MISSING -> "[视频]"
        }
        is MessageContent.Sticker -> when (value.meaningStatus) {
            DescriptionStatus.MODEL_DESCRIBED,
            DescriptionStatus.USER_SUMMARY -> value.meaningText.orEmpty()
            DescriptionStatus.MISSING -> "[表情]"
        }
    }.ifBlank { normalizedText.orEmpty() }

    private fun String.extractJsonObjectText(): String {
        val trimmed = trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return trimmed
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        require(start >= 0 && end > start) { "json_object_not_found" }
        return trimmed.substring(start, end + 1)
    }

    private fun stripJsonFence(content: String): String {
        val trimmed = content.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun JsonObject.array(name: String): JsonArray =
        this[name]?.jsonArrayOrNull() ?: JsonArray(emptyList())

    private fun JsonObject.string(name: String): String =
        this[name]?.jsonPrimitiveOrNull()?.contentOrNull.orEmpty()

    private fun JsonObject.boolean(name: String): Boolean? =
        this[name]?.jsonPrimitiveOrNull()?.contentOrNull?.toBooleanStrictOrNull()

    private fun JsonObject.stringList(name: String): List<String> =
        this[name]?.jsonArrayOrNull()?.mapNotNull { it.jsonPrimitiveOrNull()?.contentOrNull } ?: emptyList()

    private fun JsonElement.jsonArrayOrNull(): JsonArray? = runCatching { jsonArray }.getOrNull()
    private fun JsonElement.jsonObjectOrNull(): JsonObject? = runCatching { jsonObject }.getOrNull()
    private fun JsonElement.jsonPrimitiveOrNull() = runCatching { jsonPrimitive }.getOrNull()

    private fun String.toRouteType(index: Int, tag: String): ReplyRouteType {
        val value = uppercase()
        return when {
            value.contains("ARC") || value.contains("人物弧光") -> ReplyRouteType.ARC_REVEAL
            value.contains("SELF") || value.contains("表达") || value.contains("底色") -> ReplyRouteType.SELF_STORY
            value.contains("CO_CREATE") || value.contains("CO-CREATE") || value.contains("共创") || value.contains("升维") -> ReplyRouteType.CO_CREATION
            value.contains("WITHDRAW") || value.contains("COOL") || value.contains("撤退") || value.contains("收口") -> ReplyRouteType.COOL_DOWN
            value.contains("WARM") || value.contains("升温") -> ReplyRouteType.WARM_UP
            value.contains("QUESTION") || value.contains("DIRECT") || value.contains("轻问") -> ReplyRouteType.DIRECT
            value.contains("REPAIR") || value.contains("修复") -> ReplyRouteType.REPAIR
            value.contains("STABLE") || value.contains("稳") -> ReplyRouteType.STABLE
            value.contains("EMPATHY") || value.contains("RECEIVE") || value.contains("接") -> ReplyRouteType.EMPATHY
            tag == "active" && index == 2 -> ReplyRouteType.ARC_REVEAL
            tag == "active" && index == 3 -> ReplyRouteType.CO_CREATION
            else -> ReplyRouteType.STABLE
        }
    }

    private fun String.toRisk(default: RiskLevel): RiskLevel = when (uppercase()) {
        "HIGH" -> RiskLevel.HIGH
        "MEDIUM" -> RiskLevel.MEDIUM
        "LOW" -> RiskLevel.LOW
        else -> default
    }

    private fun String.toIntensity(risk: RiskLevel): InfluenceIntensity = when (uppercase()) {
        "HIGH" -> InfluenceIntensity.HIGH
        "MEDIUM" -> InfluenceIntensity.MEDIUM
        "LOW" -> InfluenceIntensity.LOW
        else -> if (risk == RiskLevel.HIGH) InfluenceIntensity.MEDIUM else InfluenceIntensity.LOW
    }

    private fun String.toStage(default: RelationshipStage): RelationshipStage =
        RelationshipStage.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: default

    private fun String.toNextMoveType(default: NextMoveType): NextMoveType =
        NextMoveType.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: default

    private fun ReplyRouteType.defaultLabel(): String = when (this) {
        ReplyRouteType.STABLE -> "稳住节奏"
        ReplyRouteType.EMPATHY -> "接住情绪"
        ReplyRouteType.CO_CREATION -> "共创升维"
        ReplyRouteType.COOL_DOWN -> "低压撤退"
        ReplyRouteType.WARM_UP -> "轻微升温"
        ReplyRouteType.SELF_STORY -> "表达我"
        ReplyRouteType.ARC_REVEAL -> "人物弧光"
        ReplyRouteType.REPAIR -> "修复关系"
        ReplyRouteType.WAIT -> "等待"
        ReplyRouteType.DIRECT -> "轻问一句"
    }

    private fun firstNonBlank(vararg values: String): String =
        values.firstOrNull { it.isNotBlank() }.orEmpty()

    private companion object {
        val requiredFields = listOf(
            "stage",
            "currentFrame",
            "passiveNext",
            "activeExpression",
            "characterArcPlan",
            "next2StepBranches",
            "risk",
            "fallback",
            "expiresWhen"
        )
        val cjkRegex = Regex("[\\u4e00-\\u9fff]")
        val activeOnlyRouteTypes = setOf(ReplyRouteType.ARC_REVEAL, ReplyRouteType.SELF_STORY, ReplyRouteType.CO_CREATION)
    }
}
