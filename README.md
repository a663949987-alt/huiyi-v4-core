# Huiyi v4 Core

会意 v4 Core 是一个从零开始的 Android 原生 Kotlin 项目。它不是 v3 修补版，也不是聊天话术工具、画像软件或情感报告，而是面向当前聊天窗口的关系战术 HUD。

## 项目定位

第一主路径是“当前屏幕开挂”：用户打开聊天窗口，点击悬浮入口，读取当前屏幕，在必要时向上补读 1-3 屏，组装当前聊天场景，判断最后一句是谁，并生成战术判断和 5 条回复路线。

## 基石假设

关系不是由聊天本身建立的。关系是两个人通过聊天，共同创造出一个原本不存在的共同意义。会意识别共创点、判断局势、预判错误，并记录反馈。

## 本地构建

1. 安装 Android Studio、Android SDK、JDK 17。
2. 确认 `local.properties` 里的 `sdk.dir` 指向本机 SDK。
3. 执行：

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

## API 配置

真实模型调用只允许通过 `local.properties` 或本地配置注入：

```properties
huiyi.api.baseUrl=https://toapis.com/v1
huiyi.api.key=你的本地 API Key
huiyi.api.model=gpt-5.5
```

默认流程使用 `FakeModelProvider`，测试默认不调用真实 API。只有显式设置 `RUN_LIVE_API_TESTS=true` 时才允许真实调用。

## 更新包生成

构建任务会预留以下输出：

- `outputs/update_server/latest.json`
- `outputs/update_server/huiyi-v4.0-release-preconfigured.apk`
- `outputs/huiyi-v4.0-release-preconfigured.apk`

App 内更新不能静默安装，下载完成后必须由用户确认安装。

## 调试报告

实现完成后输出：

- `outputs/v4-core-implementation-report-for-gpt.md`
- `outputs/v4-core-local-validation-report.md`

普通用户页面禁止展示 UUID、batchId、parserName、raw JSON、stackTrace、reasoning_content 等调试字段。

## 安全注意事项

不要提交 API Key、`local.properties`、keystore、签名配置、构建产物、截图、原始聊天记录、画像验证输出、OCR 截图或任何用户隐私数据。
