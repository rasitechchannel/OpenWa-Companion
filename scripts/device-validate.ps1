# OpenWA Companion — physical device validation harness
# Usage: powershell -ExecutionPolicy Bypass -File scripts/device-validate.ps1

$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "E:\OpenWA\android\app\build\outputs\apk\debug\app-debug.apk"
$pkg = "org.rasitech.openwacompanion.debug"
$reportDir = "E:\OpenWA\docs\device-reports"
$ts = Get-Date -Format "yyyyMMdd-HHmmss"
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null
$report = Join-Path $reportDir "device-validation-$ts.md"

function Adb { & $adb @args }
function Log($msg) {
  $line = "[$(Get-Date -Format o)] $msg"
  Write-Host $line
  Add-Content -Path $report -Value $line
}

@"
# Device validation report — $ts

"@ | Set-Content $report -Encoding utf8

$devices = Adb devices | Select-String "`tdevice$"
if (-not $devices) {
  Log "FAIL: no authorized device in adb devices"
  exit 2
}

Log "Devices:"
Adb devices -l | ForEach-Object { Log $_ }

$abi = (Adb shell getprop ro.product.cpu.abi).Trim()
$model = (Adb shell getprop ro.product.model).Trim()
$release = (Adb shell getprop ro.build.version.release).Trim()
$sdk = (Adb shell getprop ro.build.version.sdk).Trim()
Log "model=$model abi=$abi release=$release sdk=$sdk"

if ($abi -notmatch "arm64") {
  Log "WARN: expected arm64-v8a production target; got $abi"
}

Log "Installing $apk"
Adb uninstall $pkg 2>$null | Out-Null
$install = Adb install -r $apk 2>&1 | Out-String
Log $install
if ($install -notmatch "Success") { Log "FAIL: install"; exit 3 }

Log "Launching app"
Adb shell am force-stop $pkg
Adb shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | ForEach-Object { Log $_ }
Start-Sleep -Seconds 5

Log "--- logcat engine/node (15s) ---"
Adb logcat -c
Start-Sleep -Seconds 15
$logs = Adb logcat -d -t 400 *:S OpenWA-Node:V OpenWA-NodeBridge:V OpenWA-Ingester:V AndroidRuntime:E libc:E 2>&1 | Out-String
Add-Content $report ("```\n" + $logs + "\n```")

if ($logs -match "UnsatisfiedLinkError|dlopen failed|Fatal signal") {
  Log "FAIL: native crash / missing library signals in logcat"
} else {
  Log "OK: no immediate native crash signature in filtered logcat"
}

Log "--- dumpsys meminfo ---"
Adb shell dumpsys meminfo $pkg 2>&1 | Select-Object -First 40 | ForEach-Object { Log $_ }

Log "--- package size ---"
Adb shell dumpsys package $pkg 2>&1 | Select-String -Pattern "codePath|versionName|dataDir|primaryCpuAbi" | ForEach-Object { Log $_.Line }

Log "--- sensitive log scan (auth keys) ---"
$sens = Adb logcat -d 2>&1 | Select-String -Pattern "noiseKey|signedIdentityKey|advSecretKey|registrationId" | Select-Object -First 20
if ($sens) { Log "FAIL: potential secret leakage"; $sens | ForEach-Object { Log $_.Line } } else { Log "OK: no auth key patterns in logcat dump" }

Log "Report written: $report"
Write-Host "NEXT: complete QR/pairing interactively on device, then re-run scripts/device-post-pair.ps1"
