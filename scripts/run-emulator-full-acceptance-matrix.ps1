param(
    [string]$AdbPath = "",
    [int]$CloudWaitSeconds = 100,
    [switch]$SkipBuildAndUnitTests,
    [switch]$SkipSubSmokes
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $root "outputs\gpt_review_inbox"
$matrixDir = Join-Path $reportDir "full_emulator_acceptance_matrix"
New-Item -ItemType Directory -Force -Path $reportDir, $matrixDir | Out-Null

function Find-Adb {
    if ($AdbPath -and (Test-Path $AdbPath)) { return (Resolve-Path $AdbPath).Path }
    $candidates = @(
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:ANDROID_HOME\platform-tools\adb.exe",
        "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe"
    )
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) { return (Resolve-Path $candidate).Path }
    }
    return "adb"
}

function Read-JsonFile($path) {
    if (-not (Test-Path $path)) { return $null }
    try { return Get-Content $path -Raw -Encoding UTF8 | ConvertFrom-Json } catch { return $null }
}

function To-Bool($value) {
    if ($null -eq $value) { return $false }
    if ($value -is [bool]) { return $value }
    return "$value" -eq "true" -or "$value" -eq "True" -or "$value" -eq "PASS"
}

function Escape-Cell($text) {
    return ("$text" -replace "\|", "/" -replace "`r?`n", " ").Trim()
}

function Run-LoggedCommand($name, $command, $arguments) {
    $logPath = Join-Path $matrixDir "$name.log"
    $start = Get-Date
    & $command @arguments *>&1 | Tee-Object -FilePath $logPath
    $exitCode = $LASTEXITCODE
    return [ordered]@{
        name = $name
        exitCode = $exitCode
        logPath = $logPath
        durationMs = [int]((Get-Date) - $start).TotalMilliseconds
    }
}

function Add-Scenario {
    param(
        $Rows,
        [string]$Id,
        [string]$Area,
        [string]$Scenario,
        $Condition,
        [string]$KeyEvidence,
        [string]$Blocker = ""
    )
    $result = if ($null -eq $Condition) { "NOT_RUN" } elseif ([bool]$Condition) { "PASS" } else { "FAIL" }
    if ($result -eq "NOT_RUN" -and [string]::IsNullOrWhiteSpace($Blocker)) { $Blocker = "NO_EVIDENCE" }
    if ($result -eq "FAIL" -and [string]::IsNullOrWhiteSpace($Blocker)) { $Blocker = "ASSERTION_FAILED" }
    $Rows.Add([ordered]@{
        id = $Id
        area = $Area
        scenario = $Scenario
        result = $result
        keyEvidence = $KeyEvidence
        blocker = $Blocker
    }) | Out-Null
}

