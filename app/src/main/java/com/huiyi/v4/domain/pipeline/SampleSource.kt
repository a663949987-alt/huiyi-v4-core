package com.huiyi.v4.domain.pipeline

enum class SampleSource(val reportValue: String) {
    LOCAL_VALIDATION_SAMPLE("local_validation_sample"),
    FAKE_MODEL_SAMPLE("fake_model_sample"),
    EMULATOR_MOCK_CHAT_ACCESSIBILITY("emulator_mock_chat_accessibility"),
    REAL_DEVICE_ACCESSIBILITY("real_device_accessibility"),
    REAL_DEVICE_SCREENSHOT_OCR("real_device_screenshot_ocr"),
    UNKNOWN("unknown")
}
