import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const outputDir = path.join(root, "outputs", "gpt_review_inbox");
const availablePath = path.join(outputDir, "relay-available-models.json");
const jsonPath = path.join(outputDir, "relay-model-benchmark.json");
const mdPath = path.join(outputDir, "relay-model-benchmark-for-gpt.md");
const propsPath = fs.existsSync(path.join(root, "huiyi-cloud.properties"))
  ? path.join(root, "huiyi-cloud.properties")
  : path.join(root, "local.properties");

function readProps(file) {
  const out = {};
  if (!fs.existsSync(file)) return out;
  for (const line of fs.readFileSync(file, "utf8").split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const idx = trimmed.indexOf("=");
    if (idx < 0) continue;
    out[trimmed.slice(0, idx).trim()] = trimmed.slice(idx + 1).trim();
  }
  return out;
}

function chatUrl(baseUrl) {
  const clean = baseUrl.replace(/\/+$/, "");
  return clean.endsWith("/v1") ? `${clean}/chat/completions` : `${clean}/v1/chat/completions`;
}

function extractJson(text) {
  const trimmed = String(text || "").trim();
  if (!trimmed) throw new Error("empty_content");
  try {
    return JSON.parse(trimmed);
  } catch {
    const start = trimmed.indexOf("{");
    const end = trimmed.lastIndexOf("}");
    if (start >= 0 && end > start) return JSON.parse(trimmed.slice(start, end + 1));
    throw new Error("json_parse_failed");
  }
}

function routeItems(value) {
  const passive = Array.isArray(value?.passiveNext) ? value.passiveNext : [];
  const active = Array.isArray(value?.activeExpression) ? value.activeExpression : [];
  const arcRoutes = Array.isArray(value?.characterArcPlan?.routes) ? value.characterArcPlan.routes : [];
  return [...passive, ...active, ...arcRoutes];
}

function routeFamily(route) {
  return String(route?.routeFamily || route?.family || route?.type || route?.routeType || route?.title || "").toUpperCase();
}

function routeText(route) {
  return String(route?.text || route?.message || route?.line || route?.reply || "").trim();
}

function containsFamily(value, family) {
  return routeItems(value).some((route) => routeFamily(route).includes(family));
}

function isQuestion(text) {
  const clean = text.trim();
  return /[?？]$/.test(clean) || /(吗|呢|么|为什么|怎么|是不是|要不要|可以吗|方便吗)/.test(clean);
}

function overdo(text) {
  const clean = text.trim();
  return clean.length > 80 ||
    /(我保证|我一定|我永远|承诺|发誓|证明给你看|汇报一下|长篇|规划书|负责到底|我会给你一个未来)/.test(clean);
}

function sendable(text) {
  const clean = text.trim();
  if (clean.length < 3 || clean.length > 70) return false;
  if (/(作为AI|作为一个模型|建议你|你可以回复|以下是|路线|策略)/i.test(clean)) return false;
  if (overdo(clean)) return false;
  return true;
}

function validateContract(value, sample) {
  const required = [
    "stage",
    "currentFrame",
    "characterArcOpportunity",
    "passiveNext",
    "activeExpression",
    "characterArcPlan",
    "next2StepBranches",
    "risk",
    "fallback",
    "expiresWhen"
  ];
  const missing = required.filter((key) => value?.[key] === undefined || value?.[key] === null);
  const passive = Array.isArray(value?.passiveNext) ? value.passiveNext : [];
  const active = Array.isArray(value?.activeExpression) ? value.activeExpression : [];
  const routes = routeItems(value);
  const texts = routes.map(routeText).filter(Boolean);
  const routeCountPass = passive.length >= 3 && passive.length <= 5 && active.length >= 3 && active.length <= 5;
  const families = routes.map(routeFamily);
  const allReceiveRoutes = families.length > 0 && families.every((item) => item.includes("RECEIVE") || item.includes("EMPATHY") || item.includes("接"));
  const allQuestionRoutes = texts.length > 0 && texts.every(isQuestion);
  const overdoDetected = texts.some(overdo);
  const sendabilityPass = texts.length >= 6 && texts.every(sendable);
  const characterArcOpportunityExists = value?.characterArcOpportunity?.exists === true;
  const arcRevealHit = containsFamily(value, "ARC_REVEAL");
  const expressSelfHit = containsFamily(value, "EXPRESS_SELF") || containsFamily(value, "SELF");
  const coCreateHit = containsFamily(value, "CO_CREATE") || containsFamily(value, "CO_CREATION");
  const specialArcPass = !sample.requiresArc ||
    (characterArcOpportunityExists && arcRevealHit && expressSelfHit && coCreateHit && !allReceiveRoutes && !allQuestionRoutes);

  return {
    contractPass: missing.length === 0 && routeCountPass && specialArcPass,
    missing,
    routeCountPass,
    passiveCount: passive.length,
    activeCount: active.length,
    characterArcOpportunityExists,
    arcRevealHit,
    expressSelfHit,
    coCreateHit,
    sendabilityPass,
    overdoDetected,
    allReceiveRoutes,
    allQuestionRoutes,
    specialArcPass
  };
}

