# MockChatLab nova 11 模拟器状态

## 结论

已创建并启动 nova 11 近似模拟器：

`Huawei_nova_11_API_36`

模拟器参数：

- 设备名：Huawei nova 11
- 分辨率：`1084x2412`
- 密度：`395`
- 系统镜像：Android 36 Google APIs x86_64
- 说明：官方 Android Emulator 不能真实模拟 HarmonyOS，只能近似华为 nova 11 的屏幕尺寸和布局环境。

## 已安装

- 会意：`com.huiyi.v4`
- MockChatLab：`com.huiyi.mockchat`

当前前台：

`com.huiyi.mockchat/.MainActivity`

已打开场景：

`last_other`

## 权限

- 会意悬浮窗：已允许
- 会意无障碍服务：已启用

## 截图样本

已保存：

`outputs/mockchat_screenshots/mockchat_last_other.png`

## 下一步

点击会意悬浮球“下一句”，确认结果浮层显示在 MockChatLab 上方，而不是打开会意 MainActivity。