function Write-MatrixReports($data, $rows) {
    $jsonPath = Join-Path $reportDir "full-emulator-acceptance-matrix.json"
    $mdPath = Join-Path $reportDir "full-emulator-acceptance-matrix-for-gpt.md"
    $rowArray = @()
    foreach ($row in $rows) { $rowArray += $row }
    if ($data.Contains("rows")) {
        $data["rows"] = $rowArray
    } else {
        $data.Add("rows", $rowArray)
    }
    $data | ConvertTo-Json -Depth 12 | Set-Content -Path $jsonPath -Encoding UTF8

    $table = New-Object System.Text.StringBuilder
    [void]$table.AppendLine("| id | area | scenario | result | keyEvidence | blocker |")
    [void]$table.AppendLine("|---|---|---|---|---|---|")
    foreach ($row in $rows) {
        [void]$table.AppendLine("| $(Escape-Cell $row.id) | $(Escape-Cell $row.area) | $(Escape-Cell $row.scenario) | $(Escape-Cell $row.result) | $(Escape-Cell $row.keyEvidence) | $(Escape-Cell $row.blocker) |")
    }

    $md = @"
# Full Emulator Acceptance Matrix

- taskName: full_emulator_matrix_before_user_phone
- versionName: 4.1.66
- versionCode: 485
- generatedAt: $($data.generatedAt)
- emulatorDetected: $($data.emulatorDetected)
- emulatorSerial: $($data.emulatorSerial)
- huiyiInstalled: $($data.huiyiInstalled)
- mockchatInstalled: $($data.mockchatInstalled)
- accessibilityEnabled: $($data.accessibilityEnabled)
- overlayPermissionGranted: $($data.overlayPermissionGranted)
- fullEmulatorMatrixResult: $($data.fullEmulatorMatrixResult)
- totalScenarioCount: $($data.totalScenarioCount)
- p0ScenarioCount: $($data.p0ScenarioCount)
- passedCount: $($data.passedCount)
- failedCount: $($data.failedCount)
- notRunCount: $($data.notRunCount)
- allP0Passed: $($data.allP0Passed)
- failedScenarioCount: $($data.failedScenarioCount)
- userNeedsPhoneThisRound: $($data.userNeedsPhoneThisRound)

## Matrix

$($table.ToString())

## Source Reports

- passiveActiveSmoke: outputs/gpt_review_inbox/passive-active-ux-cleanup-emulator-smoke.json
- expressSelfEligibilitySmoke: outputs/gpt_review_inbox/express-self-eligibility-emulator-smoke.json
- dynamicPlaybookCloudRefreshSmoke: outputs/gpt_review_inbox/dynamic-playbook-cloud-refresh-emulator-smoke.json
- unitTestLog: outputs/gpt_review_inbox/full_emulator_acceptance_matrix/unit-tests.log
- assembleLog: outputs/gpt_review_inbox/full_emulator_acceptance_matrix/assemble-debug.log

## Rule

Only when allP0Passed=true may userNeedsPhoneThisRound become true. If any P0 scenario is FAIL or NOT_RUN, the user must not continue phone trial-and-error.
"@
    $md | Set-Content -Path $mdPath -Encoding UTF8
}

$adb = Find-Adb
$devicesText = (& $adb devices 2>&1) -join "`n"
$serialMatch = [regex]::Match($devicesText, "^(emulator-\d+)\s+device", [System.Text.RegularExpressions.RegexOptions]::Multiline)
$serial = if ($serialMatch.Success) { $serialMatch.Groups[1].Value } else { "" }
$rows = New-Object System.Collections.Generic.List[object]

$baseData = [ordered]@{
    taskName = "full_emulator_matrix_before_user_phone"
    versionName = "4.1.66"
    versionCode = 485
    generatedAt = (Get-Date).ToString("o")
    emulatorDetected = [bool]$serial
    emulatorSerial = $serial
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    unitTestsResult = "NOT_RUN"
    assembleDebugResult = "NOT_RUN"
    passiveActiveSmokeResult = "NOT_RUN"
    expressSelfEligibilitySmokeResult = "NOT_RUN"
    dynamicCloudRefreshSmokeResult = "NOT_RUN"
    fullEmulatorMatrixResult = "NOT_RUN_NO_EMULATOR"
    totalScenarioCount = 22
    p0ScenarioCount = 22
    passedCount = 0
    failedCount = 0
    notRunCount = 22
    allP0Passed = $false
    failedScenarioCount = 0
    userNeedsPhoneThisRound = $false
}

if (-not $serial) {
    $ids = @(
        "A1","A2","A3","A4","A5","B1","B2","B3","B4","B5","B6",
        "C1","C2","C3","D1","D2","D3","E1","E2","F1","F2","F3"
    )
    foreach ($id in $ids) {
        Add-Scenario $rows $id "P0" "not run because no emulator was detected" $null "adb devices did not return emulator-xxxx" "NO_EMULATOR_AVAILABLE"
    }
    Write-MatrixReports $baseData $rows
    exit 2
}

