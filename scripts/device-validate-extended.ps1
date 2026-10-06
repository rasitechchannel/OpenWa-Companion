$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$reportDir = "E:\OpenWA\docs\device-reports"
function A { & $adb -s $s @args }

function Sync-Status() {
  $tmp = Join-Path $env:TEMP "ow-live.json"
  # exec-out + utf8NoBOM avoids UTF-16/BOM that breaks browser JSON.parse
  $raw = & $adb -s $s exec-out run-as $pkg cat files/nodejs/bridge/status.json 2>$null
  if ($raw) {
    if ($raw -is [array]) { $raw = $raw -join "`n" }
    [IO.File]::WriteAllText((Join-Path $reportDir "status-live.json"), [string]$raw)
    [IO.File]::WriteAllText($tmp, [string]$raw)
    $marker = 'data:image/png;base64,'
    $idx = $raw.IndexOf($marker)
    if ($idx -ge 0) {
      $start = $idx + $marker.Length
      $end = $raw.IndexOf([char]34, $start)
      if ($end -gt $start) {
        try {
          $bytes = [Convert]::FromBase64String($raw.Substring($start, $end - $start))
          [IO.File]::WriteAllBytes((Join-Path $reportDir "openwa-qr-live.png"), $bytes)
        } catch {}
      }
    }
    return [string]$raw
  }
  return ""
}

function Get-Field([string]$raw, [string]$key) {
  $pat = [string]::Format('"{0}"\s*:\s*"([^"]*)"', $key)
  $m = [regex]::Match($raw, $pat)
  if ($m.Success) { return $m.Groups[1].Value }
  return ""
}

Write-Host "=== Session / process / network validation (pre-pair capable) ==="
$results = New-Object System.Collections.Generic.List[string]

# 1) Engine alive
$raw = Sync-Status
$results.Add("engine_connection=" + (Get-Field $raw "connection"))
$results.Add("engine_node=" + (Get-Field $raw "node"))
$results.Add("pairingCode=" + (Get-Field $raw "pairingCode"))

# 2) Force-stop and reopen
A shell am force-stop $pkg
Start-Sleep 2
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity"
Start-Sleep 5
A shell uiautomator dump /sdcard/u.xml | Out-Null
A pull /sdcard/u.xml "$reportDir\u-reopen.xml" | Out-Null
$xml = Get-Content "$reportDir\u-reopen.xml" -Raw
$match = [regex]::Match($xml, 'text="Start engine / show QR".*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
if (-not $match.Success) {
  $match = [regex]::Match($xml, 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*text="Start engine / show QR"')
}
if ($match.Success) {
  $cx = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
  $cy = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
  A shell input tap $cx $cy
  Start-Sleep 12
}
$raw = Sync-Status
$conn = Get-Field $raw "connection"
$results.Add("after_force_stop_reopen_connection=$conn")
$results.Add("after_force_stop_node=" + (Get-Field $raw "node"))

# 3) Kill via am kill (process death)
$appPid = (A shell pidof $pkg).Trim()
if ($appPid) {
  A shell "kill -9 $appPid" 2>$null
  Start-Sleep 2
}
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity"
Start-Sleep 5
# restart engine again
A shell uiautomator dump /sdcard/u.xml | Out-Null
A pull /sdcard/u.xml "$reportDir\u-kill.xml" | Out-Null
$xml = Get-Content "$reportDir\u-kill.xml" -Raw
$match = [regex]::Match($xml, 'text="Start engine / show QR".*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
if (-not $match.Success) {
  $match = [regex]::Match($xml, 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*text="Start engine / show QR"')
}
if ($match.Success) {
  $cx = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
  $cy = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
  A shell input tap $cx $cy
  Start-Sleep 12
}
$raw = Sync-Status
$results.Add("after_kill_reopen_connection=" + (Get-Field $raw "connection"))
$results.Add("after_kill_node=" + (Get-Field $raw "node"))

# 4) Screen off / on
A shell input keyevent 26
Start-Sleep 3
A shell input keyevent 26
Start-Sleep 1
A shell input keyevent 82
Start-Sleep 2
$raw = Sync-Status
$results.Add("after_screen_toggle_connection=" + (Get-Field $raw "connection"))

# 5) Background app
A shell input keyevent 3
Start-Sleep 5
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity"
Start-Sleep 3
$raw = Sync-Status
$results.Add("after_home_resume_connection=" + (Get-Field $raw "connection"))

# 6) Airplane mode bounce (needs secure settings; may fail)
A shell settings put global airplane_mode_on 1 2>$null
A shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true 2>$null | Out-Null
Start-Sleep 4
A shell settings put global airplane_mode_on 0 2>$null
A shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false 2>$null | Out-Null
Start-Sleep 8
$raw = Sync-Status
$results.Add("after_airplane_bounce_connection=" + (Get-Field $raw "connection"))

# 7) Sensitive logcat scan
$sens = A logcat -d -t 1000 | Select-String -Pattern "noiseKey|signedIdentityKey|advSecretKey|HL92FZ6S|pairingCode|privateKey|authState"
$results.Add("sensitive_log_hits=$($sens.Count)")
if ($sens.Count -gt 0) {
  $sens | Select-Object -First 5 | ForEach-Object { $results.Add("SENS: " + $_.Line.Substring(0, [Math]::Min(160, $_.Line.Length))) }
}

# 8) Notification channels
$ch = A shell dumpsys notification_listener 2>$null
$ch2 = A shell dumpsys notification --noredact 2>$null | Select-String -Pattern "openwa|OpenWA|engine_sync|privacy" | Select-Object -First 20
$results.Add("notification_dump_hits=$($ch2.Count)")

# 9) Memory / CPU snapshot
$appPid = (A shell pidof $pkg).Trim()
if ($appPid) {
  $rss = (A shell "awk '/VmRSS:/{print `$2}' /proc/$appPid/status").Trim()
  $results.Add("vm_rss_kb=$rss")
  $cpu = A shell "top -n 1 -p $appPid" 2>$null | Select-Object -Last 3
  $results.Add("top=" + (($cpu -join " ") -replace '\s+', ' '))
}
$mem = A shell dumpsys meminfo $pkg | Select-String "TOTAL PSS|TOTAL RSS|Java Heap|Native Heap|Graphics" | Select-Object -First 10
$mem | ForEach-Object { $results.Add("mem: " + $_.Line.Trim()) }

# 10) Cache growth
$cache = A shell "run-as $pkg du -sk cache files no_backup 2>/dev/null"
$results.Add("du=$cache")

$results | Set-Content "$reportDir\session-process-network.txt"
$results | ForEach-Object { Write-Host $_ }

# Long poll for open (8 min) while primary may enter code/scan
Write-Host "=== Polling for connection=open (8 min) ==="
$deadline = (Get-Date).AddMinutes(8)
$paired = $false
while ((Get-Date) -lt $deadline) {
  $raw = Sync-Status
  $conn = Get-Field $raw "connection"
  $pc = Get-Field $raw "pairingCode"
  Write-Host ("{0:T} connection={1} pairingCode={2}" -f (Get-Date), $conn, $pc)
  if ($conn -eq "open") {
    Copy-Item (Join-Path $reportDir "status-live.json") (Join-Path $reportDir "status-open.json") -Force
    $paired = $true
    break
  }
  Start-Sleep 5
}
"paired=$paired time=$(Get-Date -Format o)" | Set-Content "$reportDir\pair-result.txt"
if ($paired) { exit 0 } else { exit 4 }