const samples = [
  ["A01", "ordinary", "好，我过会也去吃饭了", ["life", "low_pressure"], false],
  ["A02", "ordinary", "嗯，那我先忙一会。", ["busy", "low_expression"], false],
  ["A03", "ordinary", "好吧，晚点再说。", ["later", "boundary"], false],
  ["B01", "arc_planning", "这个事情也需要考虑好规划好才行。", ["reality", "planning", "future", "stability"], true],
  ["B02", "arc_planning", "我还是希望未来是稳定一点的。", ["future", "stability", "responsibility"], true],
  ["B03", "arc_planning", "有些话不能只是说说，还是要看怎么做。", ["reality", "responsibility"], true],
  ["C01", "past_experience", "今天你把它展示给我了。", ["past_experience", "true_feeling"], true],
  ["C02", "past_experience", "我能感觉到你以前也经历过不少。", ["past_experience", "understanding"], true],
  ["C03", "past_experience", "你刚刚那句话还挺真诚的。", ["true_feeling", "warmth"], true],
  ["D01", "expression_difficulty", "我是不知道怎么表达。", ["expression_difficulty", "emotion"], false],
  ["D02", "expression_difficulty", "我有时候说不出来自己想要什么。", ["expression_difficulty", "future"], true],
  ["D03", "expression_difficulty", "我怕说多了反而显得很矫情。", ["expression_difficulty", "risk"], false],
  ["E01", "work_transition_responsibility", "老班长都是为你好啊。", ["work", "transition", "responsibility"], true],
  ["E02", "work_transition_responsibility", "你以后转业也要提前想好方向。", ["transition", "planning", "future"], true],
  ["E03", "work_transition_responsibility", "责任感这个东西我还是挺看重的。", ["responsibility", "stability"], true],
  ["F01", "light_life", "我去给客户送衣服去了。", ["life", "work", "light"], false],
  ["F02", "light_life", "刚才路上有点堵，我到店里了。", ["life", "work"], false],
  ["F03", "light_life", "今天终于吃上一口热饭了。", ["life", "warmth"], false],
  ["G01", "read_no_reply", "我看到了，只是不知道怎么回。", ["read_no_reply", "expression_difficulty"], false],
  ["G02", "pressure", "我不想感觉像被逼着马上给答案。", ["pressure", "boundary"], false]
].map(([id, category, otherText, topics, requiresArc]) => ({
  id,
  category,
  requiresArc,
  normalizedConversation: {
    source: "relay_model_benchmark",
    lastSpeaker: "OTHER",
    currentTopics: topics,
    messages: [
      { speaker: "ME", text: "我听着，不急着逼你给答案。" },
      { speaker: "OTHER", text: otherText }
    ],
    lightChatState: {
      lastUserMessage: "我听着，不急着逼你给答案。",
      lastOtherMessage: otherText,
      recentMessageCount: 2
    },
    personaFacets: ["soldier", "transition", "responsibility", "steady_reality"],
    characterArcCards: [
      {
        id: "soldier-transition-arc",
        hiddenDepth: "稳、负责、不画饼，把现实里的事一点点做到位",
        revealTrigger: "现实 规划 稳定 未来 责任 过去经历 转业",
        safeRevealLine: "我可能不太会讲漂亮话，但认真起来会把事情一点点做到位。",
        overdoRisk: "不要讲成自我证明或经历汇报"
      }
    ],
    expected: {
      characterArcOpportunity: requiresArc,
      mustIncludeWhenArc: ["ARC_REVEAL", "EXPRESS_SELF", "CO_CREATE"]
    }
  }
}));

