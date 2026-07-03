param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
)

$ErrorActionPreference = "Stop"

function Read-PropertiesFile([string]$Path) {
    $props = @{}
    if (!(Test-Path -LiteralPath $Path)) { return $props }
    Get-Content -LiteralPath $Path -Encoding UTF8 | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) { return }
        $idx = $line.IndexOf("=")
        if ($idx -le 0) { return }
        $key = $line.Substring(0, $idx).Trim()
        $value = $line.Substring($idx + 1).Trim()
        $props[$key] = $value
    }
    return $props
}

function Get-ConfigValue([hashtable[]]$Sources, [string[]]$Keys, [string]$Default = "") {
    foreach ($key in $Keys) {
        $envValue = [Environment]::GetEnvironmentVariable($key)
        if (![string]::IsNullOrWhiteSpace($envValue)) { return $envValue.Trim() }
    }
    foreach ($source in $Sources) {
        foreach ($key in $Keys) {
            if ($source.ContainsKey($key) -and ![string]::IsNullOrWhiteSpace([string]$source[$key])) {
                return ([string]$source[$key]).Trim()
            }
        }
    }
    return $Default
}

function Join-ChatCompletionsUrl([string]$BaseUrl) {
    $trimmed = $BaseUrl.Trim().TrimEnd("/")
    if ($trimmed.EndsWith("/chat/completions")) { return $trimmed }
    return "$trimmed/chat/completions"
}

function Redact-Text([string]$Text, [string]$ApiKey) {
    if ([string]::IsNullOrEmpty($Text)) { return "" }
    $redacted = $Text
    if (![string]::IsNullOrWhiteSpace($ApiKey)) {
        $redacted = $redacted.Replace($ApiKey, "[REDACTED_API_KEY]")
    }
    $redacted = [regex]::Replace($redacted, "Bearer\s+[A-Za-z0-9._\-]+", "Bearer [REDACTED]")
    $redacted = [regex]::Replace($redacted, "sk-[A-Za-z0-9._\-]+", "sk-[REDACTED]")
    return $redacted
}

function Strip-JsonFence([string]$Content) {
    $trimmed = $Content.Trim()
    if (!$trimmed.StartsWith('```')) { return $trimmed }
    $trimmed = [regex]::Replace($trimmed, '^```(?:json|JSON)?\s*', "")
    $trimmed = [regex]::Replace($trimmed, '\s*```$', "")
    return $trimmed.Trim()
}

function Get-LikelyCause([Nullable[int]]$StatusCode, [string]$ExceptionClass, [string]$Message) {
    if ($StatusCode -eq 401) { return "HTTP_401" }
    if ($StatusCode -eq 403) { return "HTTP_403" }
    if ($StatusCode -eq 404) { return "HTTP_404" }
    if ($StatusCode -eq 429) { return "HTTP_429" }
    if ($StatusCode -ge 500) { return "HTTP_5XX" }
    if ($Message -match "(?i)name resolution|no such host|dns|remote name could not be resolved") { return "DNS_FAILED" }
    if ($Message -match "(?i)ssl|tls|certificate|trust") { return "TLS_FAILED" }
    if ($Message -match "(?i)timeout|timed out|超时") { return "TIMEOUT" }
    if ($Message -match "(?i)invalid uri|invalid url|unsupported protocol|scheme") { return "BASE_URL_INVALID" }
    return "UNKNOWN"
}

