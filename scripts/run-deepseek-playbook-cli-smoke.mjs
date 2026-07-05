import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const propsPath = fs.existsSync(path.join(root, "huiyi-cloud.properties"))
  ? path.join(root, "huiyi-cloud.properties")
  : path.join(root, "local.properties");
const outputDir = path.join(root, "outputs", "gpt_review_inbox");
const jsonPath = path.join(outputDir, "deepseek-playbook-cli-smoke.json");
const mdPath = path.join(outputDir, "deepseek-playbook-cli-smoke-for-gpt.md");

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
  const trimmed = text.trim();
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

function validatePlaybook(value) {
  const required = [
    "stage",
    "currentFrame",
    "passiveNext",
    "activeExpression",
    "characterArcPlan",
    "next2StepBranches",
    "risk",
    "fallback",
    "expiresWhen"
  ];
  const missing = required.filter((key) => value[key] === undefined || value[key] === null);
  const passiveCount = Array.isArray(value.passiveNext) ? value.passiveNext.length : 0;
  const activeCount = Array.isArray(value.activeExpression) ? value.activeExpression.length : 0;
  const arcCount = Array.isArray(value.characterArcPlan?.arcRevealRoutes)
    ? value.characterArcPlan.arcRevealRoutes.length
    : Array.isArray(value.activeExpression)
      ? value.activeExpression.filter((route) => String(route.routeFamily || route.type || "").includes("ARC")).length
      : 0;
  return {
    pass: missing.length === 0 && passiveCount >= 3 && activeCount >= 3,
    missing,
    passiveCount,
    activeCount,
    arcCount
  };
}

const props = readProps(propsPath);
const baseUrl = props["huiyi.relay.baseUrl"] || props["relay.baseUrl"] || "https://toapis.com/v1";
const apiKey = props["huiyi.relay.apiKey"] || props["relay.apiKey"] || "";
const generatedAt = new Date().toISOString();
const models = ["deepseek-v4-flash", "deepseek-v4-pro", "gpt-5.4", "gpt-5.5"];
const normalizedConversation = {
  source: "offline_cli_smoke",
  currentTopics: ["reality", "future", "stability", "responsibility"],
  lastSpeaker: "OTHER",
  messages: [
    { speaker: "ME", text: "I hear you. I do not want to rush you." },
    { speaker: "OTHER", text: "I care about future and stability. I do not want someone who only talks." }
  ],
  personaFacets: ["soldier", "transition", "responsibility"],
  expectedOutput: "RelationshipPlaybook"
};

const systemPrompt = [
  "You are Huiyi RelationshipPlaybookGenerator.",
  "Return strict JSON only.",
  "Schema fields: stage, currentFrame, passiveNext, activeExpression, characterArcPlan, next2StepBranches, risk, fallback, expiresWhen.",
  "passiveNext and activeExpression must each include 3-5 short Chinese routes.",
  "characterArcPlan must include whether ARC_REVEAL is suitable.",
  "No images. Use only the normalized conversation JSON."
].join(" ");

async function runModel(model) {
  const started = Date.now();
  const result = {
    model,
    httpStatus: 0,
    latencyMs: 0,
    responseParsed: false,
    contractPass: false,
    passiveCount: 0,
    activeCount: 0,
    arcCount: 0,
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
      body: JSON.stringify({
        model,
        max_tokens: 1200,
        temperature: 0.25,
        response_format: { type: "json_object" },
        messages: [
          { role: "system", content: systemPrompt },
          { role: "user", content: JSON.stringify(normalizedConversation) }
        ]
      })
    });
    result.httpStatus = response.status;
    const body = await response.text();
    result.latencyMs = Date.now() - started;
    if (!response.ok) throw new Error(`http_${response.status}`);
    const payload = JSON.parse(body);
    const content = payload.choices?.[0]?.message?.content || "";
    result.finishReason = payload.choices?.[0]?.finish_reason || null;
    const parsed = extractJson(content);
    result.responseParsed = true;
    const validation = validatePlaybook(parsed);
    result.contractPass = validation.pass;
    result.passiveCount = validation.passiveCount;
    result.activeCount = validation.activeCount;
    result.arcCount = validation.arcCount;
    if (!validation.pass) result.error = `contract_missing:${validation.missing.join(",")}`;
  } catch (error) {
    result.latencyMs = Date.now() - started;
    result.error = error?.message || String(error);
  }
  return result;
}

fs.mkdirSync(outputDir, { recursive: true });

const results = [];
for (const model of models) {
  // Sequential calls keep relay pressure low and make latency easier to compare.
  results.push(await runModel(model));
}

const report = {
  taskName: "deepseek_relationship_playbook_architecture_validation",
  generatedAt,
  apiKeyIncluded: false,
  baseUrlConfigured: Boolean(baseUrl),
  models,
  normalizedConversationOnly: true,
  results
};
fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2), "utf8");

const lines = [
  "# DeepSeek Playbook CLI Smoke",
  "",
  `- generatedAt: ${generatedAt}`,
  "- apiKeyIncluded: false",
  `- baseUrlConfigured: ${Boolean(baseUrl)}`,
  "- normalizedConversationOnly: true",
  "",
  "| model | httpStatus | responseParsed | contractPass | latencyMs | passiveCount | activeCount | arcCount | finishReason | error |",
  "| --- | ---: | --- | --- | ---: | ---: | ---: | ---: | --- | --- |"
];
for (const item of results) {
  lines.push(
    `| ${item.model} | ${item.httpStatus} | ${item.responseParsed} | ${item.contractPass} | ${item.latencyMs} | ${item.passiveCount} | ${item.activeCount} | ${item.arcCount} | ${item.finishReason ?? ""} | ${item.error ?? ""} |`
  );
}
fs.writeFileSync(mdPath, `${lines.join("\n")}\n`, "utf8");

console.log(JSON.stringify({ jsonPath, mdPath, results }, null, 2));
