# Latest Next Sentence Failure

## User-visible result
- shown message: NOT_TESTED_THIS_BUILD
- bubble still visible: unknown_without_physical_device
- permission warning shown: false

## Failure stage
- stage: NOT_TESTED
- errorCode: NOT_TESTED
- likely cause: 用户上一版真机看到“这次分析失败”，本轮已改为运行时生成具体 errorCode/failedStage；本机未连接物理 Android，未复现真实点击。

## Window / root
- before click package: NOT_TESTED
- capture package: NOT_TESTED
- root package: NOT_TESTED
- root title: NOT_TESTED
- root retry count: NOT_TESTED
- root available after retry: NOT_TESTED
- root is own overlay: NOT_TESTED
- root is system ui: NOT_TESTED

## Capture
- captureSource: NONE
- used fallback snapshot: NOT_TESTED
- last stable snapshot age: NOT_TESTED
- raw node count: NOT_TESTED
- visible text count: NOT_TESTED
- parsed message count: NOT_TESTED
- effective message count: NOT_TESTED

## Decision
- last effective speaker: NOT_TESTED
- decision type: NOT_TESTED
- route count: NOT_TESTED
- api called: false

## Overlay
- bubble attached after click: NOT_TESTED
- bubble visible after failure: NOT_TESTED
- panel attached: NOT_TESTED
- panel render success: NOT_TESTED

## Recommended next fix
- 安装 4.1.8b 后在真实聊起聊天页点击“下一句”，再导出手机 Downloads/Huiyi/review/latest-next-sentence-failure.md/json。
