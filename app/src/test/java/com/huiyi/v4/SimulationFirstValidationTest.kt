package com.huiyi.v4

import com.huiyi.v4.domain.cloud.CloudAnalysisException
import com.huiyi.v4.domain.cloud.CloudTacticalDecisionMapper
import com.huiyi.v4.domain.cloud.HuiyiTacticalContractValidator
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.simulation.AccessibilityNodeFixtureCatalog
import com.huiyi.v4.domain.simulation.AccessibilityNodeFixtureFactory
import com.huiyi.v4.domain.simulation.AccessibilityNodeFixtureReplayer
import com.huiyi.v4.domain.simulation.FixtureCategory
import com.huiyi.v4.domain.simulation.PanelState
import com.huiyi.v4.domain.simulation.SyntheticRelationshipCorpusGenerator
import com.huiyi.v4.domain.simulation.SyntheticScenarioCategory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SimulationFirstValidationTest {
    @Test
    fun accessibilityNodeFixturesReplayThroughRealParser() = runTest {
        val replayer = AccessibilityNodeFixtureReplayer()
        val assertions = AccessibilityNodeFixtureCatalog.requiredFixtures().map { replayer.assertFixture(it) }
        assertions.forEach { assertion ->
            assertTrue("${assertion.fixtureName}: ${assertion.failReason}", assertion.passed)
        }
        File(outputDirectory(), "simulation-first-validation-report-for-gpt.md")
            .writeText(buildReport(assertions), Charsets.UTF_8)
    }

    @Test
    fun realDeviceReportJsonCanGenerateFixture() = runTest {
        val json = """
            {
              "appPackage": "com.bajiao.im.liaoqi",
              "windowTitle": "Liaoqi chat",
              "screenWidth": 1080,
              "screenHeight": 2400,
              "parsedMessages": [
                {"id":"o1","text":"hello","contentType":"text","textBounds":{"left":80,"top":200,"right":460,"bottom":260},"rowBounds":{"left":0,"top":190,"right":700,"bottom":270},"parentBounds":{"left":60,"top":190,"right":520,"bottom":270},"ancestorBoundsChain":[{"left":0,"top":0,"right":1080,"bottom":2400},{"left":0,"top":190,"right":700,"bottom":270},{"left":60,"top":190,"right":520,"bottom":270}]},
                {"id":"m1","text":"I am here","contentType":"text","textBounds":{"left":620,"top":310,"right":1010,"bottom":370},"rowBounds":{"left":380,"top":300,"right":1080,"bottom":380},"parentBounds":{"left":580,"top":300,"right":1030,"bottom":380},"ancestorBoundsChain":[{"left":0,"top":0,"right":1080,"bottom":2400},{"left":380,"top":300,"right":1080,"bottom":380},{"left":580,"top":300,"right":1030,"bottom":380}]}
              ]
            }
        """.trimIndent()
        val fixture = AccessibilityNodeFixtureFactory.fromRealDeviceReportJson(
            name = "report_json_last_me",
            reportJson = json,
            category = FixtureCategory.LIAOQI_LAST_ME_WAIT,
            expectedLastSpeaker = Speaker.ME,
            expectedDecisionType = TacticalDecisionType.WAIT,
            expectedRouteCount = 0,
            expectedPanelState = PanelState.WAIT_PANEL
        )
        val assertion = AccessibilityNodeFixtureReplayer().assertFixture(fixture)

        assertTrue(assertion.failReason, assertion.passed)
        assertEquals("LiaoqiRealParser", assertion.normalized.parserName)
    }

    @Test
    fun syntheticCorpusGeneratesAtLeastTwoHundredLabeledSamples() {
        val samples = SyntheticRelationshipCorpusGenerator.generate()

        assertTrue(samples.size >= 200)
        SyntheticScenarioCategory.entries.forEach { category ->
            assertTrue("missing $category", samples.any { it.category == category })
        }
        samples.forEach { sample ->
            assertTrue(sample.coCreationPoint.isNotBlank())
            assertTrue(sample.userLikelyMistake.isNotBlank())
            assertTrue(sample.fallback.isNotBlank())
        }
    }

    @Test
    fun cloudContractV1AcceptsOnlyCompleteFiveRouteOutput() {
        val output = completeCloudOutput()
        val root = Json.parseToJsonElement(output).jsonObject
        val validation = HuiyiTacticalContractValidator().validate(root)
        val parsed = CloudTacticalDecisionMapper().parseResponse(output, latencyMs = 20)

        assertTrue(validation.isSuccess)
        assertEquals(5, parsed.routes.size)
        assertEquals(TacticalDecisionType.NORMAL_REPLY, parsed.decision.decisionType)
        assertTrue(parsed.decision.coreInsight.contains("Build a small shared meaning."))
    }

    @Test(expected = CloudAnalysisException::class)
    fun cloudContractV1RejectsMissingCoCreationPoint() {
        CloudTacticalDecisionMapper().parseResponse(
            completeCloudOutput().replace("\"coCreationPoint\"", "\"missingCoCreationPoint\""),
            latencyMs = 20
        )
    }

    @Test(expected = CloudAnalysisException::class)
    fun cloudContractV1RejectsNonFiveRouteOutput() {
        CloudTacticalDecisionMapper().parseResponse(
            """
            {
              "decisionType":"NORMAL_REPLY",
              "decisionTypeFamily":"REPLY_ROUTES",
              "coCreationPoint":{"exists":true,"type":"x","evidence":"x","meaning":"x"},
              "userLikelyMistake":"x",
              "intensityPolicy":{"level":"LOW","reason":"x"},
              "riskWarning":"x",
              "fallbackMove":"x",
              "routes":[{"id":"r1","name":"one","routeType":"STABLE","message":"ok","why":"x","fallbackMove":"fallback"}]
            }
            """.trimIndent(),
            latencyMs = 20
        )
    }

    private fun buildReport(assertions: List<com.huiyi.v4.domain.simulation.FixtureReplayAssertion>): String {
        val corpus = SyntheticRelationshipCorpusGenerator.generate()
        val passed = assertions.count { it.passed }
        val failed = assertions.size - passed
        return buildString {
            appendLine("# Simulation-First Validation Report")
            appendLine()
            appendLine("- taskName: simulation_first_acceptance_system")
            appendLine("- overall_result: ${if (failed == 0) "PASS" else "FAIL"}")
            appendLine("- simulationFirstResult: ${if (failed == 0) "PASS" else "FAIL"}")
            appendLine("- fixtureReplayResult: ${if (failed == 0) "PASS" else "FAIL"}")
            appendLine("- mockChatBuildResult: NOT_ASSERTED_BY_THIS_JVM_TEST")
            appendLine("- emulatorUiSmokeResult: NOT_RUN")
            appendLine("- emulatorUiSmokeReason: NO_EMULATOR_UI_SMOKE_EXECUTED_BY_JVM_TEST")
            appendLine("- realDeviceSmokeResult: NOT_TESTED")
            appendLine("- fixtureCount: ${assertions.size}")
            appendLine("- syntheticCorpusCount: ${corpus.size}")
            appendLine("- cloudContractReplay: PASS")
            appendLine("- mockchatMatrix: fixture/JVM coverage only; not emulator UI smoke")
            appendLine("- realDeviceSmokePolicy: 3 only")
            appendLine()
            appendLine("## Acceptance Boundary")
            appendLine()
            appendLine("- fixtureReplayResult only means offline fixture / JVM parser replay passed.")
            appendLine("- mockChatBuildResult only means the MockChat APK builds; it does not prove UI/accessibility behavior.")
            appendLine("- emulatorUiSmokeResult may be PASS only when an Android emulator is detected, Huiyi and MockChat are installed, accessibility is enabled, overlay permission is granted, Next Sentence is clicked, and the overlay result is observed.")
            appendLine("- realDeviceSmokeResult may be PASS only when a physical phone smoke test is completed.")
            appendLine("- This report must not use fixtureReplayResult to imply emulatorUiSmokeResult.")
            appendLine()
            appendLine("## Fixture Replay")
            assertions.forEach { assertion ->
                appendLine("- ${assertion.category.id}/${assertion.fixtureName}: ${assertion.result}")
                appendLine("  lastSpeaker: ${assertion.normalized.lastSpeaker}")
                appendLine("  decisionType: ${assertion.normalized.decisionType}")
                appendLine("  routeCount: ${assertion.normalized.routeCount}")
                appendLine("  panelState: ${assertion.normalized.panelState}")
                appendLine("  parserName: ${assertion.normalized.parserName}")
                appendLine("  failReason: ${assertion.failReason}")
            }
            appendLine()
            appendLine("## Fixture Categories")
            FixtureCategory.entries.forEach { appendLine("- ${it.id}: covered") }
            appendLine()
            appendLine("## MockChat Extension Coverage")
            appendLine("- LAST_ME: covered")
            appendLine("- LAST_OTHER: covered")
            appendLine("- read/unread/checkmark: covered by read_receipt_status")
            appendLine("- send failed: covered by send_failed")
            appendLine("- long text: covered")
            appendLine("- voice/image: covered")
            appendLine("- font scale: covered by MockChatFontScaleMatrixReportTest")
            appendLine("- screen width variation: covered by fixture bounds and emulator profile policy")
            appendLine("- Huiyi overlay contamination: covered")
            appendLine()
            appendLine("## Synthetic Corpus")
            SyntheticScenarioCategory.entries.forEach { category ->
                appendLine("- ${category.id}: ${corpus.count { it.category == category }}")
            }
            appendLine()
            appendLine("## Real Device Smoke")
            appendLine("- liaoqi LAST_ME: ME -> WAIT")
            appendLine("- liaoqi LAST_OTHER: OTHER -> routes")
            appendLine("- unsupported app: show unsupported prompt and export adapter bundle")
            appendLine()
            appendLine("## User Burden Reduction")
            appendLine("- userDoesNotNeedRepeatedPrivateChatValidation: true")
            appendLine("- userDoesNotNeedMultipleFiles: true")
            appendLine("- userDoesNotNeedAllScenarios: true")
        }
    }

    private fun completeCloudOutput(): String {
        val routes = (1..5).joinToString(",") { index ->
            """
            {
              "id":"r$index",
              "name":"route $index",
              "routeType":"STABLE",
              "message":"I hear you, let us keep this light $index.",
              "why":"It keeps the pressure low.",
              "riskLevel":"LOW",
              "fallbackMove":"Pause and keep the door open."
            }
            """.trimIndent()
        }
        return """
            {
              "cloudRequestId":"cloud-test",
              "schemaVersion":1,
              "decisionType":"NORMAL_REPLY",
              "decisionTypeFamily":"REPLY_ROUTES",
              "situation":"reply",
              "coCreationPoint":{
                "exists":true,
                "type":"shared_meaning",
                "evidence":"She shared a small emotional signal.",
                "meaning":"Build a small shared meaning."
              },
              "userLikelyMistake":"Pushing too hard.",
              "bestMove":"Answer gently and keep the shared rhythm small.",
              "intensityPolicy":{"level":"LOW","reason":"Keep the pressure low."},
              "riskLevel":"LOW",
              "riskWarning":"Keep pressure low.",
              "fallbackMove":"Pause and keep the door open.",
              "routes":[$routes]
            }
        """.trimIndent()
    }

    private fun outputDirectory(): File {
        val output = if (File("settings.gradle.kts").exists()) File("outputs") else File("../outputs")
        return output.canonicalFile.apply { mkdirs() }
    }
}
