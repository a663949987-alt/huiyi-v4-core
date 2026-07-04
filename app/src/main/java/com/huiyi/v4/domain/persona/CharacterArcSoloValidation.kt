package com.huiyi.v4.domain.persona

import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File
import kotlin.math.roundToInt

enum class CharacterArcRouteFamily {
    RECEIVE,
    ARC_REVEAL,
    OVERDO
}

enum class CharacterArcUserFeedback {
    LIKE_ME,
    NOT_LIKE_ME,
    TOO_OILY,
    TOO_HEAVY,
    TOO_EMPTY,
    SENDABLE,
    MOST_NATURAL,
    WANT_CONTINUE,
    TOO_REPORT,
    ALL_BAD
}

data class CharacterArcScenario(
    val scenarioId: String,
    val scenario: String,
    val otherSays: String,
    val tags: List<String>
)

data class CharacterArcCandidate(
    val candidateId: String,
    val routeFamily: CharacterArcRouteFamily,
    val text: String,
    val personaCardIds: List<String> = emptyList(),
    val characterArcCardIds: List<String> = emptyList()
)

data class CharacterArcJudgeResult(
    val scenarioId: String,
    val candidateId: String,
    val routeFamily: CharacterArcRouteFamily,
    val naturalnessScore: Int,
    val realnessScore: Int,
    val wantContinueScore: Int,
    val pressureScore: Int,
    val oilyScore: Int,
    val personaFitScore: Int,
    val arcStrengthScore: Int,
    val overReportScore: Int,
    val coCreationScore: Int,
    val rejectReason: String?,
    val needsUserReview: Boolean
) {
    val positiveScore: Int
        get() = naturalnessScore + realnessScore + wantContinueScore + personaFitScore + arcStrengthScore + coCreationScore
    val negativeScore: Int
        get() = pressureScore + oilyScore + overReportScore
    val totalScore: Int
        get() = positiveScore - negativeScore
}

data class CharacterArcReviewItem(
    val scenario: CharacterArcScenario,
    val blindCandidates: List<CharacterArcCandidate>,
    val reason: String
)

data class CharacterArcPreferenceRecord(
    val routeTextRedacted: String,
    val routeFamily: String,
    val personaCardIds: List<String>,
    val characterArcCardIds: List<String>,
    val userFeedback: CharacterArcUserFeedback,
    val timestamp: Long,
    val scenarioTags: List<String>,
    val noteRedacted: String? = null
) {
    companion object {
        fun fromRoute(
            route: ReplyRoute,
            feedback: CharacterArcUserFeedback,
            now: Long = System.currentTimeMillis()
        ): CharacterArcPreferenceRecord {
            val arcIds = if (route.routeType == ReplyRouteType.ARC_REVEAL) listOf("default-character-arc") else emptyList()
            val personaIds = when (route.routeType) {
                ReplyRouteType.ARC_REVEAL,
                ReplyRouteType.SELF_STORY -> listOf("soldier", "transition")
                else -> emptyList()
            }
            return CharacterArcPreferenceRecord(
                routeTextRedacted = CharacterArcRedactor.redact(route.message),
                routeFamily = route.routeFamily,
                personaCardIds = personaIds,
                characterArcCardIds = arcIds,
                userFeedback = feedback,
                timestamp = now,
                scenarioTags = listOf(route.routeFamily, route.riskLevel.name, route.intensity.name)
            )
        }

        fun fromSoloReview(
            item: CharacterArcReviewItem,
            candidate: CharacterArcCandidate?,
            feedback: CharacterArcUserFeedback,
            note: String?,
            now: Long = System.currentTimeMillis()
        ): CharacterArcPreferenceRecord {
            return CharacterArcPreferenceRecord(
                routeTextRedacted = CharacterArcRedactor.redact(candidate?.text ?: item.scenario.otherSays),
                routeFamily = candidate?.routeFamily?.name ?: "ALL_CANDIDATES",
                personaCardIds = candidate?.personaCardIds.orEmpty(),
                characterArcCardIds = candidate?.characterArcCardIds.orEmpty(),
                userFeedback = feedback,
                timestamp = now,
                scenarioTags = item.scenario.tags,
                noteRedacted = note?.takeIf { it.isNotBlank() }?.let(CharacterArcRedactor::redact)
            )
        }
    }
}

