# ADR-002: Native bridge and layered architecture

## Context
UI must not depend on raw Baileys payloads. Preferred bridge is Kotlin ↔ JNI ↔ embedded Node.

## Options
1. JNI/`node::Start` with JS engine process — preferred.
2. Loopback HTTP/WS bridge — fallback only if native bridge proves unworkable.
3. WebView UI driving Node — rejected (Compose is primary UI).

## Decision
Implement **JNI native bridge** (`libopenwa-node.so` + `NodeBridge`). Domain flow:

`Baileys → Adapter/Normalizer → Domain Events → Repository (Room) → ViewModel → Compose`

Multi-account from day one: isolated auth dirs under `noBackupFilesDir/accounts/<accountId>`.

## Consequences
- Single Node instance per process (upstream limitation).
- Multi-account multiplexed inside one JS engine with separated session stores, or sequential connect — to be decided in pairing batch; storage isolation starts now.
- Loopback cleartext allowed only for `localhost`/`127.0.0.1` in network security config as escape hatch.

## License/Security impact
Auth keys never exposed to Compose. Backup rules exclude auth/DB/media.

## Rollback plan
Documented localhost WS fallback behind a feature flag, never as default production path without ADR update.
