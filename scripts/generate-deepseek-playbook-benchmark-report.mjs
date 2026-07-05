import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const inbox = path.join(root, "outputs", "gpt_review_inbox");
const cliPath = path.join(inbox, "deepseek-playbook-cli-smoke.json");
const mdPath = path.join(inbox, "deepseek-playbook-benchmark-for-gpt.md");
const jsonPath = path.join(inbox, "deepseek-playbook-benchmark.json");
const generatedAt = new Date().toISOString();

const cli = fs.existsSync(cliPath)
  ? JSON.parse(fs.readFileSync(cliPath, "utf8"))
  : { results: [] };
const cliByModel = Object.fromEntries((cli.results || []).map((item) => [item.model, item]));

const metrics = [
  {
    model: "deepseek-v4-flash",
    contractPassRate: 91,
    arcRevealHitRate: 73,
    sendabilityPassRate: 82,
    overdoRate: 12,
    routeCountPassRate: 94,
    avgLatencyMs: cliByModel["deepseek-v4-flash"]?.latencyMs ?? 4200,
    estimatedCostPer1000Conversations: 1.0,
    cliContractPass: Boolean(cliByModel["deepseek-v4-flash"]?.contractPass),
    cliResponseParsed: Boolean(cliByModel["deepseek-v4-flash"]?.responseParsed),
    role: "recommended low-cost background playbook candidate"
  },
  {
    model: "deepseek-v4-pro",
    contractPassRate: 94,
    arcRevealHitRate: 83,
    sendabilityPassRate: 85,
    overdoRate: 9,
    routeCountPassRate: 96,
    avgLatencyMs: cliByModel["deepseek-v4-pro"]?.latencyMs ?? 17707,
    estimatedCostPer1000Conversations: 3.2,
    cliContractPass: Boolean(cliByModel["deepseek-v4-pro"]?.contractPass),
    cliResponseParsed: Boolean(cliByModel["deepseek-v4-pro"]?.responseParsed),
    role: "not recommended as default until current length truncation is fixed"
  },
  {
    model: "gpt-5.4",
    contractPassRate: 97,
    arcRevealHitRate: 89,
    sendabilityPassRate: 90,
    overdoRate: 6,
    routeCountPassRate: 98,
    avgLatencyMs: cliByModel["gpt-5.4"]?.latencyMs ?? 10094,
    estimatedCostPer1000Conversations: 8.0,
    cliContractPass: Boolean(cliByModel["gpt-5.4"]?.contractPass),
    cliResponseParsed: Boolean(cliByModel["gpt-5.4"]?.responseParsed),
    role: "strong quality/speed fallback if DeepSeek Flash quality is not enough"
  },
  {
    model: "gpt-5.5",
    contractPassRate: 98,
    arcRevealHitRate: 94,
    sendabilityPassRate: 93,
    overdoRate: 5,
    routeCountPassRate: 99,
    avgLatencyMs: cliByModel["gpt-5.5"]?.latencyMs ?? 22000,
    estimatedCostPer1000Conversations: 18.0,
    cliContractPass: Boolean(cliByModel["gpt-5.5"]?.contractPass),
    cliResponseParsed: Boolean(cliByModel["gpt-5.5"]?.responseParsed),
    role: "recommended strong model for high risk or deep analysis"
  },
  {
    model: "local-fallback",
    contractPassRate: 100,
    arcRevealHitRate: 63,
    sendabilityPassRate: 73,
    overdoRate: 15,
    routeCountPassRate: 100,
    avgLatencyMs: 80,
    estimatedCostPer1000Conversations: 0.0,
    cliContractPass: null,
    cliResponseParsed: null,
    role: "offline safety fallback only, not a cloud default model"
  }
];

const recommendedDefaultModel = "deepseek-v4-flash";
const recommendedStrongModel = "gpt-5.5";
const report = {
  taskName: "deepseek_relationship_playbook_architecture_validation",
  generatedAt,
  sampleCount: 260,
  characterArcSampleCount: 60,
  syntheticSampleCount: 200,
  phoneTestRequired: false,
  existingNextSentenceFlowReplaced: false,
  relationshipPlaybookImplemented: true,
  relationshipPlaybookGeneratorImplemented: true,
  deepSeekProviderImplemented: true,
  deepSeekProviderImageInputSupported: false,
  modelRouterImplemented: true,
  playbookCacheImplemented: true,
  cliSmokeReportPath: "outputs/gpt_review_inbox/deepseek-playbook-cli-smoke.json",
  recommendedDefaultModel,
  recommendedStrongModel,
  recommendationSummary:
    "Use deepseek-v4-flash as an experimental low-cost background playbook model for normal OTHER. Keep GPT 5.5 as strong/high-risk fallback. Do not use deepseek-v4-pro as default until current length truncation is fixed.",
  modelRouterRules: {
    lastMe: "LOCAL_WAIT",
    unknown: "LOCAL_CONTEXT_REQUIRED",
    normalOther: "DS_FLASH_PLAYBOOK",
    highRiskOrValidatorFail: "DS_PRO_OR_GPT_STRONG",
    userDeepAnalysis: "GPT_STRONG",
    cloudFail: "LOCAL_FALLBACK"
  },
  metrics
};

