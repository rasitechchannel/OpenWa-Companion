$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$src = Join-Path $root "third_party\nodejs-mobile-android-lite-26.10.0-0"
if (-not (Test-Path (Join-Path $src "bin\arm64-v8a\libnode.so"))) {
    throw "Missing $src — download fogtape release v26.10.0-0 lite first (see README)."
}

$jni = Join-Path $root "android\app\src\main\jniLibs"
$libnode = Join-Path $root "android\app\libnode"
New-Item -ItemType Directory -Force -Path (Join-Path $libnode "include") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $libnode "bin") | Out-Null

Copy-Item (Join-Path $src "include\*") (Join-Path $libnode "include\") -Recurse -Force

foreach ($abi in @("arm64-v8a", "x86_64")) {
    $jniAbi = Join-Path $jni $abi
    $binAbi = Join-Path $libnode "bin\$abi"
    New-Item -ItemType Directory -Force -Path $jniAbi, $binAbi | Out-Null
    Copy-Item (Join-Path $src "bin\$abi\libnode.so") (Join-Path $jniAbi "libnode.so") -Force
    Copy-Item (Join-Path $src "bin\$abi\libnode.so") (Join-Path $binAbi "libnode.so") -Force
}

Write-Host "Native Node libraries staged under android/app/jniLibs and android/app/libnode"