if (-not $SkipBuildAndUnitTests) {
    $unit = Run-LoggedCommand "unit-tests" (Join-Path $root "gradlew.bat") @(":app:testDebugUnitTest")
    $baseData.unitTestsResult = if ($unit.exitCode -eq 0) { "PASS" } else { "FAIL" }
    $assemble = Run-LoggedCommand "assemble-debug" (Join-Path $root "gradlew.bat") @(":app:assembleDebug", ":mockchat:assembleDebug")
    $baseData.assembleDebugResult = if ($assemble.exitCode -eq 0) { "PASS" } else { "FAIL" }
}

if (-not $SkipSubSmokes) {
    Run-LoggedCommand "passive-active-ux-cleanup-smoke" "powershell" @(
        "-ExecutionPolicy", "Bypass",
        "-File", (Join-Path $PSScriptRoot "run-emulator-passive-active-ux-cleanup-smoke.ps1"),
        "-AdbPath", $adb
    ) | Out-Null
    Run-LoggedCommand "express-self-eligibility-smoke" "powershell" @(
        "-ExecutionPolicy", "Bypass",
        "-File", (Join-Path $PSScriptRoot "run-emulator-express-self-eligibility-smoke.ps1"),
        "-AdbPath", $adb,
        "-UnitTestsResult", $baseData.unitTestsResult,
        "-FixtureTestsResult", $baseData.unitTestsResult
    ) | Out-Null
    Run-LoggedCommand "dynamic-playbook-cloud-refresh-smoke" "powershell" @(
        "-ExecutionPolicy", "Bypass",
        "-File", (Join-Path $PSScriptRoot "run-emulator-dynamic-playbook-cloud-refresh-smoke.ps1"),
        "-AdbPath", $adb,
        "-CloudWaitSeconds", "$CloudWaitSeconds"
    ) | Out-Null
}

$passive = Read-JsonFile (Join-Path $reportDir "passive-active-ux-cleanup-emulator-smoke.json")
$express = Read-JsonFile (Join-Path $reportDir "express-self-eligibility-emulator-smoke.json")
$dynamic = Read-JsonFile (Join-Path $reportDir "dynamic-playbook-cloud-refresh-emulator-smoke.json")
$unitPass = $baseData.unitTestsResult -eq "PASS"

$baseData.huiyiInstalled = (To-Bool $passive.huiyiInstalled) -or (To-Bool $express.huiyiInstalled) -or (To-Bool $dynamic.huiyiInstalled)
$baseData.mockchatInstalled = (To-Bool $passive.mockchatInstalled) -or (To-Bool $express.mockchatInstalled) -or (To-Bool $dynamic.mockchatInstalled)
$baseData.accessibilityEnabled = (To-Bool $passive.accessibilityEnabled) -or (To-Bool $express.accessibilityEnabled) -or (To-Bool $dynamic.accessibilityEnabled)
$baseData.overlayPermissionGranted = (To-Bool $passive.overlayPermissionGranted) -or (To-Bool $express.overlayPermissionGranted) -or (To-Bool $dynamic.overlayPermissionGranted)
$baseData.passiveActiveSmokeResult = if ($passive) { "$($passive.overallResult)" } else { "NOT_RUN" }
$baseData.expressSelfEligibilitySmokeResult = if ($express) { "$($express.finalOverallResult)" } else { "NOT_RUN" }
$baseData.dynamicCloudRefreshSmokeResult = if ($dynamic) { "$($dynamic.finalOverallResult)" } else { "NOT_RUN" }

Add-Scenario -Rows $rows -Id "A1" -Area "next_sentence" -Scenario "LAST_ME_WAIT_NO_ROUTES_NO_CLOUD" -Condition ((To-Bool $dynamic.lastMeWaitPass) -and -not (To-Bool $dynamic.lastMeCloudAttempted) -and [int]$dynamic.lastMeRouteCount -eq 0) -KeyEvidence "dynamic smoke lastMeWaitPass=$($dynamic.lastMeWaitPass) lastMeCloudAttempted=$($dynamic.lastMeCloudAttempted) routeCount=$($dynamic.lastMeRouteCount)"