function Validate-HuiyiContract($Contract) {
    if ($null -eq $Contract) { throw "contract_missing" }
    if ([int]$Contract.schemaVersion -ne 1) { throw "schemaVersion_invalid" }
    if (@("NORMAL_REPLY", "EMPATHY_FIRST", "CONTEXT_REQUIRED") -notcontains [string]$Contract.decisionType) { throw "decisionType_invalid" }
    if (@("REPLY_ROUTES", "CONTEXT_REQUIRED") -notcontains [string]$Contract.decisionTypeFamily) { throw "decisionTypeFamily_invalid" }
    if (($Contract.decisionType -eq "CONTEXT_REQUIRED") -and ($Contract.decisionTypeFamily -ne "CONTEXT_REQUIRED")) { throw "decision_family_mismatch" }
    if (($Contract.decisionType -ne "CONTEXT_REQUIRED") -and ($Contract.decisionTypeFamily -ne "REPLY_ROUTES")) { throw "decision_family_mismatch" }
    if ($null -eq $Contract.coCreationPoint) { throw "coCreationPoint_missing" }
    if ([string]::IsNullOrWhiteSpace([string]$Contract.coCreationPoint.type)) { throw "coCreationPoint_type_missing" }
    if ([string]::IsNullOrWhiteSpace([string]$Contract.coCreationPoint.evidence)) { throw "coCreationPoint_evidence_missing" }
    if ([string]::IsNullOrWhiteSpace([string]$Contract.coCreationPoint.meaning)) { throw "coCreationPoint_meaning_missing" }
    if ([string]::IsNullOrWhiteSpace([string]$Contract.userLikelyMistake)) { throw "userLikelyMistake_missing" }
    if ([string]::IsNullOrWhiteSpace([string]$Contract.bestMove)) { throw "bestMove_missing" }
    if ($null -eq $Contract.intensityPolicy -or @("LOW", "MEDIUM", "HIGH") -notcontains [string]$Contract.intensityPolicy.level) { throw "intensityPolicy_invalid" }
    if ([string]::IsNullOrWhiteSpace([string]$Contract.fallbackMove)) { throw "fallbackMove_missing" }
    $routes = @($Contract.routes)
    if ($Contract.decisionType -ne "CONTEXT_REQUIRED" -and $routes.Count -ne 5) { throw "routes_count_invalid" }
    $seen = @{}
    foreach ($route in $routes) {
        if ([string]::IsNullOrWhiteSpace([string]$route.message)) { throw "route_message_missing" }
        if ([string]::IsNullOrWhiteSpace([string]$route.why)) { throw "route_why_missing" }
        if (@("LOW", "MEDIUM", "HIGH") -notcontains ([string]$route.riskLevel)) { throw "route_risk_invalid" }
        if ($seen.ContainsKey([string]$route.message)) { throw "route_duplicate_message" }
        $seen[[string]$route.message] = $true
    }
    return $routes.Count
}

$sources = @(
    (Read-PropertiesFile (Join-Path $ProjectRoot ".env.local")),
    (Read-PropertiesFile (Join-Path $ProjectRoot "huiyi-cloud.properties")),
    (Read-PropertiesFile (Join-Path $ProjectRoot "local.properties"))
)

$baseUrl = Get-ConfigValue $sources @("HUIYI_RELAY_BASE_URL", "huiyi.relay.baseUrl")
$model = Get-ConfigValue $sources @("HUIYI_RELAY_MODEL", "huiyi.relay.model") "gpt-5.5"
$apiKey = Get-ConfigValue $sources @("HUIYI_RELAY_API_KEY", "huiyi.relay.apiKey")
$timeoutMsText = Get-ConfigValue $sources @("HUIYI_RELAY_TIMEOUT_MS", "huiyi.relay.timeoutMs") "30000"
$timeoutSec = [Math]::Max(90, [Math]::Ceiling(([int]$timeoutMsText) / 1000))
$requestUrl = if (![string]::IsNullOrWhiteSpace($baseUrl)) { Join-ChatCompletionsUrl $baseUrl } else { "" }
$requestUrlRedacted = if ($requestUrl) { $requestUrl -replace '([?&]api_key=)[^&]+', '$1[REDACTED]' } else { "" }
$manifestPath = Join-Path $ProjectRoot "app/src/main/AndroidManifest.xml"
$manifestText = if (Test-Path -LiteralPath $manifestPath) { Get-Content -LiteralPath $manifestPath -Raw } else { "" }
$baseUrlJoinValid = (![string]::IsNullOrWhiteSpace($requestUrl)) -and $requestUrl.EndsWith("/chat/completions") -and ($requestUrl -notmatch "/v1/v1/")

