$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem

$root = Split-Path -Parent $PSScriptRoot
$engine = Join-Path $root "engine"
$staging = Join-Path $root "third_party\engine-staging"
$outDir = Join-Path $root "android\app\src\main\assets\nodejs"
$bundle = Join-Path $outDir "engine-bundle.zip"

Remove-Item $staging -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $staging, $outDir | Out-Null
Copy-Item (Join-Path $engine "package.json") (Join-Path $staging "package.json")
Copy-Item (Join-Path $engine "src") (Join-Path $staging "src") -Recurse
Copy-Item (Join-Path $engine "node_modules") (Join-Path $staging "node_modules") -Recurse
Get-ChildItem (Join-Path $staging "node_modules") -Recurse -Filter "*.node" -ErrorAction SilentlyContinue |
    Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem (Join-Path $staging "node_modules\@img") -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -match "sharp-(win32|darwin|linux)" } |
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue

$boot = @'
'use strict';
const path = require('path');
const fs = require('fs');
const { pathToFileURL } = require('url');
async function main() {
  const entry = path.join(__dirname, 'src', 'main.mjs');
  await import(pathToFileURL(entry).href);
}
main().catch((err) => {
  const bridge = path.join(process.cwd(), 'bridge');
  fs.mkdirSync(bridge, { recursive: true });
  fs.writeFileSync(
    path.join(bridge, 'status.json'),
    JSON.stringify({ ok: false, connection: 'error', lastError: String(err) }, null, 2),
  );
  console.error(err);
  process.exitCode = 1;
});
'@
Set-Content -Path (Join-Path $staging "main.js") -Value $boot -Encoding utf8

if (Test-Path $bundle) { Remove-Item $bundle -Force }
$zip = [System.IO.Compression.ZipFile]::Open($bundle, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    Get-ChildItem $staging -Recurse -File | ForEach-Object {
        $rel = $_.FullName.Substring($staging.Length).TrimStart("\").Replace("\", "/")
        [void][System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
            $zip,
            $_.FullName,
            $rel,
            [System.IO.Compression.CompressionLevel]::Optimal
        )
    }
}
finally {
    $zip.Dispose()
}

Copy-Item (Join-Path $staging "main.js") (Join-Path $outDir "main.js") -Force
$sha = (Get-FileHash $bundle -Algorithm SHA1).Hash.ToLower()
Set-Content "$bundle.sha1" $sha -Encoding ascii -NoNewline
Write-Host "Packed $bundle size=$((Get-Item $bundle).Length) sha1=$sha"
