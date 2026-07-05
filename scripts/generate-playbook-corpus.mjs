import fs from "fs";
import path from "path";

const outDir = path.join("tools", "playbook_corpus");
fs.mkdirSync(outDir, { recursive: true });

const categories = [
  ["ordinary", "好，我过会也去吃饭了", "RECEIVE_OTHER"],
  ["last_me", "我已经回她一句了，现在先等她。", "WAIT"],
  ["reality_planning", "这个事情也需要考虑好规划好才行。", "ARC_REVEAL"],
  ["stability", "我其实更看重稳定和踏实。", "ARC_REVEAL"],
  ["future", "以后怎么走还是要想清楚。", "CO_CREATE_MEANING"],
  ["past", "今天你把它展示给我了。", "ARC_REVEAL"],
  ["true_feeling", "你这样说我会有点不知道怎么接。", "EXPRESS_SELF"],
  ["expression_difficulty", "我是不知道怎么表达。", "EXPRESS_SELF"],
  ["work_pressure", "今天工作太多了，客户一直催。", "RECEIVE_OTHER"],
  ["transition", "转业以后很多事都要重新适应。", "ARC_REVEAL"],
  ["army", "老班长都是为你好啊。", "ARC_REVEAL"],
  ["responsibility", "我觉得责任感真的很重要。", "ARC_REVEAL"],
  ["kids", "带孩子真的很耗精力。", "RECEIVE_OTHER"],
  ["read_no_reply", "刚刚我看到已读了，但不知道怎么回。", "WITHDRAW"],
  ["light_life", "我去给客户送衣服去了。", "RECEIVE_OTHER"],
  ["topic_shift", "算了，先不聊这个了。", "WITHDRAW"],
  ["other_retreat", "嗯，好，晚点说吧。", "WITHDRAW"],
  ["user_just_expressed", "我刚刚已经表达过自己的态度了。", "WAIT"]
];

const scenarios = [];
let id = 1;
for (const [category, otherText, expectedNextMove] of categories) {
  for (let i = 0; i < 5; i += 1) {
    const lastSpeaker = category === "last_me" || category === "user_just_expressed" ? "ME" : "OTHER";
    scenarios.push({
      scenarioId: `pb-${String(id).padStart(3, "0")}`,
      category,
      appPackage: "com.bajiao.im.liaoqi",
      windowTitle: `corpus-${category}`,
      messages: lastSpeaker === "ME"
        ? [
            { speaker: "OTHER", text: otherText },
            { speaker: "ME", text: "我明白你的意思，我先按稳一点的节奏来。" }
          ]
        : [
            { speaker: "ME", text: "我在听，你慢慢说。" },
            { speaker: "OTHER", text: `${otherText}${i % 2 === 0 ? "" : " 我想看看你会怎么处理。"}` }
          ],
      expected: {
        lastSpeaker,
        nextSentenceDecisionType: lastSpeaker === "ME" ? "WAIT" : "NORMAL_REPLY",
        nextSentenceRouteCountMin: lastSpeaker === "ME" ? 0 : 3,
        nextSentenceRouteCountMax: lastSpeaker === "ME" ? 0 : 5,
        expressSelfAllowed: lastSpeaker === "OTHER",
        expectedNextMove,
        expectedArcReveal: ["reality_planning", "stability", "past", "transition", "army", "responsibility"].includes(category),
        cloudBlockingAllowed: false,
        rawPrivateChat: false
      }
    });
    id += 1;
  }
}

const expected = scenarios.map((item) => ({
  scenarioId: item.scenarioId,
  category: item.category,
  expected: item.expected
}));

fs.writeFileSync(
  path.join(outDir, "scenarios.json"),
  JSON.stringify({ generatedBy: "generate-playbook-corpus.mjs", sampleCount: scenarios.length, scenarios }, null, 2),
  "utf8"
);
fs.writeFileSync(
  path.join(outDir, "expected.json"),
  JSON.stringify({ generatedBy: "generate-playbook-corpus.mjs", sampleCount: expected.length, expected }, null, 2),
  "utf8"
);

console.log(`generated ${scenarios.length} playbook corpus samples`);