Add-Scenario -Rows $rows -Id "A2" -Area "next_sentence" -Scenario "LAST_OTHER_CLOUD_VERIFIED_CACHE_ROUTES" -Condition ((To-Bool $dynamic.nextClickReadsCloudEnhancedPlaybook) -and [int]$dynamic.cloudEnhancedRouteCount -ge 3 -and [int]$dynamic.cloudEnhancedRouteCount -le 5) -KeyEvidence "dynamic smoke nextClickReadsCloudEnhancedPlaybook=$($dynamic.nextClickReadsCloudEnhancedPlaybook) cloudEnhancedRouteCount=$($dynamic.cloudEnhancedRouteCount)"

Add-Scenario -Rows $rows -Id "A3" -Area "next_sentence" -Scenario "LAST_OTHER_NO_CLOUD_CACHE_PASSIVE_WAIT" -Condition ((To-Bool $passive.nextSentenceCloudOnly) -and (To-Bool $passive.passiveWaitPanelShown) -and [int]$passive.nextSentenceRouteCount -eq 0 -and -not (To-Bool $passive.localPassiveRoutesShownToUser)) -KeyEvidence "passive smoke terminalState=$($passive.nextSentenceTerminalState) decisionType=$($passive.nextSentenceDecisionType) routeCount=$($passive.nextSentenceRouteCount) localPassiveRoutesShownToUser=$($passive.localPassiveRoutesShownToUser)"

Add-Scenario -Rows $rows -Id "A4" -Area "next_sentence" -Scenario "SLOW_CLOUD_NON_BLOCKING_WAIT_OR_CACHE" -Condition ((To-Bool $passive.passiveWaitPanelShown) -and (To-Bool $dynamic.cloudRefreshAttempted) -and (To-Bool $dynamic.nextClickReadsCloudEnhancedPlaybook) -and (To-Bool $dynamic.staleCloudRefreshDiscarded)) -KeyEvidence "combined smoke passiveWaitPanelShown=$($passive.passiveWaitPanelShown) cloudRefreshAttempted=$($dynamic.cloudRefreshAttempted) staleCloudRefreshDiscarded=$($dynamic.staleCloudRefreshDiscarded)"

Add-Scenario -Rows $rows -Id "A5" -Area "next_sentence" -Scenario "CLOUD_FAILURE_NO_ANALYSIS_FAILED_NO_LOCAL_PASSIVE" -Condition ($unitPass -and -not (To-Bool $passive.localPassiveRoutesShownToUser)) -KeyEvidence "unit CloudNetworkFailureShowsVisibleFallbackReasonTest localPassiveRoutesShownToUser=$($passive.localPassiveRoutesShownToUser)"

Add-Scenario -Rows $rows -Id "B1" -Area "express_self" -Scenario "LAST_ME_RECENT_HOLD_BACK" -Condition ((To-Bool $express.expressSelfRecentLastMeHoldBack) -and "$($express.lastMe.terminalState)" -eq "HOLD_BACK_PANEL" -and [int]$express.lastMe.routeCount -eq 0 -and -not (To-Bool $express.lastMe.cloudAttempted)) -KeyEvidence "express smoke terminalState=$($express.lastMe.terminalState) decisionType=$($express.lastMe.decisionType) routeCount=$($express.lastMe.routeCount) cloudAttempted=$($express.lastMe.cloudAttempted)"

Add-Scenario -Rows $rows -Id "B2" -Area "express_self" -Scenario "LAST_OTHER_PLANNING_ARC_REVEAL_SIMPLE_PANEL" -Condition ((To-Bool $express.expressSelfPlanningArcReveal) -and [int]$express.lastOther.routeCount -ge 2 -and [int]$express.lastOther.routeCount -le 5 -and "$($express.lastOther.routeTypesCsv)" -match "ARC_REVEAL") -KeyEvidence "express smoke routeTypesCsv=$($express.lastOther.routeTypesCsv) routeCount=$($express.lastOther.routeCount) eligibilityMode=$($express.lastOther.expressSelfEligibilityMode)"

