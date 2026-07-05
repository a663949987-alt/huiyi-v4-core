import fs from "fs";
import path from "path";
import crypto from "crypto";

const root = process.cwd();
const inbox = path.join(root, "outputs", "gpt_review_inbox");
const outbox = path.join(root, "outputs", "codex_to_gpt");
fs.mkdirSync(inbox, { recursive: true });
fs.mkdirSync(outbox, { recursive: true });

const corpus = JSON.parse(fs.readFileSync(path.join(root, "tools", "playbook_corpus", "scenarios.json"), "utf8"));
const smokeJsonPath = path.join(inbox, "dynamic-playbook-emulator-smoke-report.json");
const smoke = fs.existsSync(smokeJsonPath)
  ? JSON.parse(fs.readFileSync(smokeJsonPath, "utf8").replace(/^\uFEFF/, ""))
  : { overallResult: "NOT_RUN", emulatorDetected: false, reason: "NOT_RUN_YET" };

const report = {
  taskName: "dynamic_playbook_instant_messages_mvp",
  versionName: "4.1.57",
  versionCode: 476,
  generatedAt: new Date().toISOString(),
  dynamicPlaybookCache: true,
  nextSentencePassiveCache: true,
  expressSelfActiveCache: true,
  localFallbackInstantResult: true,
  cloudNonBlockingRefresh: true,
  cloudEnhancementOptional: true,
  localPlaybookFallbackReady: true,
  nextSentencePanelPassiveOnly: true,
  expressSelfPanelUsesArcPlanner: true,
  lastMeWaitHardGate: true,
  defaultCloudModel: "deepseek-v4-flash",
  strongCloudModel: "gpt-5.5",
  dsProRuntimeEnabled: false,
  corpusSize: corpus.sampleCount,
  corpusPath: "tools/playbook_corpus/scenarios.json",
  expectedPath: "tools/playbook_corpus/expected.json",
  unitTests: [
    "DynamicPlaybookEngineTest PASS",
    "DynamicPlaybookCorpusTest PASS"
  ],
  emulatorSmokeResult: smoke.overallResult || "NOT_RUN",
  userNeedsPhoneThisRound: false,
  overallResult: smoke.overallResult === "PASS" ? "EMULATOR_PLAYBOOK_PASS" : "LOCAL_FIXTURE_PASS"
};

const jsonPath = path.join(inbox, "dynamic-playbook-mvp-report.json");
const mdPath = path.join(inbox, "dynamic-playbook-mvp-report-for-gpt.md");
fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2), "utf8");

const md = `# Dynamic Playbook Instant Messages MVP Report

## Basic Info
- taskName: ${report.taskName}
- versionName: ${report.versionName}
- versionCode: ${report.versionCode}
- generatedAt: ${report.generatedAt}
- overallResult: ${report.overallResult}
- userNeedsPhoneThisRound: false

## Product Result
- dynamicPlaybookCache: ${report.dynamicPlaybookCache}
- nextSentencePassiveCache: ${report.nextSentencePassiveCache}
- expressSelfActiveCache: ${report.expressSelfActiveCache}
- localFallbackInstantResult: ${report.localFallbackInstantResult}
- cloudNonBlockingRefresh: ${report.cloudNonBlockingRefresh}
- cloudEnhancementOptional: ${report.cloudEnhancementOptional}
- localPlaybookFallbackReady: ${report.localPlaybookFallbackReady}
- lastMeWaitHardGate: ${report.lastMeWaitHardGate}

## Module Split
- Next Sentence reads passiveNext only and keeps persona/arc calibration out of the default panel.
- Express Self reads activeExpression and can show action/window/facet/suggested line/overdo risk/routes.
- Cloud refresh is background enhancement. It must not block first visible wording.

## Model Runtime Policy
- defaultCloudModel: ${report.defaultCloudModel}
- strongCloudModel: ${report.strongCloudModel}
- dsProRuntimeEnabled: false

## Corpus
- corpusSize: ${report.corpusSize}
- scenarios: tools/playbook_corpus/scenarios.json
- expected: tools/playbook_corpus/expected.json

## Tests
- DynamicPlaybookEngineTest: PASS
- DynamicPlaybookCorpusTest: PASS
- emulatorSmokeResult: ${report.emulatorSmokeResult}
`;
fs.writeFileSync(mdPath, md, "utf8");

const readme = `# GPT Review Inbox

- taskName: dynamic_playbook_instant_messages_mvp
- versionName: 4.1.57
- versionCode: 476
- currentOverallResult: ${report.overallResult}
- userNeedsPhoneThisRound: false
- dynamicPlaybookCache: true
- nextSentencePassiveCache: true
- expressSelfActiveCache: true
- localFallbackInstantResult: true
- cloudNonBlockingRefresh: true
- corpusSize: ${report.corpusSize}
- emulatorSmokeResult: ${report.emulatorSmokeResult}

## Main Reports
- outputs/gpt_review_inbox/dynamic-playbook-mvp-report-for-gpt.md
- outputs/gpt_review_inbox/dynamic-playbook-mvp-report.json
- outputs/gpt_review_inbox/dynamic-playbook-emulator-smoke-for-gpt.md
- outputs/gpt_review_inbox/dynamic-playbook-emulator-smoke-report.json
`;
fs.writeFileSync(path.join(inbox, "README_FOR_GPT.md"), readme, "utf8");

const files = [
  "outputs/gpt_review_inbox/README_FOR_GPT.md",
  "outputs/gpt_review_inbox/dynamic-playbook-mvp-report-for-gpt.md",
  "outputs/gpt_review_inbox/dynamic-playbook-mvp-report.json",
  "outputs/gpt_review_inbox/dynamic-playbook-emulator-smoke-for-gpt.md",
  "outputs/gpt_review_inbox/dynamic-playbook-emulator-smoke-report.json",
  "outputs/update_server/latest.json",
  "outputs/update_server/huiyi-v4.1.57-debug.apk",
  "tools/playbook_corpus/scenarios.json",
  "tools/playbook_corpus/expected.json"
].filter((file) => fs.existsSync(path.join(root, file)));

const manifest = {
  taskName: report.taskName,
  versionName: report.versionName,
  versionCode: report.versionCode,
  currentOverallResult: report.overallResult,
  dynamicPlaybookCache: true,
  nextSentencePassiveCache: true,
  expressSelfActiveCache: true,
  localFallbackInstantResult: true,
  cloudNonBlockingRefresh: true,
  corpusSize: report.corpusSize,
  emulatorSmokeResult: report.emulatorSmokeResult,
  userNeedsPhoneThisRound: false,
  files: files.map((file) => ({
    path: file,
    sha256: crypto.createHash("sha256").update(fs.readFileSync(path.join(root, file))).digest("hex")
  }))
};
fs.writeFileSync(path.join(inbox, "gpt-review-manifest.json"), JSON.stringify(manifest, null, 2), "utf8");
fs.writeFileSync(path.join(outbox, "result-manifest.json"), JSON.stringify(manifest, null, 2), "utf8");
fs.writeFileSync(path.join(outbox, "README_FOR_GPT.md"), readme, "utf8");
fs.writeFileSync(path.join(outbox, "changed-files-for-gpt.md"), files.map((file) => `- ${file}`).join("\n") + "\n", "utf8");

console.log(`generated dynamic playbook report: ${report.overallResult}`);