$report = [ordered]@{
    taskName = "relay_cloud_smoke_before_user_phone"
    versionName = "4.1.28"
    versionCode = 447
    generatedAt = (Get-Date).ToString("o")
    relaySmokeAttempted = $false
    relaySmokeResult = "NOT_RUN_ENV_LIMITED"
    baseUrlConfigured = ![string]::IsNullOrWhiteSpace($baseUrl)
    modelConfigured = ![string]::IsNullOrWhiteSpace($model)
    apiKeyConfigured = ![string]::IsNullOrWhiteSpace($apiKey)
    apiKeyLeaked = $false
    requestUrlRedacted = $requestUrlRedacted
    httpStatus = $null
    responseReceived = $false
    choicesMessageContentPresent = $false
    responseParsed = $false
    contractValidationResult = "NOT_RUN"
    routeCount = 0
    latencyMs = $null
    errorClass = $null
    errorMessageRedacted = $null
    likelyCause = "UNKNOWN"
    cloudRequestActuallySent = $false
    androidInternetPermissionDeclared = $manifestText.Contains("android.permission.INTERNET")
    finalRequestUrlRedacted = $requestUrlRedacted
    baseUrlJoinValid = $baseUrlJoinValid
    authorizationHeaderPresent = ![string]::IsNullOrWhiteSpace($apiKey)
    cleartextBlockedSuspected = $baseUrl.StartsWith("http://")
    tlsFailureSuspected = $false
    dnsFailureSuspected = $false
    networkExceptionClass = $null
    networkExceptionMessageRedacted = $null
}