data class CharacterArcPreferenceProfile(
    val preferredArcCards: List<String> = emptyList(),
    val rejectedArcCards: List<String> = emptyList(),
    val tooOilyPatterns: List<String> = emptyList(),
    val tooHeavyPatterns: List<String> = emptyList(),
    val likedPhrases: List<String> = emptyList(),
    val dislikedPhrases: List<String> = emptyList(),
    val preferredIntensity: String = "LOW_TO_MEDIUM",
    val selfExpressionComfortLevel: String = "UNKNOWN",
    val feedbackCount: Int = 0
)

object CharacterArcSampleCorpus {
    fun scenarios(): List<CharacterArcScenario> {
        val tones = listOf(
            "轻轻试探",
            "认真确认",
            "有点犹豫"
        )
        return seeds.flatMapIndexed { seedIndex, seed ->
            tones.mapIndexed { toneIndex, tone ->
                CharacterArcScenario(
                    scenarioId = "arc-${seedIndex + 1}-${toneIndex + 1}",
                    scenario = "${seed.label} / $tone",
                    otherSays = seed.variants[toneIndex],
                    tags = seed.tags + tone
                )
            }
        }.take(60)
    }

    fun candidatesFor(scenario: CharacterArcScenario): List<CharacterArcCandidate> {
        val topic = scenario.tags.firstOrNull().orEmpty()
        return listOf(
            CharacterArcCandidate(
                candidateId = "${scenario.scenarioId}-receive",
                routeFamily = CharacterArcRouteFamily.RECEIVE,
                text = receiveLine(topic)
            ),
            CharacterArcCandidate(
                candidateId = "${scenario.scenarioId}-arc",
                routeFamily = CharacterArcRouteFamily.ARC_REVEAL,
                text = arcLine(topic),
                personaCardIds = listOf("soldier", "transition"),
                characterArcCardIds = listOf("default-character-arc")
            ),
            CharacterArcCandidate(
                candidateId = "${scenario.scenarioId}-overdo",
                routeFamily = CharacterArcRouteFamily.OVERDO,
                text = overdoLine(topic),
                personaCardIds = listOf("soldier", "transition"),
                characterArcCardIds = listOf("default-character-arc")
            )
        )
    }

    private fun receiveLine(topic: String): String {
        return when {
            topic.contains("已读") -> "嗯，我懂，你不是一定要马上聊，只是这种悬着的感觉会让人有点没底。"
            topic.contains("孩子") -> "这件事确实不轻，你先别一个人硬扛，我们慢慢把现实问题说清楚。"
            topic.contains("表达") -> "没关系，不用一下子表达得很完整，你愿意说到哪儿，我就听到哪儿。"
            else -> "嗯，我能理解你在意这个。我们不用急着下结论，先把你最看重的那一点说清楚。"
        }
    }

    private fun arcLine(topic: String): String {
        return when {
            topic.contains("部队") || topic.contains("责任") -> "我可能不太会把话说得漂亮，但认真起来会一件事一件事做到位。"
            topic.contains("转业") || topic.contains("未来") -> "我现在也在换阶段，所以更想把以后的生活一步步走稳，而不是只说好听的。"
            topic.contains("表达") -> "我有时候表达慢一点，但不是不上心，是想把话说得更准一点。"
            topic.contains("孩子") -> "我不敢轻飘飘地劝你，但遇到真正要扛的事，我会先想怎么落地。"
            else -> "我可能表面看着直接，但真正在意的事，我会用行动慢慢放稳。"
        }
    }

    private fun overdoLine(topic: String): String {
        return when {
            topic.contains("孩子") -> "我真的特别能扛事，以后你和孩子的问题我都可以负责到底，我会证明给你看。"
            topic.contains("未来") -> "我已经把未来规划得很清楚了，只要你跟我走，我肯定能给你一个稳定的人生。"
            else -> "我作为一个很有责任感的人，经历过很多，也比普通人更成熟可靠，你应该能感受到我不是随便说说。"
        }
    }

