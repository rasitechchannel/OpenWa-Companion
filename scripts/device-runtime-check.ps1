$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$out = "E:\OpenWA\docs\device-reports\runtime-prepair.txt"
function A { & $adb -s $s @args }

$lines = New-Object System.Collections.Generic.List[string]
function L([string]$t) { $lines.Add($t); Write-Host $t }

L "=== OpenWA Physical Runtime Pre-Pair Report ==="
L "time=$(Get-Date -Format o)"
L "device=$(A shell getprop ro.product.model)"
L "abi=$(A shell getprop ro.product.cpu.abi)"
L "sdk=$(A shell getprop ro.build.version.sdk)"
L "release=$(A shell getprop ro.build.version.release)"
L "page_size=$(A shell getconf PAGE_SIZE)"

$pidApp = (A shell pidof $pkg).Trim()
L "app_pid=$pidApp"

# Native libs loaded
$maps = A shell "cat /proc/$pidApp/maps 2>/dev/null" | Out-String
foreach ($lib in @("libopenwa-node.so","libnode.so","libc++_shared.so")) {
  $hit = $maps -match [regex]::Escape($lib)
  L "maps_$lib=$hit"
}

# Missing libs / crashes in logcat
$lc = A logcat -d -t 400 *:E *:W | Out-String
$miss = [regex]::Matches($lc, "dlopen failed|UnsatisfiedLinkError|MISSING.*\.so|Fatal signal|AndroidRuntime") | ForEach-Object { $_.Value } | Select-Object -Unique
L "logcat_native_errors=$($miss -join '; ')"

# status.json essentials
$tmp = Join-Path $env:TEMP "ow-status.json"
A shell run-as $pkg cat files/nodejs/bridge/status.json > $tmp 2>$null
$raw = Get-Content $tmp -Raw -ErrorAction SilentlyContinue
if ($raw) {
  $node = [regex]::Match($raw,'"node"\s*:\s*"([^"]+)"').Groups[1].Value
  $bail = [regex]::Match($raw,'"baileys"\s*:\s*"([^"]+)"').Groups[1].Value
  $conn = [regex]::Match($raw,'"connection"\s*:\s*"([^"]+)"').Groups[1].Value
  $ok = [regex]::Match($raw,'"ok"\s*:\s*(true|false)').Groups[1].Value
  L "status_ok=$ok node=$node baileys=$bail connection=$conn"
}

# Node RSS estimate from smaps
if ($pidApp) {
  $rssKb = A shell "awk '/^Rss:/{s+=`$2} END{print s}' /proc/$pidApp/smaps 2>/dev/null"
  L "app_rss_kb=$($rssKb.Trim())"
  $pss = A shell dumpsys meminfo $pkg | Select-String -Pattern "TOTAL PSS|TOTAL:" | Select-Object -First 3
  L "meminfo=$($pss -join ' | ')"
}

# APK size
$apkPath = (A shell pm path $pkg).Replace("package:","").Trim()
$apkSize = A shell "stat -c %s $apkPath 2>/dev/null"
L "apk_path=$apkPath"
L "apk_installed_bytes=$($apkSize.Trim())"

# Extract native libs from APK and check 16KB alignment offline if llvm-readelf available
$reportAlign = "E:\OpenWA\docs\device-reports\elf-align-check.txt"
$apkLocal = "E:\OpenWA\android\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkLocal) {
  $tmpDir = Join-Path $env:TEMP "openwa-elf-check"
  New-Item -ItemType Directory -Force -Path $tmpDir | Out-Null
  Add-Type -AssemblyName System.IO.Compression.FileSystem
  $zip = [System.IO.Compression.ZipFile]::OpenRead($apkLocal)
  foreach ($e in $zip.Entries) {
    if ($e.FullName -match 'lib/arm64-v8a/(libopenwa-node|libnode|libc\+\+_shared)\.so$') {
      $dest = Join-Path $tmpDir (Split-Path $e.FullName -Leaf)
      [System.IO.Compression.ZipFileExtensions]::ExtractToFile($e, $dest, $true)
    }
  }
  $zip.Dispose()
  $readelf = @(
    "C:\Users\LENOVO\AppData\Local\Android\Sdk\ndk\27.2.12479018\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-readelf.exe",
    "C:\Users\LENOVO\AppData\Local\Android\Sdk\ndk\27.0.12077973\toolchains\llvm\prebuilt\windows-x86_64\bin\llvm-readelf.exe"
  ) | Where-Object { Test-Path $_ } | Select-Object -First 1
  $alignLines = @()
  if ($readelf) {
    Get-ChildItem $tmpDir -Filter *.so | ForEach-Object {
      $info = & $readelf -l $_.FullName 2>&1 | Out-String
      $alignMatch = [regex]::Matches($info, "LOAD\s+0x[0-9a-fA-F]+\s+0x[0-9a-fA-F]+\s+0x[0-9a-fA-F]+\s+0x[0-9a-fA-F]+\s+0x[0-9a-fA-F]+\s+(0x[0-9a-fA-F]+)")
      $aligns = ($alignMatch | ForEach-Object { $_.Groups[1].Value }) -join ","
      $alignLines += "$($_.Name) Align=$aligns"
    }
  } else {
    $alignLines += "llvm-readelf not found"
  }
  $alignLines | Set-Content $reportAlign
  L "elf_align_report=$reportAlign"
  $alignLines | ForEach-Object { L $_ }
}

# Sensitive log scan (auth keys / message bodies)
$sens = A logcat -d -t 800 | Select-String -Pattern "noiseKey|signedIdentityKey|advSecretKey|message\.conversation|authState|creds\.json|privateKey" -CaseSensitive:$false
L "sensitive_log_hits=$($sens.Count)"
if ($sens.Count -gt 0) {
  $sens | Select-Object -First 5 | ForEach-Object { L ("SENSITIVE: " + $_.Line.Substring(0, [Math]::Min(120, $_.Line.Length))) }
}

# backup rules presence
$bak = A shell dumpsys package $pkg | Select-String -Pattern "allowBackup|dataDir"
L "package_dump_backup=$($bak -join ' | ')"

$lines | Set-Content $out
Write-Host "Wrote $out"
