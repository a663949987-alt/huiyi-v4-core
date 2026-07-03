# Overlay Window Flags Audit

## Basic
- versionName: 4.1.8b
- versionCode: 422
- taskName: next_sentence_analysis_failure_diagnosis

## FloatingBubble
- type: `TYPE_APPLICATION_OVERLAY` on Android O+, otherwise `TYPE_PHONE`
- flags: `FLAG_NOT_FOCUSABLE`
- focusable: false
- touchable: true
- mayStealActiveWindow: low, but tap can temporarily move active root/focused window
- recommendation: keep `FLAG_NOT_FOCUSABLE`; use lastStableChatSnapshot fallback after click, already implemented this round.

## FloatingResultPanel
- type: `TYPE_APPLICATION_OVERLAY` on Android O+, otherwise `TYPE_PHONE`
- flags: `FLAG_NOT_FOCUSABLE`
- focusable: false
- touchable: true
- mayStealActiveWindow: low, but panel display can still affect active root timing
- recommendation: keep non-focusable panel; failure panel now records specific error and does not open MainActivity.

## Result
- overlayBubbleSurvivesAfterNextSentence: unknown_without_physical_device
- MainActivityOpenedByNextSentence: no code path found in `FloatingBubbleService` / `FloatingResultPanelController` except explicit accessibility settings button on error panel.
- risk: Android may still report own overlay as active root immediately after tap; mitigated by root retry and stable snapshot fallback.