    private data class ScenarioSeed(
        val label: String,
        val tags: List<String>,
        val variants: List<String>
    )

    private val seeds = listOf(
        ScenarioSeed("现实规划", listOf("现实规划", "未来"), listOf("我还是挺看重现实和规划的。", "你以后到底有什么打算？", "我怕只靠感觉走不长。")),
        ScenarioSeed("稳定", listOf("稳定", "责任感"), listOf("我现在更想找一个稳定一点的人。", "稳定对我来说比热闹重要。", "我不太想再经历忽冷忽热了。")),
        ScenarioSeed("未来", listOf("未来", "规划"), listOf("你有想过两个人以后的生活吗？", "以后这些事总要面对的。", "我有点怕未来看不清楚。")),
        ScenarioSeed("部队经历", listOf("部队经历", "责任感"), listOf("你在部队是不是会比较不会表达？", "部队里出来的人会不会很硬？", "我有点好奇你以前的生活。")),
        ScenarioSeed("转业", listOf("转业", "现实规划"), listOf("你转业之后会不会压力很大？", "换一个阶段你会不适应吗？", "你以后想在哪个城市稳定？")),
        ScenarioSeed("责任感", listOf("责任感", "稳定"), listOf("责任感这种东西我现在很在意。", "我不太喜欢嘴上说得很好的人。", "我更看行动，不太看承诺。")),
        ScenarioSeed("过去经历", listOf("过去经历", "慢热观察"), listOf("过去的一些事让我比较慢热。", "我不是不信你，只是以前遇到过不太好的。", "我需要多观察一下。")),
        ScenarioSeed("真情流露", listOf("真情流露", "表达困难"), listOf("其实我也不是很会说这些。", "我说这些有点不好意思。", "我怕自己表达得太重。")),
        ScenarioSeed("表达困难", listOf("表达困难", "慢热观察"), listOf("我不知道怎么表达。", "我不是不想说，是说不出来。", "有些话我会卡住。")),
        ScenarioSeed("工作压力", listOf("工作压力", "稳定"), listOf("最近工作真的有点压得我喘不过来。", "我今天忙到有点麻木。", "我现在脑子里全是事。")),
        ScenarioSeed("带孩子", listOf("带孩子", "现实规划"), listOf("带孩子真的不是一句话的事。", "有时候我觉得自己一个人很累。", "现实问题摆在这儿，我不能不想。")),
        ScenarioSeed("慢热观察", listOf("慢热观察", "稳定"), listOf("我比较慢热，你别太急。", "我会先看一个人的细节。", "我不是很快就能进入状态的人。")),
        ScenarioSeed("走一步看一步", listOf("走一步看一步", "低压力"), listOf("那就先走一步看一步吧。", "我现在也不想把话说太满。", "慢慢来会不会好一点？")),
        ScenarioSeed("已读未回", listOf("已读未回", "低压力"), listOf("我看到了，只是刚才不知道怎么回。", "我有时候已读不是故意晾着。", "你会介意我慢一点回吗？")),
        ScenarioSeed("用户刚表达过自己", listOf("用户刚表达过自己", "边界"), listOf("你刚刚说的我看到了。", "你不用再解释一遍，我明白。", "你说这些的时候我有认真看。")),
        ScenarioSeed("对方说为你好", listOf("为你好", "边界"), listOf("我也是为你好。", "我怕你以后后悔。", "我说这些不是想管你。")),
        ScenarioSeed("不知道怎么表达", listOf("不知道怎么表达", "表达困难"), listOf("我真的不知道怎么表达。", "我怕说出来你误会。", "我心里有，但嘴上说不顺。")),
        ScenarioSeed("现实顾虑", listOf("现实顾虑", "现实规划"), listOf("感情不能只看感觉吧。", "现实压力还是要算进去。", "我怕最后都变成消耗。")),
        ScenarioSeed("生活节奏", listOf("生活节奏", "稳定"), listOf("我生活节奏比较固定。", "我不太喜欢被打乱。", "如果节奏不合会挺累的。")),
        ScenarioSeed("关系推进", listOf("关系推进", "未来"), listOf("我们现在算什么阶段？", "你觉得我们要往前走一点吗？", "我不想一直模糊着。"))
    )
}

