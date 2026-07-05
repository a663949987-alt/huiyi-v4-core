import fs from "node:fs";
import path from "node:path";

const root = process.cwd();
const outputDir = path.join(root, "outputs", "gpt_review_inbox");
const jsonPath = path.join(outputDir, "relay-available-models.json");
const mdPath = path.join(outputDir, "relay-available-models-for-gpt.md");
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

function modelsUrl(baseUrl) {
  const clean = baseUrl.replace(/\/+$/, "");
  return clean.endsWith("/v1") ? `${clean}/models` : `${clean}/v1/models`;
}

function likelyTextChatModel(model) {
  const id = String(model.id || model.name || "").toLowerCase();
  const joined = JSON.stringify(model).toLowerCase();
  if (!id) return false;
  if (/(embedding|tts|audio|whisper|image|vision|ocr|rerank|moderation|speech)/.test(id)) return false;
  if (joined.includes("chat.completions") || joined.includes("chat_completions")) return true;
  if (joined.includes("responses") && !joined.includes("image")) return true;
  return /(gpt|deepseek|qwen|kimi|glm|claude|gemini|minimax|yi-|hunyuan|doubao|ernie|moonshot)/.test(id);
}

const props = readProps(propsPath);
const baseUrl = props["huiyi.relay.baseUrl"] || props["relay.baseUrl"] || "https://toapis.com/v1";
const apiKey = props["huiyi.relay.apiKey"] || props["relay.apiKey"] || "";
const generatedAt = new Date().toISOString();

fs.mkdirSync(outputDir, { recursive: true });

const report = {
  taskName: "relay_model_benchmark_default_playbook_model_search",
  generatedAt,
  baseUrlConfigured: Boolean(baseUrl),
  apiKeyIncluded: false,
  rawModelCount: 0,
  availableTextModels: [],
  models: [],
  error: null
};

try {
  if (!apiKey) throw new Error("api_key_missing");
  const response = await fetch(modelsUrl(baseUrl), {
    headers: { Authorization: `Bearer ${apiKey}` }
  });
  report.httpStatus = response.status;
  const body = await response.text();
  if (!response.ok) throw new Error(`http_${response.status}`);
  const payload = JSON.parse(body);
  const data = Array.isArray(payload.data) ? payload.data : Array.isArray(payload) ? payload : [];
  report.rawModelCount = data.length;
  report.models = data.map((model) => ({
    id: String(model.id || model.name || ""),
    object: model.object || null,
    ownedBy: model.owned_by || model.ownedBy || null,
    created: model.created || null,
    supportsChatCompletions: likelyTextChatModel(model)
  })).filter((model) => model.id);
  report.availableTextModels = report.models
    .filter((model) => model.supportsChatCompletions)
    .map((model) => model.id);
} catch (error) {
  report.error = error?.message || String(error);
}

fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2), "utf8");

const lines = [
  "# Relay Available Models",
  "",
  `- generatedAt: ${generatedAt}`,
  "- apiKeyIncluded: false",
  `- baseUrlConfigured: ${report.baseUrlConfigured}`,
  `- httpStatus: ${report.httpStatus ?? ""}`,
  `- rawModelCount: ${report.rawModelCount}`,
  `- availableTextModelCount: ${report.availableTextModels.length}`,
  `- error: ${report.error ?? ""}`,
  "",
  "## availableTextModels",
  ""
];
for (const model of report.availableTextModels) {
  lines.push(`- ${model}`);
}
lines.push("", "## Model Table", "", "| model | supportsChatCompletions | ownedBy |", "| --- | --- | --- |");
for (const model of report.models) {
  lines.push(`| ${model.id} | ${model.supportsChatCompletions} | ${model.ownedBy ?? ""} |`);
}
fs.writeFileSync(mdPath, `${lines.join("\n")}\n`, "utf8");

console.log(JSON.stringify({ jsonPath, mdPath, availableTextModelCount: report.availableTextModels.length, error: report.error }, null, 2));