Add-Scenario -Rows $rows -Id "B3" -Area "express_self" -Scenario "LONG_INACTIVE_COLD_START_LOW_PRESSURE" -Condition ((To-Bool $express.expressSelfColdStartAllowed) -and $unitPass) -KeyEvidence "unit ExpressSelfAllowsColdStartAfterLongInactiveTest coldStartAssertionSource=$($express.coldStartAssertionSource)"

Add-Scenario -Rows $rows -Id "B4" -Area "express_self" -Scenario "RECENT_OVER_EXPRESSION_HOLD_BACK" -Condition ($unitPass) -KeyEvidence "unit ExpressSelfHoldBackAfterRecentSelfExpressionTest and ExpressionLedger hold back coverage"

Add-Scenario -Rows $rows -Id "B5" -Area "express_self" -Scenario "SAME_SCENE_REPEAT_CLICK_CACHE_STABLE" -Condition ((To-Bool $passive.expressSelfRepeatClickStable) -and [int]$passive.expressSelfRepeatRouteCount -ge 1 -and [int]$passive.expressSelfRepeatRouteCount -le 3) -KeyEvidence "passive-active smoke expressSelfRepeatClickStable=$($passive.expressSelfRepeatClickStable) repeatRouteCount=$($passive.expressSelfRepeatRouteCount)"

Add-Scenario -Rows $rows -Id "B6" -Area "express_self" -Scenario "SWITCH_FACET_AND_ELEVATE_MEANING_STABLE" -Condition ($unitPass) -KeyEvidence "unit ExpressionLedgerTest covers SWITCH_FACET and ELEVATE_MEANING"

Add-Scenario -Rows $rows -Id "C1" -Area "untrusted_state" -Scenario "LAUNCHER_DESKTOP_BLOCKS_ROUTES_AND_CLOUD" -Condition ((To-Bool $express.expressSelfUnsupportedBlocked) -and "$($express.desktop.terminalState)" -eq "CONTROLLED_FAIL_PANEL" -and [int]$express.desktop.routeCount -eq 0 -and -not (To-Bool $express.desktop.cloudAttempted)) -KeyEvidence "express emulator desktop terminalState=$($express.desktop.terminalState) routeCount=$($express.desktop.routeCount) cloudAttempted=$($express.desktop.cloudAttempted)"

Add-Scenario -Rows $rows -Id "C2" -Area "untrusted_state" -Scenario "HUIYI_PANEL_CONTAMINATION_BLOCKS_ANALYSIS" -Condition ($unitPass) -KeyEvidence "unit PreAnalysisTitleContaminationDetectedForOneTapPanelTest and PreAnalysisTitleWithWaitPhraseIsContaminatedTest"

Add-Scenario -Rows $rows -Id "C3" -Area "untrusted_state" -Scenario "LAST_STABLE_SNAPSHOT_WINDOW_MISMATCH_BLOCKS" -Condition ($unitPass) -KeyEvidence "unit ExpressSelfBlocksUntrustedLastStableSnapshotTest"

Add-Scenario -Rows $rows -Id "D1" -Area "xiaoenai_generic_trial" -Scenario "XIAOENAI_STABLE_CHAT_HIGH_CONFIDENCE_GENERIC_TRIAL" -Condition ($unitPass) -KeyEvidence "unit V4165_XIAOENAI_NORMAL_CHAT_GENERIC_TRIAL and XiaoenaiGenericTrialAllowsStableChatTest"

Add-Scenario -Rows $rows -Id "D2" -Area "xiaoenai_generic_trial" -Scenario "XIAOENAI_LOW_CONFIDENCE_PRECISE_BLOCK" -Condition ($unitPass) -KeyEvidence "unit GenericTrialBlocksLowConfidenceTest expects LOW_GENERIC_CONFIDENCE not desktop"

Add-Scenario -Rows $rows -Id "D3" -Area "xiaoenai_generic_trial" -Scenario "XIAOENAI_STATUS_NODES_NOT_LAST_EFFECTIVE_MESSAGE" -Condition ($unitPass) -KeyEvidence "unit status metadata tests cover read delivery checkmark not effective"