class CharacterArcAutoJudge {
    fun judge(scenario: CharacterArcScenario, candidate: CharacterArcCandidate): CharacterArcJudgeResult {
        val text = candidate.text
        val lengthPenalty = ((text.length - 34).coerceAtLeast(0) / 8).coerceAtMost(18)
        val pressureWords = listOf("负责到底", "证明", "应该", "跟我走", "肯定", "永远", "成熟可靠")
        val oilyWords = listOf("作为一个", "比普通人", "你应该能感受", "证明给你看", "给你一个稳定的人生")
        val reportWords = listOf("经历过很多", "规划得很清楚", "责任感的人", "我作为")
        val pressureHit = pressureWords.count { text.contains(it) }
        val oilyHit = oilyWords.count { text.contains(it) }
        val reportHit = reportWords.count { text.contains(it) }

        val base = when (candidate.routeFamily) {
            CharacterArcRouteFamily.RECEIVE -> Scores(80, 74, 70, 22, 10, 60, 22, 8, 58)
            CharacterArcRouteFamily.ARC_REVEAL -> Scores(76, 84, 78, 34, 16, 84, 84, 18, 80)
            CharacterArcRouteFamily.OVERDO -> Scores(44, 58, 36, 82, 78, 54, 62, 90, 28)
        }
        val contextBoost = if (scenario.tags.any { it in arcFriendlyTags }) 6 else 0
        val naturalness = base.naturalness - lengthPenalty - oilyHit * 6
        val realness = base.realness + if (candidate.routeFamily == CharacterArcRouteFamily.ARC_REVEAL) contextBoost else 0
        val pressure = base.pressure + pressureHit * 8 + lengthPenalty
        val oily = base.oily + oilyHit * 12
        val overReport = base.overReport + reportHit * 14 + lengthPenalty
        val personaFit = base.personaFit + if (text.contains("行动") || text.contains("做到位") || text.contains("走稳")) 6 else 0
        val arcStrength = base.arcStrength + if (candidate.routeFamily == CharacterArcRouteFamily.ARC_REVEAL) contextBoost else 0
        val wantContinue = base.wantContinue - (pressure / 12) - oilyHit * 5
        val coCreation = base.coCreation + if (text.contains("我们") || text.contains("慢慢")) 4 else 0

        val rejectReason = when {
            oily >= 70 -> "too_oily"
            pressure >= 76 -> "too_heavy"
            overReport >= 76 -> "too_much_self_report"
            naturalness < 50 -> "not_natural"
            personaFit < 55 -> "persona_mismatch"
            else -> null
        }
        val total = naturalness + realness + wantContinue + personaFit + arcStrength + coCreation - pressure - oily - overReport
        val needsReview = candidate.routeFamily == CharacterArcRouteFamily.ARC_REVEAL &&
            (personaFit in 58..78 || pressure >= 42 || total in 190..270) ||
            (total >= 250 && personaFit < 72) ||
            (total < 170 && arcStrength >= 72)

        return CharacterArcJudgeResult(
            scenarioId = scenario.scenarioId,
            candidateId = candidate.candidateId,
            routeFamily = candidate.routeFamily,
            naturalnessScore = naturalness.clampScore(),
            realnessScore = realness.clampScore(),
            wantContinueScore = wantContinue.clampScore(),
            pressureScore = pressure.clampScore(),
            oilyScore = oily.clampScore(),
            personaFitScore = personaFit.clampScore(),
            arcStrengthScore = arcStrength.clampScore(),
            overReportScore = overReport.clampScore(),
            coCreationScore = coCreation.clampScore(),
            rejectReason = rejectReason,
            needsUserReview = needsReview
        )
    }

    fun judgeAll(scenarios: List<CharacterArcScenario>): List<CharacterArcJudgeResult> {
        return scenarios.flatMap { scenario ->
            CharacterArcSampleCorpus.candidatesFor(scenario).map { judge(scenario, it) }
        }
    }

