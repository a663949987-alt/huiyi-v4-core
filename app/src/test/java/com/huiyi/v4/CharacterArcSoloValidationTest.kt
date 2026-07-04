package com.huiyi.v4

import com.huiyi.v4.domain.model.InfluenceIntensity
import com.huiyi.v4.domain.model.ReplyRoute
import com.huiyi.v4.domain.model.ReplyRouteType
import com.huiyi.v4.domain.model.RiskLevel
import com.huiyi.v4.domain.persona.CharacterArcActiveSampler
import com.huiyi.v4.domain.persona.CharacterArcAutoJudge
import com.huiyi.v4.domain.persona.CharacterArcPreferenceRecord
import com.huiyi.v4.domain.persona.CharacterArcPreferenceStore
import com.huiyi.v4.domain.persona.CharacterArcRouteFamily
import com.huiyi.v4.domain.persona.CharacterArcSampleCorpus
import com.huiyi.v4.domain.persona.CharacterArcSoloValidationReportGenerator
import com.huiyi.v4.domain.persona.CharacterArcUserFeedback
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterArcSoloValidationTest {
    @Test
    fun AutoJudgeRunsSixtyScenariosAndSamplesAtMostTwentyTest() {
        val scenarios = CharacterArcSampleCorpus.scenarios()
        val results = CharacterArcAutoJudge().judgeAll(scenarios)
        val reviewItems = CharacterArcActiveSampler().selectForInitialReview(scenarios, 20)

        assertEquals(60, scenarios.size)
        assertEquals(180, results.size)
        scenarios.forEach { scenario ->
            val families = CharacterArcSampleCorpus.candidatesFor(scenario).map { it.routeFamily }.toSet()
            assertEquals(
                setOf(
                    CharacterArcRouteFamily.RECEIVE,
                    CharacterArcRouteFamily.ARC_REVEAL,
                    CharacterArcRouteFamily.OVERDO
                ),
                families
            )
        }
        assertTrue(reviewItems.isNotEmpty())
        assertTrue(reviewItems.size <= 20)
        assertTrue(reviewItems.any { it.reason == "arc_reveal_score_unstable" || it.reason == "new_character_arc_card_first_seen" })
    }

    @Test
    fun PreferenceStoreRedactsRouteFeedbackAndBuildsProfileTest() {
        val file = File.createTempFile("character-arc-preference", ".jsonl").also { it.delete() }
        val store = CharacterArcPreferenceStore(file)
        val route = ReplyRoute(
            id = "arc-1",
            name = "人物弧光",
            routeType = ReplyRouteType.ARC_REVEAL,
            tag = "ARC_REVEAL",
            message = "我电话 13812345678，但认真起来会把事做到位。",
            intensity = InfluenceIntensity.LOW,
            riskLevel = RiskLevel.MEDIUM,
            riskWarning = "不要说过头",
            expectedEffect = "轻表达",
            fallbackMove = "回到接住她",
            recommended = true
        )

        store.recordFeedback(
            CharacterArcPreferenceRecord.fromRoute(route, CharacterArcUserFeedback.LIKE_ME, now = 1L)
        ).getOrThrow()

        val raw = file.readText(Charsets.UTF_8)
        val profile = store.buildProfile()

        assertFalse(raw.contains("13812345678"))
        assertTrue(raw.contains("[phone]"))
        assertEquals(1, profile.feedbackCount)
        assertTrue(profile.preferredArcCards.contains("default-character-arc"))
        assertFalse(raw.contains("raw private chat"))
    }

    @Test
    fun SoloValidationReportWritesForGptTest() {
        val report = CharacterArcSoloValidationReportGenerator().generate(
            generatedAt = "2026-07-04T18:00:00+08:00"
        )
        val outDir = File(repoRoot(), "outputs/gpt_review_inbox").apply { mkdirs() }
        val markdown = File(outDir, "character-arc-solo-validation-report-for-gpt.md")
        val json = File(outDir, "character-arc-solo-validation-report.json")
        markdown.writeText(report.markdown, Charsets.UTF_8)
        json.writeText(report.json, Charsets.UTF_8)

        assertEquals(60, report.autoJudgedScenarioCount)
        assertEquals(180, report.candidateCount)
        assertTrue(report.userReviewNeededCount <= 20)
        assertTrue(report.markdown.contains("longTermRawPrivateChatSaved: false"))
        assertTrue(report.markdown.contains("autoSend: false"))
        assertTrue(json.readText(Charsets.UTF_8).contains("\"rawPrivateChatUploadedToGithub\":false"))
    }

    private fun repoRoot(): File {
        var current: File? = File(".").absoluteFile
        while (current != null && !File(current, "settings.gradle.kts").exists()) {
            current = current.parentFile
        }
        return current ?: File(".").absoluteFile
    }
}
