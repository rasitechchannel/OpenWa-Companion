# Third-Party Notices — OpenWA Companion

## Baileys
- Project: WhiskeySockets/Baileys
- Role: WhatsApp Web / multi-device protocol client
- Source: https://github.com/WhiskeySockets/Baileys
- Version: `@whiskeysockets/baileys@6.7.24` (pin; 7.x evaluation pending)
- License: MIT
- Modifications: none yet (to be adapted behind OpenWA adapter)

## Embedded Node.js runtime
- Project: fogtape/nodejs-mobile (Node.js for Mobile Apps builds)
- Role: In-APK `libnode.so` runtime
- Source: https://github.com/fogtape/nodejs-mobile
- Version: `v26.10.0-0` Android **lite**
- Artifact: `nodejs-mobile-android-lite-26.10.0-0.zip`
- SHA256: `f09e6a565feb5619dc1e8f424fa354b635a1df22da3414c57f6df11c46b01453`
- License: MIT (Node.js) + third-party notices in upstream `NOTICE.md` / `LICENSE`
- Provenance: GitHub Release assets; checksum verified locally 2026-10-06
- 16 KB: arm64 LOAD Align `0x4000` verified via `llvm-readelf`
- Modifications: none to binary; OpenWA JNI wrapper is separate (`openwa-node`)

## Node.js upstream
- https://github.com/nodejs/node — MIT and bundled licenses (see `licenses/NODE_LICENSE`)

## AndroidX / Jetpack / Kotlin
Exact versions: see `docs/DEPENDENCY_LOCK.md`. Licenses Apache-2.0 unless noted.

## OpenWA original assets
Starter branding under `OpenWA-Companion-Agent-Pack/assets/` — Dual Chat Link concept; not WhatsApp assets.

## Disclaimer
OpenWA Companion is an unofficial open-source project and is not affiliated with WhatsApp LLC or Meta Platforms, Inc.