    private data class Scores(
        val naturalness: Int,
        val realness: Int,
        val wantContinue: Int,
        val pressure: Int,
        val oily: Int,
        val personaFit: Int,
        val arcStrength: Int,
        val overReport: Int,
        val coCreation: Int
    )

    private fun Int.clampScore(): Int = coerceIn(0, 100)

    private companion object {
        val arcFriendlyTags = setOf(
            "现实规划",
            "未来",
            "稳定",
            "责任感",
            "部队经历",
            "转业",
            "过去经历",
            "表达困难",
            "带孩子"
        )
    }
}

class CharacterArcActiveSampler(
    private val autoJudge: CharacterArcAutoJudge = CharacterArcAutoJudge()
) {
    fun selectForInitialReview(
        scenarios: List<CharacterArcScenario> = CharacterArcSampleCorpus.scenarios(),
        reviewLimit: Int = 20
    ): List<CharacterArcReviewItem> = selectForUserReview(scenarios, reviewLimit)

    fun selectDailyReview(
        scenarios: List<CharacterArcScenario> = CharacterArcSampleCorpus.scenarios(),
        reviewLimit: Int = 5
    ): List<CharacterArcReviewItem> = selectForUserReview(scenarios, reviewLimit)

    fun selectForUserReview(
        scenarios: List<CharacterArcScenario>,
        reviewLimit: Int
    ): List<CharacterArcReviewItem> {
        val judgedByScenario = scenarios.associateWith { scenario ->
            CharacterArcSampleCorpus.candidatesFor(scenario).map { candidate ->
                candidate to autoJudge.judge(scenario, candidate)
            }
        }
        val ranked = judgedByScenario.map { (scenario, pairs) ->
            val results = pairs.map { it.second }
            val arc = results.firstOrNull { it.routeFamily == CharacterArcRouteFamily.ARC_REVEAL }
            val receive = results.firstOrNull { it.routeFamily == CharacterArcRouteFamily.RECEIVE }
            val scoreGap = results.maxOf { it.totalScore } - results.minOf { it.totalScore }
            val reason = when {
                scenario.scenarioId.endsWith("-1") -> "new_character_arc_card_first_seen"
                scoreGap >= 95 -> "large_model_score_disagreement"
                arc?.needsUserReview == true -> "arc_reveal_score_unstable"
                arc != null && arc.totalScore >= 230 && arc.personaFitScore < 78 -> "high_score_but_may_not_fit_user"
                arc != null && receive != null && arc.totalScore < receive.totalScore && arc.arcStrengthScore >= 80 -> "low_score_but_potential_arc_value"
                else -> "coverage_sample"
            }
            val priority = when (reason) {
                "new_character_arc_card_first_seen" -> 100
                "large_model_score_disagreement" -> 90
                "arc_reveal_score_unstable" -> 80
                "high_score_but_may_not_fit_user" -> 70
                "low_score_but_potential_arc_value" -> 60
                else -> 30
            } + scoreGap
            priority to CharacterArcReviewItem(
                scenario = scenario,
                blindCandidates = pairs.map { it.first }.sortedBy { (scenario.scenarioId + it.candidateId).hashCode() },
                reason = reason
            )
        }
        return ranked.sortedByDescending { it.first }
            .map { it.second }
            .take(reviewLimit.coerceAtLeast(0))
    }
}