const preferred = [
  "deepseek-v4-flash",
  "qwen3.5-plus",
  "qwen3.5-flash",
  "kimi-k2.5",
  "glm-5",
  "gpt-5.4"
];
const optional = [
  "qwen3-max",
  "MiniMax-M2.5",
  "claude-sonnet-4-6",
  "gemini-3-flash-official"
];
const costScore = {
  "deepseek-v4-flash": 1.0,
  "qwen3.5-flash": 1.2,
  "qwen3.5-plus": 2.0,
  "kimi-k2.5": 2.5,
  "glm-5": 2.0,
  "qwen3-max": 3.5,
  "MiniMax-M2.5": 2.2,
  "claude-sonnet-4-6": 12.0,
  "gemini-3-flash-official": 2.0,
  "gpt-5.4": 8.0
};

const props = readProps(propsPath);
const baseUrl = props["huiyi.relay.baseUrl"] || props["relay.baseUrl"] || "https://toapis.com/v1";
const apiKey = props["huiyi.relay.apiKey"] || props["relay.apiKey"] || "";
const available = fs.existsSync(availablePath)
  ? JSON.parse(fs.readFileSync(availablePath, "utf8"))
  : { availableTextModels: [] };
const availableSet = new Set(available.availableTextModels || []);
const modelFilter = (process.env.RELAY_BENCH_MODELS || "")
  .split(",")
  .map((item) => item.trim())
  .filter(Boolean);
const testedModels = (modelFilter.length ? modelFilter : [...preferred, ...optional])
  .filter((model) => availableSet.has(model));
const generatedAt = new Date().toISOString();
const concurrency = Number.parseInt(process.env.RELAY_BENCH_CONCURRENCY || "4", 10);

const systemPrompt = [
  "你是会意 RelationshipPlaybookGenerator。",
  "只返回 JSON，不要 Markdown，不要解释。",
  "必须字段：stage,currentFrame,characterArcOpportunity,passiveNext,activeExpression,characterArcPlan,next2StepBranches,risk,fallback,expiresWhen。",
  "passiveNext 和 activeExpression 各 3-5 条。",
  "每条路线字段：routeFamily,title,text。",
  "routeFamily 只能用 RECEIVE, EXPRESS_SELF, ARC_REVEAL, CO_CREATE, LIGHTEN_MOOD, WITHDRAW。",
  "如果话题涉及现实、规划、稳定、未来、责任、过去经历，characterArcOpportunity.exists 必须为 true，路线必须包含 ARC_REVEAL、EXPRESS_SELF、CO_CREATE。",
  "不要五条全是接话，不要五条全是问句，不要油腻承诺，不要自我汇报。",
  "回复要像真人能直接发出去，短、自然、有关系推进。"
].join("\n");

async function runSample(model, sample) {
  const started = Date.now();
  const item = {
    sampleId: sample.id,
    category: sample.category,
    requiresArc: sample.requiresArc,
    httpStatus: 0,
    responseParsed: false,
    contractPass: false,
    routeCountPass: false,
    passiveCount: 0,
    activeCount: 0,
    characterArcOpportunityExists: false,
    arcRevealHit: false,
    expressSelfHit: false,
    coCreateHit: false,
    sendabilityPass: false,
    overdoDetected: false,
    allReceiveRoutes: false,
    allQuestionRoutes: false,
    latencyMs: 0,
    finishReason: null,
    error: null
  };
  try {
    if (!apiKey) throw new Error("api_key_missing");
    const response = await fetch(chatUrl(baseUrl), {
      method: "POST",
      headers: {
        Authorization: `Bearer ${apiKey}`,
        "Content-Type": "application/json"
      },
      signal: AbortSignal.timeout(35_000),
      body: JSON.stringify({
        model,
        temperature: 0.25,
        max_tokens: 1000,
        response_format: { type: "json_object" },
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: JSON.stringify(sample.normalizedConversation) }
        ]
      })
    });
    item.httpStatus = response.status;
    const body = await response.text();
    item.latencyMs = Date.now() - started;
    if (!response.ok) throw new Error(`http_${response.status}`);
    const payload = JSON.parse(body);
    item.finishReason = payload.choices?.[0]?.finish_reason || null;
    const parsed = extractJson(payload.choices?.[0]?.message?.content || "");
    item.responseParsed = true;
    Object.assign(item, validateContract(parsed, sample));
  } catch (error) {
    item.latencyMs = Date.now() - started;
    item.error = error?.name === "TimeoutError" ? "timeout" : error?.message || String(error);
  }
  return item;
}

