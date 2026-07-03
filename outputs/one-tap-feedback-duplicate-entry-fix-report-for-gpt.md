# One Tap Feedback Duplicate Entry Fix

- versionName: 4.1.15
- versionCode: 433
- taskName: one_tap_feedback_duplicate_entry_fix
- userReportedError: duplicate entry recent-sessions/session-*.json
- rootCause: latest session was already present in recentRecords, then exporter appended latest again and wrote the same zip entry twice.
- fix: recent session records are deduplicated by sessionId before writing zip entries and session index.
- regressionTest: recentSessionRecordsDeduplicateLatestSessionBeforeWritingZipEntries
- unitTests: PASS
- debugBuild: PASS
- lanUpdate: PASS
- lanUpdateVersion: 4.1.15 (433)
- overallResult: PASS
