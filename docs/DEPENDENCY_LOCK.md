# Dependency Lock — OpenWA Companion

Pinned after audit on 2026-10-06. Refresh before production release.

| Component | Source | Exact Version/Commit | License | Purpose | Verified |
|---|---|---|---|---|---|
| Baileys | npm `@whiskeysockets/baileys` / https://github.com/WhiskeySockets/Baileys | **6.7.24** (stable pin for first pairing POC; evaluate 7.0.0-rc14 after engine soak) | MIT | WhatsApp multi-device protocol client | ☑ engines `node>=20`; MIT on npm |
| Embedded Node | https://github.com/fogtape/nodejs-mobile | **v26.10.0-0 lite** (`nodejs-mobile-android-lite-26.10.0-0.zip`) SHA256 `f09e6a565feb5619dc1e8f424fa354b635a1df22da3414c57f6df11c46b01453` | MIT (Node.js + deps per NOTICE) | `libnode.so` in-APK runtime | ☑ checksum; arm64 LOAD align `0x4000` (16 KB) |
| Android Gradle Plugin | Google | 8.9.1 | Apache-2.0 | Build | ☑ |
| Kotlin | JetBrains | 2.1.20 | Apache-2.0 | Language | ☑ |
| Compose BOM | AndroidX | 2025.08.00 | Apache-2.0 | UI | ☑ |
| Navigation Compose | AndroidX | 2.9.3 | Apache-2.0 | Nav | ☑ |
| Security Crypto | AndroidX | 1.0.0 | Apache-2.0 | Keystore prefs | ☑ |
| Biometric | AndroidX | 1.1.0 | Apache-2.0 | App lock | ☑ |
| Room | AndroidX | 2.7.2 | Apache-2.0 | Local DB | ☑ |
| Coroutines | JetBrains | 1.10.2 | Apache-2.0 | Async | ☑ |
| Material Components | Google | 1.12.0 | Apache-2.0 | XML theme host | ☑ |
| NDK | Android SDK | r27+ / project uses SDK NDK with `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON` | Apache-2.0 / NDK terms | Native bridge build | ☑ strategy |

## ABI
- Production: `arm64-v8a` (required)
- Debug/emulator: `x86_64`
- `armeabi-v7a` not shipped in app module (available in third_party archive if needed)

## Rejected / deferred
| Candidate | Reason |
|---|---|
| nodejs-mobile official v18.20.4 | Node 18 < Baileys engines `>=20` |
| Baileys 7.0.0-rc14 as default pin | RC; adopt after pairing + history sync soak on device |
| External message relay / developer VPS | Forbidden by project rules |

## 16 KB page-size
`llvm-readelf -l` on `bin/arm64-v8a/libnode.so` shows LOAD Align `0x4000`. CMake enables `ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON` for `libopenwa-node.so`.