Add-Scenario -Rows $rows -Id "E1" -Area "message_status_metadata" -Scenario "READ_DELIVERED_CHECKMARK_FILTERED_AND_ATTACHED" -Condition ($unitPass) -KeyEvidence "unit ReadReceiptNodeIsMetadataNotMessageTest DeliveryStatusAttachedToPreviousMeMessageTest StatusArtifactDoesNotBecomeEffectiveMessageTest"

Add-Scenario -Rows $rows -Id "E2" -Area "message_status_metadata" -Scenario "REAL_MESSAGE_BEFORE_STATUS_REMAINS_LAST_EFFECTIVE" -Condition ($unitPass) -KeyEvidence "unit ReadReceiptDoesNotAffectLastSpeakerTest and LastMeWithReadReceiptStillWaitsTest"

Add-Scenario -Rows $rows -Id "F1" -Area "cloud_refresh_stale_discard" -Scenario "DYNAMIC_PLAYBOOK_CLOUD_REFRESH_UPDATES_CACHE" -Condition ((To-Bool $dynamic.cloudRefreshAttempted) -and (To-Bool $dynamic.cloudRefreshSuccess) -and "$($dynamic.cloudContractValidationResult)" -eq "PASS" -and (To-Bool $dynamic.playbookCacheUpdatedFromCloud) -and (To-Bool $dynamic.nextClickReadsCloudEnhancedPlaybook)) -KeyEvidence "dynamic cloud smoke attempted=$($dynamic.cloudRefreshAttempted) success=$($dynamic.cloudRefreshSuccess) contract=$($dynamic.cloudContractValidationResult) cacheUpdated=$($dynamic.playbookCacheUpdatedFromCloud)"

Add-Scenario -Rows $rows -Id "F2" -Area "cloud_refresh_stale_discard" -Scenario "CHAT_SWITCH_DISCARDS_OLD_CLOUD_REFRESH" -Condition ((To-Bool $dynamic.staleCloudRefreshDiscarded) -and "$($dynamic.staleDiscardReason)" -eq "CHAT_KEY_CHANGED") -KeyEvidence "dynamic cloud smoke staleCloudRefreshDiscarded=$($dynamic.staleCloudRefreshDiscarded) staleDiscardReason=$($dynamic.staleDiscardReason)"

Add-Scenario -Rows $rows -Id "F3" -Area "cloud_refresh_stale_discard" -Scenario "DOUBLE_CLICK_ONLY_ONE_ACTIVE_SESSION" -Condition ($unitPass) -KeyEvidence "unit DoubleClickNextOnlyOneActiveSessionTest and OneClickOneTerminalPanelTest"

$passed = @($rows | Where-Object { $_.result -eq "PASS" }).Count
$failed = @($rows | Where-Object { $_.result -eq "FAIL" }).Count
$notRun = @($rows | Where-Object { $_.result -eq "NOT_RUN" }).Count
$allP0Passed = $rows.Count -gt 0 -and $failed -eq 0 -and $notRun -eq 0
$baseData.totalScenarioCount = $rows.Count
$baseData.p0ScenarioCount = $rows.Count
$baseData.passedCount = $passed
$baseData.failedCount = $failed
$baseData.notRunCount = $notRun
$baseData.failedScenarioCount = $failed
$baseData.allP0Passed = $allP0Passed
$baseData.userNeedsPhoneThisRound = $allP0Passed
$baseData.fullEmulatorMatrixResult = if ($allP0Passed) { "PASS" } elseif ($notRun -gt 0) { "FAIL_OR_NOT_RUN" } else { "FAIL" }

Write-MatrixReports $baseData $rows
Write-Host "fullEmulatorMatrix=$($baseData.fullEmulatorMatrixResult)"
Write-Host "allP0Passed=$($baseData.allP0Passed)"
Write-Host "userNeedsPhoneThisRound=$($baseData.userNeedsPhoneThisRound)"
if (-not $allP0Passed) { exit 2 }