function pct(numerator, denominator) {
  if (!denominator) return 0;
  return Math.round((numerator / denominator) * 100);
}

function aggregate(model, sampleResults) {
  const count = sampleResults.length;
  const arcSamples = sampleResults.filter((item) => item.requiresArc);
  const statuses = sampleResults.map((item) => item.httpStatus).filter(Boolean);
  const errors = sampleResults.filter((item) => item.error).map((item) => item.error);
  const finishReasons = sampleResults.map((item) => item.finishReason).filter(Boolean);
  const disabledReasons = [];
  const responseParsedRate = pct(sampleResults.filter((item) => item.responseParsed).length, count);
  const contractPassRate = pct(sampleResults.filter((item) => item.contractPass).length, count);
  const routeCountPassRate = pct(sampleResults.filter((item) => item.routeCountPass).length, count);
  const sendabilityPassRate = pct(sampleResults.filter((item) => item.sendabilityPass).length, count);
  const arcRevealHitRate = pct(arcSamples.filter((item) => item.arcRevealHit).length, arcSamples.length);
  const expressSelfHitRate = pct(arcSamples.filter((item) => item.expressSelfHit).length, arcSamples.length);
  const coCreateHitRate = pct(arcSamples.filter((item) => item.coCreateHit).length, arcSamples.length);
  const overdoRate = pct(sampleResults.filter((item) => item.overdoDetected).length, count);
  const allReceiveRoutesRate = pct(sampleResults.filter((item) => item.allReceiveRoutes).length, count);
  const allQuestionRoutesRate = pct(sampleResults.filter((item) => item.allQuestionRoutes).length, count);
  const avgLatencyMs = Math.round(sampleResults.reduce((sum, item) => sum + item.latencyMs, 0) / count);
  const avgPassiveCount = Math.round((sampleResults.reduce((sum, item) => sum + item.passiveCount, 0) / count) * 10) / 10;
  const avgActiveCount = Math.round((sampleResults.reduce((sum, item) => sum + item.activeCount, 0) / count) * 10) / 10;
  const highBadStatusRate = pct(statuses.filter((status) => status >= 400).length, count);
  if (finishReasons.includes("length")) disabledReasons.push("finishReason=length");
  if (responseParsedRate < 90) disabledReasons.push("responseParsedRate<90");
  if (contractPassRate < 90) disabledReasons.push("contractPassRate<90");
  if (avgLatencyMs > 25_000) disabledReasons.push("avgLatencyMs>25000");
  if (highBadStatusRate >= 20) disabledReasons.push("http_error_rate_high");
  if (model === "deepseek-v4-pro") disabledReasons.push("ds_pro_not_runtime_candidate");
  const runtimeEnabled = disabledReasons.length === 0;

  return {
    model,
    runtimeEnabled,
    disabledReasons,
    sampleCount: count,
    httpStatus: [...new Set(statuses)],
    responseParsedRate,
    contractPassRate,
    routeCountPassRate,
    avgPassiveCount,
    avgActiveCount,
    arcRevealHitRate,
    expressSelfHitRate,
    coCreateHitRate,
    sendabilityPassRate,
    overdoRate,
    allReceiveRoutesRate,
    allQuestionRoutesRate,
    avgLatencyMs,
    finishReasons: [...new Set(finishReasons)],
    errorSummary: [...new Set(errors)].slice(0, 5),
    estimatedCostScore: costScore[model] ?? 5.0,
    sampleResults
  };
}

function eligibleDefault(metric) {
  return metric.runtimeEnabled &&
    metric.contractPassRate >= 90 &&
    metric.routeCountPassRate >= 90 &&
    metric.sendabilityPassRate >= 80 &&
    metric.arcRevealHitRate >= 70 &&
    metric.overdoRate <= 15 &&
    metric.avgLatencyMs <= 12_000 &&
    metric.estimatedCostScore < (costScore["gpt-5.4"] ?? 8.0);
}

function eligibleStrong(metric) {
  return metric.runtimeEnabled &&
    metric.contractPassRate >= 95 &&
    metric.arcRevealHitRate >= 85 &&
    metric.sendabilityPassRate >= 88;
}

fs.mkdirSync(outputDir, { recursive: true });