if (!$report.baseUrlConfigured -or !$report.modelConfigured -or !$report.apiKeyConfigured) {
    $report.likelyCause = "BASE_URL_INVALID"
} else {
    $sampleText = "好，我过会也去吃饭了"
    $systemPrompt = "Return compact JSON only. No markdown."
    $userPrompt = "HuiyiTacticalContract-v1 for chat. Last speaker OTHER, last message '$sampleText'. Return this exact shape with five unique low-pressure routes: {`"schemaVersion`":1,`"decisionType`":`"NORMAL_REPLY`",`"decisionTypeFamily`":`"REPLY_ROUTES`",`"situation`":`"short daily-life reply`",`"coCreationPoint`":{`"exists`":true,`"type`":`"daily_rhythm`",`"evidence`":`"other said they will eat later`",`"meaning`":`"share a low-pressure daily rhythm`"},`"userLikelyMistake`":`"pushing too much`",`"bestMove`":`"care lightly`",`"intensityPolicy`":{`"level`":`"LOW`",`"reason`":`"simple daily message`"},`"riskWarning`":`"`",`"fallbackMove`":`"wait calmly`",`"routes`":[{`"slot`":`"stable`",`"message`":`"...`",`"why`":`"...`",`"riskLevel`":`"LOW`",`"fallbackMove`":`"...`"}]}"
    $body = [ordered]@{
        model = $model
        temperature = 0.2
        max_tokens = 1200
        messages = @(
            [ordered]@{ role = "system"; content = $systemPrompt },
            [ordered]@{ role = "user"; content = $userPrompt }
        )
    } | ConvertTo-Json -Depth 20

    $headers = @{
        Authorization = "Bearer $apiKey"
    }
    $started = Get-Date
    $report.relaySmokeAttempted = $true
    try {
        $report.cloudRequestActuallySent = $true
        $response = Invoke-WebRequest -UseBasicParsing -Uri $requestUrl -Method Post -Headers $headers -ContentType "application/json; charset=utf-8" -Body $body -TimeoutSec $timeoutSec
        $report.httpStatus = [int]$response.StatusCode
        $report.responseReceived = $true
        $json = $response.Content | ConvertFrom-Json
        $content = [string]$json.choices[0].message.content
        $report.choicesMessageContentPresent = ![string]::IsNullOrWhiteSpace($content)
        $contract = (Strip-JsonFence $content) | ConvertFrom-Json
        $report.responseParsed = $true
        $report.routeCount = Validate-HuiyiContract $contract
        $report.contractValidationResult = "PASS"
        $report.relaySmokeResult = "PASS"
        $report.likelyCause = "NONE"
    } catch {
        $report.relaySmokeResult = "FAIL"
        $report.errorClass = $_.Exception.GetType().FullName
        $message = $_.Exception.Message
        $report.errorMessageRedacted = Redact-Text $message $apiKey
        $report.networkExceptionClass = $report.errorClass
        $report.networkExceptionMessageRedacted = $report.errorMessageRedacted
        $status = $null
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $status = [int]$_.Exception.Response.StatusCode
            $report.httpStatus = $status
            $report.responseReceived = $true
        }
        if ($report.responseParsed -and $report.contractValidationResult -ne "PASS") {
            $report.contractValidationResult = "FAIL"
        } elseif ($report.choicesMessageContentPresent -and !$report.responseParsed) {
            $report.likelyCause = "PARSE_FAILED"
            $report.contractValidationResult = "NOT_RUN"
        }
        if ($_.Exception.Status -eq [System.Net.WebExceptionStatus]::Timeout) {
            $report.likelyCause = "TIMEOUT"
        } elseif ($_.Exception.Message -match "contract|schema|route_|missing|invalid|duplicate") {
            $report.likelyCause = "CONTRACT_INVALID"
            $report.contractValidationResult = "FAIL"
        } elseif ($report.likelyCause -eq "UNKNOWN") {
            $report.likelyCause = Get-LikelyCause $status $report.errorClass $message
        }
        $report.tlsFailureSuspected = $report.likelyCause -eq "TLS_FAILED"
        $report.dnsFailureSuspected = $report.likelyCause -eq "DNS_FAILED"
    } finally {
        $report.latencyMs = [int]((Get-Date) - $started).TotalMilliseconds
    }
}

$outDir = Join-Path $ProjectRoot "outputs/gpt_review_inbox"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$jsonPath = Join-Path $outDir "relay-cloud-smoke-report.json"
$mdPath = Join-Path $outDir "relay-cloud-smoke-report-for-gpt.md"
$jsonText = $report | ConvertTo-Json -Depth 20
$apiKeyLeaked = (![string]::IsNullOrWhiteSpace($apiKey) -and $jsonText.Contains($apiKey))
$report.apiKeyLeaked = $apiKeyLeaked
$jsonText = $report | ConvertTo-Json -Depth 20
Set-Content -LiteralPath $jsonPath -Value $jsonText -Encoding UTF8

$markdown = @"
# Relay Cloud Smoke Report

- taskName: $($report.taskName)
- versionName: $($report.versionName)
- versionCode: $($report.versionCode)
- generatedAt: $($report.generatedAt)
- relaySmokeAttempted: $($report.relaySmokeAttempted)
- relaySmokeResult: $($report.relaySmokeResult)
- baseUrlConfigured: $($report.baseUrlConfigured)
- modelConfigured: $($report.modelConfigured)
- apiKeyConfigured: $($report.apiKeyConfigured)
- apiKeyLeaked: $($report.apiKeyLeaked)
- requestUrlRedacted: $($report.requestUrlRedacted)
- httpStatus: $($report.httpStatus)
- responseReceived: $($report.responseReceived)
- choicesMessageContentPresent: $($report.choicesMessageContentPresent)
- responseParsed: $($report.responseParsed)
- contractValidationResult: $($report.contractValidationResult)
- routeCount: $($report.routeCount)
- latencyMs: $($report.latencyMs)
- errorClass: $($report.errorClass)
- errorMessageRedacted: $($report.errorMessageRedacted)
- likelyCause: $($report.likelyCause)
- cloudRequestActuallySent: $($report.cloudRequestActuallySent)
- androidInternetPermissionDeclared: $($report.androidInternetPermissionDeclared)
- finalRequestUrlRedacted: $($report.finalRequestUrlRedacted)
- baseUrlJoinValid: $($report.baseUrlJoinValid)
- authorizationHeaderPresent: $($report.authorizationHeaderPresent)
- cleartextBlockedSuspected: $($report.cleartextBlockedSuspected)
- tlsFailureSuspected: $($report.tlsFailureSuspected)
- dnsFailureSuspected: $($report.dnsFailureSuspected)
- networkExceptionClass: $($report.networkExceptionClass)
- networkExceptionMessageRedacted: $($report.networkExceptionMessageRedacted)
"@
Set-Content -LiteralPath $mdPath -Value $markdown -Encoding UTF8

Write-Host "relaySmokeResult=$($report.relaySmokeResult)"
Write-Host "report=$mdPath"
Write-Host "json=$jsonPath"
if ($report.relaySmokeResult -ne "PASS") { exit 2 }