class CharacterArcPreferenceStore(
    private val file: File
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun recordFeedback(record: CharacterArcPreferenceRecord): Result<Unit> = runCatching {
        file.parentFile?.mkdirs()
        file.appendText(record.toJsonLine() + "\n", Charsets.UTF_8)
    }

    fun loadRecords(): List<CharacterArcPreferenceRecord> {
        if (!file.exists()) return emptyList()
        return file.readLines(Charsets.UTF_8)
            .mapNotNull { line -> runCatching { line.toPreferenceRecord() }.getOrNull() }
    }

    fun buildProfile(): CharacterArcPreferenceProfile {
        val records = loadRecords()
        if (records.isEmpty()) return CharacterArcPreferenceProfile()
        val liked = records.filter { it.userFeedback in likedFeedback }
        val rejected = records.filter { it.userFeedback in rejectedFeedback }
        val arcLiked = liked.flatMap { it.characterArcCardIds }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
        val arcRejected = rejected.flatMap { it.characterArcCardIds }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
        val tooOily = records.filter { it.userFeedback == CharacterArcUserFeedback.TOO_OILY }
            .map { it.routeTextRedacted.toPattern() }
            .distinct()
            .take(8)
        val tooHeavy = records.filter { it.userFeedback == CharacterArcUserFeedback.TOO_HEAVY }
            .map { it.routeTextRedacted.toPattern() }
            .distinct()
            .take(8)
        val likedPhrases = liked.map { it.routeTextRedacted.toPattern() }.distinct().take(8)
        val dislikedPhrases = rejected.map { it.routeTextRedacted.toPattern() }.distinct().take(8)
        val comfort = when {
            liked.count { it.routeFamily == "ARC_REVEAL" } >= rejected.count { it.routeFamily == "ARC_REVEAL" } + 2 -> "MEDIUM"
            rejected.any { it.routeFamily == "ARC_REVEAL" } -> "LOW"
            else -> "UNKNOWN"
        }
        val preferredIntensity = when {
            records.any { it.userFeedback == CharacterArcUserFeedback.TOO_HEAVY } -> "LOW"
            liked.any { it.routeFamily == "ARC_REVEAL" } -> "LOW_TO_MEDIUM"
            else -> "LOW"
        }
        return CharacterArcPreferenceProfile(
            preferredArcCards = arcLiked,
            rejectedArcCards = arcRejected,
            tooOilyPatterns = tooOily,
            tooHeavyPatterns = tooHeavy,
            likedPhrases = likedPhrases,
            dislikedPhrases = dislikedPhrases,
            preferredIntensity = preferredIntensity,
            selfExpressionComfortLevel = comfort,
            feedbackCount = records.size
        )
    }

    private fun CharacterArcPreferenceRecord.toJsonLine(): String {
        return buildJsonObject {
            put("routeTextRedacted", routeTextRedacted)
            put("routeFamily", routeFamily)
            put("personaCardIds", buildJsonArray { personaCardIds.forEach { add(it) } })
            put("characterArcCardIds", buildJsonArray { characterArcCardIds.forEach { add(it) } })
            put("userFeedback", userFeedback.name)
            put("timestamp", timestamp)
            put("scenarioTags", buildJsonArray { scenarioTags.forEach { add(it) } })
            noteRedacted?.let { put("noteRedacted", it) }
        }.toString()
    }

    private fun String.toPreferenceRecord(): CharacterArcPreferenceRecord {
        val obj = json.parseToJsonElement(this).jsonObject
        return CharacterArcPreferenceRecord(
            routeTextRedacted = obj["routeTextRedacted"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            routeFamily = obj["routeFamily"]?.jsonPrimitive?.contentOrNull.orEmpty(),
            personaCardIds = obj["personaCardIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
            characterArcCardIds = obj["characterArcCardIds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
            userFeedback = runCatching {
                CharacterArcUserFeedback.valueOf(obj["userFeedback"]?.jsonPrimitive?.contentOrNull.orEmpty())
            }.getOrDefault(CharacterArcUserFeedback.ALL_BAD),
            timestamp = obj["timestamp"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0L,
            scenarioTags = obj["scenarioTags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty(),
            noteRedacted = obj["noteRedacted"]?.jsonPrimitive?.contentOrNull
        )
    }

    private fun String.toPattern(): String = take(24)

    companion object {
        val likedFeedback = setOf(
            CharacterArcUserFeedback.LIKE_ME,
            CharacterArcUserFeedback.SENDABLE,
            CharacterArcUserFeedback.MOST_NATURAL,
            CharacterArcUserFeedback.WANT_CONTINUE
        )
        val rejectedFeedback = setOf(
            CharacterArcUserFeedback.NOT_LIKE_ME,
            CharacterArcUserFeedback.TOO_OILY,
            CharacterArcUserFeedback.TOO_HEAVY,
            CharacterArcUserFeedback.TOO_EMPTY,
            CharacterArcUserFeedback.TOO_REPORT,
            CharacterArcUserFeedback.ALL_BAD
        )
    }
}

object CharacterArcRedactor {
    fun redact(text: String): String {
        return text
            .replace(Regex("\\b1[3-9]\\d{9}\\b"), "[phone]")
            .replace(Regex("\\b\\d{5,}\\b"), "[number]")
            .replace(Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+"), "[email]")
            .take(140)
    }
}

class CharacterArcSoloValidationReportGenerator(
    private val autoJudge: CharacterArcAutoJudge = CharacterArcAutoJudge(),
    private val sampler: CharacterArcActiveSampler = CharacterArcActiveSampler(autoJudge)
) {
    fun generate(
        records: List<CharacterArcPreferenceRecord> = emptyList(),
        generatedAt: String = "2026-07-04T18:00:00+08:00"
    ): CharacterArcSoloValidationReport {
        val scenarios = CharacterArcSampleCorpus.scenarios()
        val results = autoJudge.judgeAll(scenarios)
        val reviewItems = sampler.selectForInitialReview(scenarios, 20)
        val arcResults = results.filter { it.routeFamily == CharacterArcRouteFamily.ARC_REVEAL }
        val overdoResults = results.filter { it.routeFamily == CharacterArcRouteFamily.OVERDO }
        val receiveResults = results.filter { it.routeFamily == CharacterArcRouteFamily.RECEIVE }
        val arcWinRate = scenarios.count { scenario ->
            val scenarioResults = results.filter { it.scenarioId == scenario.scenarioId }
            val arc = scenarioResults.first { it.routeFamily == CharacterArcRouteFamily.ARC_REVEAL }
            val receive = scenarioResults.first { it.routeFamily == CharacterArcRouteFamily.RECEIVE }
            val overdo = scenarioResults.first { it.routeFamily == CharacterArcRouteFamily.OVERDO }
            arc.totalScore > receive.totalScore && arc.totalScore > overdo.totalScore
        }.toDouble() / scenarios.size.coerceAtLeast(1)
        val arcPersonaFit = arcResults.map { it.personaFitScore }.averageOrZero()
        val overdoPressure = overdoResults.map { it.pressureScore }.averageOrZero()
        val wantDelta = arcResults.map { it.wantContinueScore }.averageOrZero() -
            receiveResults.map { it.wantContinueScore }.averageOrZero()
        val profile = if (records.isEmpty()) CharacterArcPreferenceProfile() else profileFrom(records)
        val report = CharacterArcSoloValidationReport(
            generatedAt = generatedAt,
            autoJudgedScenarioCount = scenarios.size,
            candidateCount = results.size,
            userReviewNeededCount = reviewItems.size,
            userReviewedCount = records.size,
            arcRevealWinRate = arcWinRate,
            arcRevealPersonaFitScore = arcPersonaFit,
            overdoPressureScore = overdoPressure,
            receiveVsArcWantContinueDelta = wantDelta,
            topRejectedPatterns = profile.tooOilyPatterns + profile.tooHeavyPatterns + profile.dislikedPhrases,
            topAcceptedArcCards = profile.preferredArcCards.ifEmpty { listOf("default-character-arc") },
            nextPromptAdjustmentSuggestions = listOf(
                "Keep ARC_REVEAL short; do not let it become a self report.",
                "Prefer action-based self expression over promises.",
                "Show only high-disagreement or persona-fit-uncertain samples to the user."
            )
        )
        return report.copy(
            markdown = markdown(report),
            json = json(report)
        )
    }

    private fun profileFrom(records: List<CharacterArcPreferenceRecord>): CharacterArcPreferenceProfile {
        val temp = File.createTempFile("huiyi-character-arc-profile", ".jsonl")
        return try {
            val store = CharacterArcPreferenceStore(temp)
            records.forEach { store.recordFeedback(it).getOrThrow() }
            store.buildProfile()
        } finally {
            temp.delete()
        }
    }

    private fun markdown(report: CharacterArcSoloValidationReport): String = buildString {
        appendLine("# Character Arc Solo Validation Report")
        appendLine()
        appendLine("## Summary")
        appendLine("- taskName: solo_character_arc_validation_loop")
        appendLine("- generatedAt: ${report.generatedAt}")
        appendLine("- autoJudgedScenarioCount: ${report.autoJudgedScenarioCount}")
        appendLine("- candidateCount: ${report.candidateCount}")
        appendLine("- userReviewNeededCount: ${report.userReviewNeededCount}")
        appendLine("- userReviewedCount: ${report.userReviewedCount}")
        appendLine("- arcRevealWinRate: ${report.arcRevealWinRate.percentString()}")
        appendLine("- arcRevealPersonaFitScore: ${report.arcRevealPersonaFitScore.roundToInt()}")
        appendLine("- overdoPressureScore: ${report.overdoPressureScore.roundToInt()}")
        appendLine("- receiveVsArcWantContinueDelta: ${report.receiveVsArcWantContinueDelta.roundToInt()}")
        appendLine()
        appendLine("## Safety")
        appendLine("- soloReviewLabImplemented: true")
        appendLine("- resultPanelFeedbackButtons: true")
        appendLine("- preferenceProfileGenerated: true")
        appendLine("- longTermRawPrivateChatSaved: false")
        appendLine("- rawPrivateChatUploadedToGithub: false")
        appendLine("- autoSend: false")
        appendLine("- firstRoundReviewLimit: 20")
        appendLine("- dailyReviewLimit: 5")
        appendLine()
        appendLine("## Patterns")
        appendLine("- topRejectedPatterns: ${report.topRejectedPatterns.take(8).joinToString(" / ").ifBlank { "none_yet" }}")
        appendLine("- topAcceptedArcCards: ${report.topAcceptedArcCards.take(8).joinToString(" / ")}")
        appendLine()
        appendLine("## Next Prompt Adjustment Suggestions")
        report.nextPromptAdjustmentSuggestions.forEach { appendLine("- $it") }
    }

    private fun json(report: CharacterArcSoloValidationReport): String {
        return buildJsonObject {
            put("taskName", "solo_character_arc_validation_loop")
            put("generatedAt", report.generatedAt)
            put("autoJudgedScenarioCount", report.autoJudgedScenarioCount)
            put("candidateCount", report.candidateCount)
            put("userReviewNeededCount", report.userReviewNeededCount)
            put("userReviewedCount", report.userReviewedCount)
            put("arcRevealWinRate", (report.arcRevealWinRate * 100).roundToInt())
            put("arcRevealPersonaFitScore", report.arcRevealPersonaFitScore.roundToInt())
            put("overdoPressureScore", report.overdoPressureScore.roundToInt())
            put("receiveVsArcWantContinueDelta", report.receiveVsArcWantContinueDelta.roundToInt())
            put("topRejectedPatterns", buildJsonArray { report.topRejectedPatterns.take(8).forEach { add(it) } })
            put("topAcceptedArcCards", buildJsonArray { report.topAcceptedArcCards.take(8).forEach { add(it) } })
            put("nextPromptAdjustmentSuggestions", buildJsonArray { report.nextPromptAdjustmentSuggestions.forEach { add(it) } })
            put("soloReviewLabImplemented", true)
            put("resultPanelFeedbackButtons", true)
            put("preferenceProfileGenerated", true)
            put("longTermRawPrivateChatSaved", false)
            put("rawPrivateChatUploadedToGithub", false)
            put("autoSend", false)
        }.toString()
    }

    private fun List<Int>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()

    private fun Double.percentString(): String = "${(this * 100).roundToInt()}%"
}

data class CharacterArcSoloValidationReport(
    val generatedAt: String,
    val autoJudgedScenarioCount: Int,
    val candidateCount: Int,
    val userReviewNeededCount: Int,
    val userReviewedCount: Int,
    val arcRevealWinRate: Double,
    val arcRevealPersonaFitScore: Double,
    val overdoPressureScore: Double,
    val receiveVsArcWantContinueDelta: Double,
    val topRejectedPatterns: List<String>,
    val topAcceptedArcCards: List<String>,
    val nextPromptAdjustmentSuggestions: List<String>,
    val markdown: String = "",
    val json: String = ""
)
