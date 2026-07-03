# MockChatLab Layout Matrix Report

## 1. 基本信息

- generatedAt: 1783008000000
- mockchatVersion: v4.1.3-matrix
- huiyiVersion: v4.1.3-debug
- totalProfiles: 5
- totalScenarios: 50
- passed: 50
- failed: 0

## 2. Profile 汇总

- profileName: WECHAT_LIKE
  scenarioCount: 10
  passCount: 10
  failCount: 0
- profileName: QQ_LIKE
  scenarioCount: 10
  passCount: 10
  failCount: 0
- profileName: REDBOOK_DM_LIKE
  scenarioCount: 10
  passCount: 10
  failCount: 0
- profileName: DATING_APP_LIKE
  scenarioCount: 10
  passCount: 10
  failCount: 0
- profileName: MINIMAL_CHAT_LIKE
  scenarioCount: 10
  passCount: 10
  failCount: 0

## 3. 场景明细

- profileName: WECHAT_LIKE
  scenarioName: last_me
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 19
  metadataFilteredCount: 13
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 13
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: metadata_trap
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 18
  metadataFilteredCount: 15
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 15
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: voice_last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 10
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 10
  voiceCount: 2
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: VOICE_SUMMARY_REQUIRED
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: image_or_sticker
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: low_expression
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 15
  metadataFilteredCount: 10
  effectiveMessageCount: 5
  meCount: 0
  otherCount: 5
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: BOUNDARY_RESPECT
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: long_multiline
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: quoted_reply
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: unknown_bounds
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 11
  effectiveMessageCount: 2
  meCount: 0
  otherCount: 2
  unknownCount: 1
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: WECHAT_LIKE
  scenarioName: time_at_bottom
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 20
  metadataFilteredCount: 14
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 14
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: last_me
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 19
  metadataFilteredCount: 13
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 13
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: metadata_trap
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 18
  metadataFilteredCount: 15
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 15
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: voice_last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 10
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 10
  voiceCount: 2
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: VOICE_SUMMARY_REQUIRED
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: image_or_sticker
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: low_expression
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 15
  metadataFilteredCount: 10
  effectiveMessageCount: 5
  meCount: 0
  otherCount: 5
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: BOUNDARY_RESPECT
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: long_multiline
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: quoted_reply
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: unknown_bounds
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 11
  effectiveMessageCount: 2
  meCount: 0
  otherCount: 2
  unknownCount: 1
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: QQ_LIKE
  scenarioName: time_at_bottom
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 20
  metadataFilteredCount: 14
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 14
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: last_me
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 19
  metadataFilteredCount: 13
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 13
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: metadata_trap
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 18
  metadataFilteredCount: 15
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 15
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: voice_last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 10
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 10
  voiceCount: 2
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: VOICE_SUMMARY_REQUIRED
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: image_or_sticker
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: low_expression
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 15
  metadataFilteredCount: 10
  effectiveMessageCount: 5
  meCount: 0
  otherCount: 5
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: BOUNDARY_RESPECT
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: long_multiline
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: quoted_reply
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: unknown_bounds
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 11
  effectiveMessageCount: 2
  meCount: 0
  otherCount: 2
  unknownCount: 1
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: REDBOOK_DM_LIKE
  scenarioName: time_at_bottom
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 20
  metadataFilteredCount: 14
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 14
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: last_me
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 19
  metadataFilteredCount: 13
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 13
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: metadata_trap
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 18
  metadataFilteredCount: 15
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 15
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: voice_last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 10
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 10
  voiceCount: 2
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: VOICE_SUMMARY_REQUIRED
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: image_or_sticker
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: low_expression
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 15
  metadataFilteredCount: 10
  effectiveMessageCount: 5
  meCount: 0
  otherCount: 5
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: BOUNDARY_RESPECT
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: long_multiline
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: quoted_reply
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: unknown_bounds
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 11
  effectiveMessageCount: 2
  meCount: 0
  otherCount: 2
  unknownCount: 1
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: DATING_APP_LIKE
  scenarioName: time_at_bottom
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 20
  metadataFilteredCount: 14
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 14
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: last_me
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: ME
  decisionType: WAIT
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 19
  metadataFilteredCount: 13
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 13
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: metadata_trap
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 18
  metadataFilteredCount: 15
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 15
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: voice_last_other
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 10
  effectiveMessageCount: 3
  meCount: 1
  otherCount: 2
  unknownCount: 0
  systemCount: 10
  voiceCount: 2
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: VOICE_SUMMARY_REQUIRED
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: image_or_sticker
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: low_expression
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 15
  metadataFilteredCount: 10
  effectiveMessageCount: 5
  meCount: 0
  otherCount: 5
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: BOUNDARY_RESPECT
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: long_multiline
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: quoted_reply
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 12
  metadataFilteredCount: 10
  effectiveMessageCount: 2
  meCount: 1
  otherCount: 1
  unknownCount: 0
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: unknown_bounds
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 13
  metadataFilteredCount: 11
  effectiveMessageCount: 2
  meCount: 0
  otherCount: 2
  unknownCount: 1
  systemCount: 10
  voiceCount: 0
  imageCount: 0
  lastEffectiveSpeaker: OTHER
  decisionType: CONTEXT_REQUIRED
  routeCount: 0
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