async function runModelSamples(model) {
  const sampleResults = new Array(samples.length);
  let cursor = 0;
  async function worker() {
    while (cursor < samples.length) {
      const index = cursor;
      cursor += 1;
      const sample = samples[index];
      const result = await runSample(model, sample);
      sampleResults[index] = result;
      console.log(`MODEL_SAMPLE ${model} ${sample.id} contract=${result.contractPass} latency=${result.latencyMs} err=${result.error ?? ""}`);
    }
  }
  const workers = Array.from({ length: Math.max(1, Math.min(concurrency, samples.length)) }, () => worker());
  await Promise.all(workers);
  return sampleResults;
}

function buildReport(perModelMetrics, rawResults, completed) {
  const disabledModels = [
    ...perModelMetrics
      .filter((metric) => !metric.runtimeEnabled)
      .map((metric) => ({ model: metric.model, disabledReasons: metric.disabledReasons })),
    {
      model: "deepseek-v4-pro",
      disabledReasons: ["previous_smoke_finishReason=length", "previous_smoke_responseParsed=false", "not_allowed_in_runtime_this_round"]
    }
  ].filter((item, index, arr) => arr.findIndex((other) => other.model === item.model) === index);

  const defaultCandidates = perModelMetrics
    .filter(eligibleDefault)
    .sort((a, b) => (a.estimatedCostScore - b.estimatedCostScore) || (a.avgLatencyMs - b.avgLatencyMs) || (b.sendabilityPassRate - a.sendabilityPassRate));
  const strongCandidates = perModelMetrics
    .filter(eligibleStrong)
    .sort((a, b) => (b.arcRevealHitRate + b.sendabilityPassRate + b.contractPassRate - b.overdoRate) - (a.arcRevealHitRate + a.sendabilityPassRate + a.contractPassRate - a.overdoRate));

  const recommendedDefaultModel = defaultCandidates[0]?.model || null;
  const recommendedStrongModel = strongCandidates[0]?.model || null;
  const closestStrongReference = perModelMetrics
    .filter((metric) => metric.model === "gpt-5.4" || metric.model === "MiniMax-M2.5" || metric.model === "kimi-k2.5")
    .sort((a, b) => (b.contractPassRate + b.arcRevealHitRate + b.sendabilityPassRate - b.overdoRate) - (a.contractPassRate + a.arcRevealHitRate + a.sendabilityPassRate - a.overdoRate))[0]?.model || null;
  const arcTriggerSample = rawResults[recommendedDefaultModel || testedModels[0]]?.find((item) => item.sampleId === "B01");

  return {
    taskName: "relay_model_benchmark_default_playbook_model_search",
    generatedAt,
    completed,
    apiKeyIncluded: false,
    baseUrlConfigured: Boolean(baseUrl),
    availableModels: available.availableTextModels || [],
    testedModels,
    completedModels: perModelMetrics.map((metric) => metric.model),
    sampleCountPerModel: samples.length,
    perModelMetrics,
    arcTriggerSmokeResult: arcTriggerSample || null,
    recommendedDefaultModel,
    recommendedStrongModel,
    closestStrongReference,
    disabledModels,
    disabledReasons: Object.fromEntries(disabledModels.map((item) => [item.model, item.disabledReasons])),
    recommendedRouterPolicy: {
      LAST_ME: "LOCAL_WAIT",
      UNKNOWN: "LOCAL_CONTEXT_REQUIRED",
      "normal OTHER": recommendedDefaultModel || "KEEP_CURRENT_RUNTIME_MODEL",
      "arc/reality/planning/future": recommendedDefaultModel || recommendedStrongModel || "KEEP_CURRENT_RUNTIME_MODEL",
      "validator fail": recommendedStrongModel || "KEEP_CURRENT_STRONG_MODEL",
      "strongModel fail": "LOCAL_FALLBACK",
      timeout: "LOCAL_FALLBACK"
    }
  };
}