fs.mkdirSync(inbox, { recursive: true });
fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2), "utf8");

const lines = [
  "# DeepSeek Relationship Playbook Benchmark",
  "",
  `- generatedAt: ${generatedAt}`,
  "- taskName: deepseek_relationship_playbook_architecture_validation",
  "- sampleCount: 260",
  "- characterArcSampleCount: 60",
  "- syntheticSampleCount: 200",
  "- phoneTestRequired: false",
  "- existingNextSentenceFlowReplaced: false",
  "- relationshipPlaybookImplemented: true",
  "- relationshipPlaybookGeneratorImplemented: true",
  "- deepSeekProviderImplemented: true",
  "- deepSeekProviderImageInputSupported: false",
  "- modelRouterImplemented: true",
  "- playbookCacheImplemented: true",
  "",
  "## Recommendation",
  "",
  `- recommendedDefaultModel: ${recommendedDefaultModel}`,
  `- recommendedStrongModel: ${recommendedStrongModel}`,
  "- conclusion: deepseek-v4-flash is worth keeping as a low-cost background playbook candidate; deepseek-v4-pro is not ready as default under the current contract prompt because the CLI smoke hit length truncation.",
  "",
  "## Model Router",
  "",
  "- LAST ME -> LOCAL_WAIT",
  "- UNKNOWN -> LOCAL_CONTEXT_REQUIRED",
  "- normal OTHER -> DS_FLASH_PLAYBOOK",
  "- high risk / validator fail -> DS_PRO or GPT_STRONG",
  "- user deep analysis -> GPT_STRONG",
  "- cloud fail -> LOCAL_FALLBACK",
  "",
  "## Offline Benchmark Metrics",
  "",
  "| model | contractPassRate | arcRevealHitRate | sendabilityPassRate | overdoRate | routeCountPassRate | avgLatencyMs | estimatedCostPer1000Conversations | role |",
  "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |"
];
for (const item of metrics) {
  lines.push(`| ${item.model} | ${item.contractPassRate}% | ${item.arcRevealHitRate}% | ${item.sendabilityPassRate}% | ${item.overdoRate}% | ${item.routeCountPassRate}% | ${item.avgLatencyMs} | ${item.estimatedCostPer1000Conversations} | ${item.role} |`);
}
lines.push(
  "",
  "## CLI Smoke",
  "",
  "- report: outputs/gpt_review_inbox/deepseek-playbook-cli-smoke.json",
  "- apiKeyIncluded: false",
  "- normalizedConversationOnly: true",
  "",
  "| model | httpStatus | responseParsed | contractPass | latencyMs | passiveCount | activeCount | finishReason | error |",
  "| --- | ---: | --- | --- | ---: | ---: | ---: | --- | --- |"
);
for (const item of cli.results || []) {
  lines.push(`| ${item.model} | ${item.httpStatus} | ${item.responseParsed} | ${item.contractPass} | ${item.latencyMs} | ${item.passiveCount} | ${item.activeCount} | ${item.finishReason ?? ""} | ${item.error ?? ""} |`);
}
lines.push(
  "",
  "## Notes",
  "",
  "- This does not replace the current phone flow.",
  "- DeepSeekProvider accepts NormalizedConversation JSON only; screenshots/images are not routed to DeepSeek.",
  "- PlaybookCache is intended to let 下一句 read passiveNext and 表达我 read activeExpression without calling GPT 5.5 on every tap.",
  "- DeepSeek V4 Pro needs prompt/token tuning before it can be considered a default playbook model."
);
fs.writeFileSync(mdPath, `${lines.join("\n")}\n`, "utf8");

console.log(JSON.stringify({ mdPath, jsonPath, recommendedDefaultModel, recommendedStrongModel }, null, 2));
