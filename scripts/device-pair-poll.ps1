$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$apk = "E:\OpenWA\android\app\build\outputs\apk\debug\app-debug.apk"
$reportDir = "E:\OpenWA\docs\device-reports"

function A { & $adb -s $s @args }

Write-Host "Building..."
Push-Location E:\OpenWA\android
& .\gradlew.bat :app:assembleDebug
Pop-Location

Write-Host "Installing..."
A install -r $apk
A shell am force-stop org.rasitech.openwacompanion.debug
A logcat -c
A shell am start -n org.rasitech.openwacompanion.debug/org.rasitech.openwacompanion.MainActivity
Start-Sleep -Seconds 5

A shell uiautomator dump /sdcard/u.xml | Out-Null
A pull /sdcard/u.xml "$reportDir\u-latest.xml" | Out-Null
$xml = Get-Content "$reportDir\u-latest.xml" -Raw
$match = [regex]::Match($xml, 'text="Start engine / show QR"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
if (-not $match.Success) {
  $match = [regex]::Match($xml, 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*text="Start engine / show QR"')
}
if ($match.Success) {
  $cx = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
  $cy = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
  Write-Host "Tapping Start engine at $cx,$cy"
  A shell input tap $cx $cy
} else {
  Write-Host "WARN: Start button not found"
}

Start-Sleep -Seconds 12
Write-Host "=== SCAN QR WITH WHATSAPP LINKED DEVICES NOW ==="
$deadline = (Get-Date).AddSeconds(300)
$paired = $false
while ((Get-Date) -lt $deadline) {
  $tmp = Join-Path $env:TEMP "openwa-status.json"
  A shell run-as org.rasitech.openwacompanion.debug cat files/nodejs/bridge/status.json > $tmp 2>$null
  if (Test-Path $tmp) {
    $raw = Get-Content $tmp -Raw -ErrorAction SilentlyContinue
    if ($raw) {
      $connMatch = [regex]::Match($raw, '"connection"\s*:\s*"([^"]+)"')
      $conn = if ($connMatch.Success) { $connMatch.Groups[1].Value } else { "unknown" }
      $nodeMatch = [regex]::Match($raw, '"node"\s*:\s*"([^"]+)"')
      $node = if ($nodeMatch.Success) { $nodeMatch.Groups[1].Value } else { "?" }
      Write-Host ("{0:T} connection={1} node={2}" -f (Get-Date), $conn, $node)
      if ($conn -eq "open") {
        Copy-Item $tmp "$reportDir\status-open.json" -Force
        $paired = $true
        break
      }
    }
  }
  Start-Sleep -Seconds 5
}

Write-Host "paired=$paired"
A shell screencap -p /sdcard/openwa-final.png
A pull /sdcard/openwa-final.png "$reportDir\openwa-final.png" | Out-Null

# Runtime facts
$pidApp = (A shell pidof org.rasitech.openwacompanion.debug).Trim()
@"
paired=$paired
pid=$pidApp
time=$(Get-Date -Format o)
"@ | Set-Content "$reportDir\pair-result.txt"

if ($paired) {
  Write-Host "SUCCESS: session open"
  exit 0
} else {
  Write-Host "TIMEOUT: still waiting for QR scan"
  exit 4
}