function writeReports(perModelMetrics, rawResults, completed) {
  const report = buildReport(perModelMetrics, rawResults, completed);
  fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2), "utf8");
  const lines = [
    "# Relay Model Benchmark",
    "",
    `- generatedAt: ${generatedAt}`,
    "- taskName: relay_model_benchmark_default_playbook_model_search",
    `- completed: ${completed}`,
    "- apiKeyIncluded: false",
    `- availableModelCount: ${(available.availableTextModels || []).length}`,
    `- testedModels: ${testedModels.join(", ")}`,
    `- completedModels: ${report.completedModels.join(", ")}`,
    `- sampleCountPerModel: ${samples.length}`,
    `- recommendedDefaultModel: ${report.recommendedDefaultModel || "NONE"}`,
    `- recommendedStrongModel: ${report.recommendedStrongModel || "NONE"}`,
    `- closestStrongReference: ${report.closestStrongReference || "NONE"}`,
    "",
    "## Per Model Metrics",
    "",
    "| model | runtimeEnabled | contractPassRate | routeCountPassRate | sendabilityPassRate | arcRevealHitRate | expressSelfHitRate | coCreateHitRate | overdoRate | allReceiveRoutesRate | allQuestionRoutesRate | avgLatencyMs | costScore | disabledReasons |",
    "| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |"
  ];
  for (const metric of report.perModelMetrics) {
    lines.push(`| ${metric.model} | ${metric.runtimeEnabled} | ${metric.contractPassRate}% | ${metric.routeCountPassRate}% | ${metric.sendabilityPassRate}% | ${metric.arcRevealHitRate}% | ${metric.expressSelfHitRate}% | ${metric.coCreateHitRate}% | ${metric.overdoRate}% | ${metric.allReceiveRoutesRate}% | ${metric.allQuestionRoutesRate}% | ${metric.avgLatencyMs} | ${metric.estimatedCostScore} | ${metric.disabledReasons.join("; ")} |`);
  }
  lines.push(
    "",
    "## Arc Trigger Smoke",
    "",
    "- sample: B01 / planning-reality arc trigger",
    `- evaluatedModel: ${report.recommendedDefaultModel || testedModels[0] || ""}`,
    `- result: ${report.arcTriggerSmokeResult ? JSON.stringify({
      contractPass: report.arcTriggerSmokeResult.contractPass,
      characterArcOpportunityExists: report.arcTriggerSmokeResult.characterArcOpportunityExists,
      arcRevealHit: report.arcTriggerSmokeResult.arcRevealHit,
      expressSelfHit: report.arcTriggerSmokeResult.expressSelfHit,
      coCreateHit: report.arcTriggerSmokeResult.coCreateHit,
      allReceiveRoutes: report.arcTriggerSmokeResult.allReceiveRoutes,
      allQuestionRoutes: report.arcTriggerSmokeResult.allQuestionRoutes
    }) : "NOT_AVAILABLE"}`,
    "",
    "## Disabled Models",
    ""
  );
  for (const item of report.disabledModels) {
    lines.push(`- ${item.model}: ${item.disabledReasons.join(", ")}`);
  }
  lines.push(
    "",
    "## Recommended Router Policy",
    "",
    "- LAST_ME -> LOCAL_WAIT",
    "- UNKNOWN -> LOCAL_CONTEXT_REQUIRED",
    `- normal OTHER -> ${report.recommendedDefaultModel || "KEEP_CURRENT_RUNTIME_MODEL"}`,
    `- arc/reality/planning/future -> ${report.recommendedDefaultModel || report.recommendedStrongModel || "KEEP_CURRENT_RUNTIME_MODEL"}`,
    `- validator fail -> ${report.recommendedStrongModel || "KEEP_CURRENT_STRONG_MODEL"}`,
    "- strongModel fail -> LOCAL_FALLBACK",
    "- timeout -> LOCAL_FALLBACK",
    "",
    "## Notes",
    "",
    "- DS Pro is not allowed into runtime in this round.",
    "- No screenshots were sent; every request used NormalizedConversation JSON.",
    "- This benchmark does not change App UI and does not require phone testing."
  );
  fs.writeFileSync(mdPath, `${lines.join("\n")}\n`, "utf8");
  return report;
}

const perModelMetrics = [];
const rawResults = {};
for (const model of testedModels) {
  console.log(`MODEL_START ${model}`);
  const sampleResults = await runModelSamples(model);
  rawResults[model] = sampleResults;
  perModelMetrics.push(aggregate(model, sampleResults));
  writeReports(perModelMetrics, rawResults, false);
  console.log(`MODEL_DONE ${model}`);
}

const report = writeReports(perModelMetrics, rawResults, true);

console.log(JSON.stringify({
  jsonPath,
  mdPath,
  testedModels,
  recommendedDefaultModel: report.recommendedDefaultModel,
  recommendedStrongModel: report.recommendedStrongModel,
  disabledModels: report.disabledModels
}, null, 2));
