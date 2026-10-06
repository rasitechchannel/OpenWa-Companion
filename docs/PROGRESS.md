# Progress — 2026-10-06

## Done
### Batch 01 — Bootstrap
- Android Kotlin/Compose app at `android/` (`org.rasitech.openwacompanion`)
- Dependency audit + pins in `docs/DEPENDENCY_LOCK.md`
- ADRs: embedded Node (fogtape 26.10.0-0 lite), JNI bridge architecture
- Design tokens/references + third-party notices
- Debug APK builds successfully
- 16 KB LOAD alignment verified for `libnode.so`, `libopenwa-node.so`, `libc++_shared.so`

### Batch 02 — Embedded Node POC (code complete; device pending)
- JNI bridge `libopenwa-node.so` → `node::Start`
- Env setup per fogtape EMBEDDING.md (`TMPDIR`, `HOME`, compile cache, chdir)
- Compile-time Node version probe from headers (26.x)

### Batch 03 — Baileys pairing (code complete; device pending)
- Engine package with `@whiskeysockets/baileys@6.7.24`
- Bundled into APK assets (`engine-bundle.zip` ~9.4 MB)
- File-bridge status/commands + QR / pairing-code / logout debug UI
- Auth state path: `noBackupFilesDir/accounts/<accountId>/auth`

## Blocked / next
- No physical arm64 device attached via adb in this session
- On device: Start engine → scan QR → confirm session persistence across restart
- Then continue batches 04–12 (event matrix, Room sync, UI, media, security, release)

## Build
```powershell
.\scripts\setup-native.ps1
.\scripts\package-engine.ps1
cd android
.\gradlew.bat :app:assembleDebug
# APK: android/app/build/outputs/apk/debug/app-debug.apk
```
