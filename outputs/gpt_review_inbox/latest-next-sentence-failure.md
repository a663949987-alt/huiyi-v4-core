# Latest Next Sentence Failure

## User-visible result
- shown message: NOT_TESTED_THIS_BUILD
- bubble still visible: unknown_without_physical_device
- permission warning shown: false

## Failure stage
- stage: NOT_TESTED
- errorCode: NOT_TESTED
- secondaryErrorCode: NOT_TESTED
- pipelineExceptionClass: NOT_TESTED
- pipelineExceptionMessageRedacted: NOT_TESTED
- likely cause: 用户真机上一版暴露出截图 capability 缺失；本轮已将截图降级为 optional diagnostic，本机未连接物理 Android，未复现真实点击。

## Window / root
- before click package: NOT_TESTED
- capture package: NOT_TESTED
- root package: NOT_TESTED
- root package at capture start: NOT_TESTED
- root package before failure UI: NOT_TESTED
- root package after failure UI: NOT_TESTED
- root title: NOT_TESTED
- root retry count: 0
- root available after retry: NOT_TESTED
- root is own overlay: false
- root is system ui: false

## Capture
- primaryCapturePath: NONE
- nodeTreeAttempted: NOT_TESTED
- nodeTreeSuccess: NOT_TESTED
- fallbackSnapshotAttempted: NOT_TESTED
- fallbackSnapshotSuccess: NOT_TESTED
- screenshotAttempted: NOT_TESTED
- screenshotSuccess: NOT_TESTED
- screenshotAvailable: NOT_TESTED
- screenshotCapabilityDeclared: true
- screenshotErrorCode: NOT_TESTED
- screenshotExceptionClass: NOT_TESTED
- screenshotExceptionMessageRedacted: NOT_TESTED
- captureSource: NONE
- used fallback snapshot: false
- last stable snapshot age: none
- raw node count: 0
- visible text count: 0
- parsed message count: 0
- effective message count: 0

## Decision
- last effective speaker: NOT_TESTED
- decision type: NOT_TESTED
- route count: 0
- api called: false

## Overlay
- bubble attached after click: NOT_TESTED
- bubble visible after failure: NOT_TESTED
- panel attached: NOT_TESTED
- panel render success: NOT_TESTED

## Recommended next fix
- 安装 4.1.9 后在真实聊起聊天页点击“下一句”，再导出手机 Downloads/Huiyi/review/latest-next-sentence-failure.md/json。
