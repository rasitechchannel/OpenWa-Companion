$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$reportDir = "E:\OpenWA\docs\device-reports"
$phone = "6285128006442"
$utf8 = New-Object System.Text.UTF8Encoding $false
function A { & $adb -s $s @args }

function Sync-Status {
  $raw = & $adb -s $s exec-out run-as $pkg cat files/nodejs/bridge/status.json 2>$null
  if (-not $raw) { return "" }
  if ($raw -is [array]) { $raw = $raw -join "`n" }
  $raw = [string]$raw
  [IO.File]::WriteAllText((Join-Path $reportDir "status-live.json"), $raw, $utf8)
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
  return $raw
}

function Get-Field([string]$raw, [string]$key) {
  $m = [regex]::Match($raw, [string]::Format('"{0}"\s*:\s*"([^"]*)"', $key))
  if ($m.Success) { return $m.Groups[1].Value }
  return ""
}

function Ensure-Engine {
  A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
  Start-Sleep 3
  $raw = Sync-Status
  if ($raw -and (Get-Field $raw "connection") -match 'qr|pairing-code|connecting|open') { return $raw }
  A shell uiautomator dump /sdcard/u.xml | Out-Null
  A pull /sdcard/u.xml (Join-Path $reportDir "u-pair.xml") | Out-Null
  $xml = Get-Content (Join-Path $reportDir "u-pair.xml") -Raw
  $match = [regex]::Match($xml, 'text="Start engine / show QR".*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
  if (-not $match.Success) {
    $match = [regex]::Match($xml, 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*text="Start engine / show QR"')
  }
  if ($match.Success) {
    $cx = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
    $cy = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
    Write-Host "Tapping Start engine $cx,$cy"
    A shell input tap $cx $cy
    Start-Sleep 12
  }
  return (Sync-Status)
}

Write-Host "=== Fresh pairing session ==="
# Clear auth for clean QR/code (keep app installed)
A shell "run-as $pkg sh -c 'rm -rf no_backup/accounts/default/auth; mkdir -p no_backup/accounts/default/auth'"
A shell am force-stop $pkg
Start-Sleep 1
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
Start-Sleep 4
$raw = Ensure-Engine
Write-Host ("engine connection=" + (Get-Field $raw "connection") + " node=" + (Get-Field $raw "node"))

# Request new pairing code
$payload = '{"type":"request-pairing-code","phone":"' + $phone + '"}'
[IO.File]::WriteAllText((Join-Path $env:TEMP "openwa-command.json"), $payload)
A push (Join-Path $env:TEMP "openwa-command.json") /data/local/tmp/openwa-command.json | Out-Null
A shell "cat /data/local/tmp/openwa-command.json | run-as $pkg tee files/nodejs/bridge/command.json > /dev/null"
Start-Sleep 8
$raw = Sync-Status
$code = ([regex]::Match($raw, '"pairingCode"\s*:\s*(null|"[^"]*")')).Groups[1].Value
Write-Host ("NEW pairingCode=" + $code)
Write-Host "SCAN QR or enter code on PRIMARY WhatsApp Linked Devices"
Write-Host "Helper: http://127.0.0.1:8765/qr-scan.html"

$deadline = (Get-Date).AddMinutes(15)
$paired = $false
while ((Get-Date) -lt $deadline) {
  $raw = Sync-Status
  $conn = Get-Field $raw "connection"
  $pc = ([regex]::Match($raw, '"pairingCode"\s*:\s*(null|"[^"]*")')).Groups[1].Value
  $me = ([regex]::Match($raw, '"me"\s*:\s*(null|\{[^}]*\}|"[^"]*")')).Groups[1].Value
  Write-Host ("{0:T} connection={1} pairingCode={2} me={3}" -f (Get-Date), $conn, $pc, $me)
  if ($conn -eq "open") {
    Copy-Item (Join-Path $reportDir "status-live.json") (Join-Path $reportDir "status-open.json") -Force
    $paired = $true
    break
  }
  # If logged-out, restart engine
  if ($conn -eq "logged-out" -or $conn -eq "error") {
    Write-Host "Recovering from $conn"
    A shell am force-stop $pkg
    Start-Sleep 1
    Ensure-Engine | Out-Null
    Start-Sleep 2
    A shell "cat /data/local/tmp/openwa-command.json | run-as $pkg tee files/nodejs/bridge/command.json > /dev/null"
  }
  Start-Sleep 5
}

"paired=$paired time=$(Get-Date -Format o)" | Set-Content (Join-Path $reportDir "pair-result.txt")
if ($paired) {
  Write-Host "SUCCESS: connection=open"
  exit 0
}
Write-Host "TIMEOUT: still not open"
exit 4
