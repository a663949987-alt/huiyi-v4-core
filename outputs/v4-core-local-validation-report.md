# 会意 v4 Core 本地验证报告

## 构建环境

- JDK：17.0.19
- Android SDK：`C:\Users\fbjdf\AppData\Local\Android\Sdk`
- Gradle Wrapper：8.8
- compileSdk：35
- minSdk：29

## 验证结果

- `testDebugUnitTest`：通过
- `assembleDebug`：通过
- `assembleRelease`：通过，产物为未签名 release APK

## APK

- 可实机安装 APK：`outputs/huiyi-v4.0-release-preconfigured.apk`
- update_server APK：`outputs/update_server/huiyi-v4.0-release-preconfigured.apk`
- SHA-256：`1A57423C36E26BD458E44DCB361500826F0083FB4AF978739579FEE61D27A20A`

## 说明

当前没有配置正式 keystore，因此 release 构建生成的是 `app-release-unsigned.apk`，不作为本轮实机交付文件。已按任务书要求输出可安装 debug APK，并在报告中说明原因。