- profileName: MINIMAL_CHAT_LIKE
  scenarioName: time_at_bottom
  sample_source: emulator_mock_chat_accessibility
  appPackage: com.huiyi.mockchat
  parsedMessageCount: 20
  metadataFilteredCount: 14
  effectiveMessageCount: 6
  meCount: 1
  otherCount: 5
  unknownCount: 0
  systemCount: 14
  voiceCount: 0
  imageCount: 1
  lastEffectiveSpeaker: OTHER
  decisionType: NORMAL_REPLY
  routeCount: 5
  overlayShownInTargetApp: true
  mainActivityOpened: false
  result: PASS
  failReason: none

## 4. 解析样例

### WECHAT_LIKE / metadata_trap
[m001][center][system 100% ui_control_metadata] 返回
[m002][center][system 100% header_metadata] 白云蓝天
[m003][center][system 100% online_status_metadata] 上次在线时间07-02 18:06
[m004][center][system 100% ui_control_metadata] C metadata_trap
[m005][center][system 100% ui_control_metadata] 导出截图
[m006][center][system 100% time_metadata] 2026-07-03
[m007][center][system 100% system_notice_metadata] 系统提示：你们已成为好友
[m008][center][system 100% time_metadata] 10:56
[m009][left][other 82% bubble_edge_left] 白云蓝天：今天事情有点多。
[m010][right][me 82% bubble_edge_right] 我在，你挑你想说的讲。
[m011][center][system 100% time_metadata] 11:00
[m012][center][system 100% system_notice_metadata] 系统推荐：保持礼貌沟通
[m013][left][other 82% bubble_edge_left] 我就是怕一说又变成抱怨。
[m014][center][system 100% time_metadata] 11:02
[m015][center][system 100% ui_control_metadata] 语音
[m016][center][system 100% ui_control_metadata] 输入框
[m017][center][system 100% ui_control_metadata] 表情
[m018][center][system 100% ui_control_metadata] 发送

### QQ_LIKE / metadata_trap
[m001][center][system 100% ui_control_metadata] 返回
[m002][center][system 100% header_metadata] 蓝桥
[m003][center][system 100% online_status_metadata] 手机在线
[m004][center][system 100% ui_control_metadata] C metadata_trap
[m005][center][system 100% ui_control_metadata] 导出截图
[m006][center][system 100% time_metadata] 2026-07-03
[m007][center][system 100% system_notice_metadata] 系统提示：你们已成为好友
[m008][center][system 100% time_metadata] 10:56
[m009][left][other 82% bubble_edge_left] 蓝桥：今天事情有点多。
[m010][right][me 82% bubble_edge_right] 我在，你挑你想说的讲。
[m011][center][system 100% time_metadata] 11:00
[m012][center][system 100% system_notice_metadata] 系统推荐：保持礼貌沟通
[m013][left][other 82% bubble_edge_left] 我就是怕一说又变成抱怨。
[m014][center][system 100% time_metadata] 11:02
[m015][center][system 100% ui_control_metadata] 语音
[m016][center][system 100% ui_control_metadata] 输入框
[m017][center][system 100% ui_control_metadata] 表情
[m018][center][system 100% ui_control_metadata] 发送

