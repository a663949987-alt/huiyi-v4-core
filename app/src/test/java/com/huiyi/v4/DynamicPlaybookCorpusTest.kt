package com.huiyi.v4

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicPlaybookCorpusTest {
    @Test
    fun PlaybookCorpusContainsAtLeastEightyCoveredSamplesTest() {
        val scenarios = corpusFile("scenarios.json").readText()
        val expected = corpusFile("expected.json").readText()

        assertTrue(scenarios.contains("\"sampleCount\": 90"))
        assertTrue(expected.contains("\"sampleCount\": 90"))
        listOf(
            "ordinary",
            "last_me",
            "reality_planning",
            "stability",
            "future",
            "past",
            "true_feeling",
            "expression_difficulty",
            "work_pressure",
            "transition",
            "army",
            "responsibility",
            "kids",
            "read_no_reply",
            "light_life",
            "topic_shift",
            "other_retreat",
            "user_just_expressed"
        ).forEach { category ->
            assertTrue("missing category $category", scenarios.contains("\"category\": \"$category\""))
        }
        assertTrue(scenarios.contains("\"cloudBlockingAllowed\": false"))
        assertTrue(scenarios.contains("\"rawPrivateChat\": false"))
    }

    private fun corpusFile(name: String): File =
        listOf(
            File("tools/playbook_corpus/$name"),
            File("../tools/playbook_corpus/$name")
        ).first { it.exists() }
}
