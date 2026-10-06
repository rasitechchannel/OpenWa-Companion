# OpenWA Companion

Unofficial open-source Android companion client for WhatsApp multi-device linking.

**Local-first.** Messages and credentials stay on the device. OpenWA does not run a message backend.

> Not affiliated with WhatsApp LLC or Meta Platforms.

## Features (in progress)

- Embedded Node.js + [Baileys](https://github.com/WhiskeySockets/Baileys) on Android
- QR / pairing-code link flow (companion device)
- Room-backed chats, history ingest, media as local file paths
- Multi-account isolation, Keystore-backed prefs, app lock
- WhatsApp-inspired UI with OpenWA branding (no proprietary WhatsApp assets)

## Stack

| Layer | Tech |
|---|---|
| UI | Kotlin, Jetpack Compose |
| Engine | Embedded Node (`libnode`) + Baileys 6.7.24 |
| Storage | Room / SQLite, app-private media |
| Security | Android Keystore / EncryptedSharedPreferences |
| Package | `org.rasitech.openwacompanion` |

## Requirements

- JDK 17+
- Android SDK (API 36), NDK **27.2.12479018**, CMake 3.22+
- PowerShell (Windows) or equivalent for setup scripts
- Node.js 20+ on the host (to install/package the engine)

## Setup

1. Clone this repository.

2. Download Node mobile lite (if missing under `third_party/`):

```powershell
gh release download v26.10.0-0 --repo fogtape/nodejs-mobile `
  --pattern "nodejs-mobile-android-lite-26.10.0-0.zip" `
  --pattern "SHA256SUMS" --dir third_party
Expand-Archive third_party\nodejs-mobile-android-lite-26.10.0-0.zip `
  -DestinationPath third_party\nodejs-mobile-android-lite-26.10.0-0 -Force
.\scripts\setup-native.ps1
```

3. Install and package the Baileys engine bundle:

```powershell
cd engine
npm ci
cd ..
.\scripts\package-engine.ps1
```

4. Create `android/local.properties`:

```
sdk.dir=C:\\Users\\YOU\\AppData\\Local\\Android\\Sdk
```

5. Build:

```powershell
cd android
.\gradlew.bat :app:assembleDebug
```

## Docs

- [`docs/DEPENDENCY_LOCK.md`](docs/DEPENDENCY_LOCK.md)
- [`docs/BAILEYS_COVERAGE_MATRIX.md`](docs/BAILEYS_COVERAGE_MATRIX.md)
- [`docs/FINAL_CHECKLIST_PROGRESS.md`](docs/FINAL_CHECKLIST_PROGRESS.md)
- [`docs/ui-refs/UI_COMPARISON.md`](docs/ui-refs/UI_COMPARISON.md)
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)
- Agent pack: [`OpenWA-Companion-Agent-Pack/START_HERE.md`](OpenWA-Companion-Agent-Pack/START_HERE.md)

## License / notices

This project is released under the [MIT License](LICENSE).

Third-party licenses are listed in `THIRD_PARTY_NOTICES.md` and `licenses/`.

## Contributing

This project is under active development. Prefer small PRs; do not commit secrets, device pairing QR codes, or personal chat screenshots.