### REDBOOK_DM_LIKE / metadata_trap
[m001][center][system 100% ui_control_metadata] 返回
[m002][center][system 100% header_metadata] 小鹿同学
[m003][center][system 100% online_status_metadata] 刚刚在线
[m004][center][system 100% ui_control_metadata] C metadata_trap
[m005][center][system 100% ui_control_metadata] 导出截图
[m006][center][system 100% time_metadata] 2026-07-03
[m007][center][system 100% system_notice_metadata] 系统提示：你们已成为好友
[m008][center][system 100% time_metadata] 10:56
[m009][left][other 82% bubble_edge_left] 小鹿同学：今天事情有点多。
[m010][right][me 82% bubble_edge_right] 我在，你挑你想说的讲。
[m011][center][system 100% time_metadata] 11:00
[m012][center][system 100% system_notice_metadata] 系统推荐：保持礼貌沟通
[m013][left][other 82% bubble_edge_left] 我就是怕一说又变成抱怨。
[m014][center][system 100% time_metadata] 11:02
[m015][center][system 100% ui_control_metadata] 语音
[m016][center][system 100% ui_control_metadata] 输入框
[m017][center][system 100% ui_control_metadata] 表情
[m018][center][system 100% ui_control_metadata] 发送

### DATING_APP_LIKE / metadata_trap
[m001][center][system 100% ui_control_metadata] 返回
[m002][center][system 100% header_metadata] 林夏
[m003][center][system 100% online_status_metadata] 资料完整度 82%
[m004][center][system 100% ui_control_metadata] C metadata_trap
[m005][center][system 100% ui_control_metadata] 导出截图
[m006][center][system 100% time_metadata] 2026-07-03
[m007][center][system 100% system_notice_metadata] 系统提示：你们已成为好友
[m008][center][system 100% time_metadata] 10:56
[m009][left][other 82% bubble_edge_left] 林夏：今天事情有点多。
[m010][right][me 82% bubble_edge_right] 我在，你挑你想说的讲。
[m011][center][system 100% time_metadata] 11:00
[m012][center][system 100% system_notice_metadata] 系统推荐：保持礼貌沟通
[m013][left][other 82% bubble_edge_left] 我就是怕一说又变成抱怨。
[m014][center][system 100% time_metadata] 11:02
[m015][center][system 100% ui_control_metadata] 语音
[m016][center][system 100% ui_control_metadata] 输入框
[m017][center][system 100% ui_control_metadata] 表情
[m018][center][system 100% ui_control_metadata] 发送

### MINIMAL_CHAT_LIKE / metadata_trap
[m001][center][system 100% ui_control_metadata] 返回
[m002][center][system 100% header_metadata] 对话
[m003][center][system 100% online_status_metadata] 在线
[m004][center][system 100% ui_control_metadata] C metadata_trap
[m005][center][system 100% ui_control_metadata] 导出截图
[m006][center][system 100% time_metadata] 2026-07-03
[m007][center][system 100% system_notice_metadata] 系统提示：你们已成为好友
[m008][center][system 100% time_metadata] 10:56
[m009][left][other 82% bubble_edge_left] 对话：今天事情有点多。
[m010][right][me 82% bubble_edge_right] 我在，你挑你想说的讲。
[m011][center][system 100% time_metadata] 11:00
[m012][center][system 100% system_notice_metadata] 系统推荐：保持礼貌沟通
[m013][left][other 82% bubble_edge_left] 我就是怕一说又变成抱怨。
[m014][center][system 100% time_metadata] 11:02
[m015][center][system 100% ui_control_metadata] 语音
[m016][center][system 100% ui_control_metadata] 输入框
[m017][center][system 100% ui_control_metadata] 表情
[m018][center][system 100% ui_control_metadata] 发送

## 5. 失败项

- none

## 6. 截图样本

- outputs/mockchat_screenshots/wechat_like_metadata_trap.png
- outputs/mockchat_screenshots/qq_like_voice_last_other.png
- outputs/mockchat_screenshots/redbook_like_last_other.png
- outputs/mockchat_screenshots/dating_like_profile_card.png
- outputs/mockchat_screenshots/minimal_like_unknown_bounds.png
