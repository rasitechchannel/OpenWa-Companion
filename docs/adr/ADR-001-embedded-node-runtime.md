# ADR-001: Embedded Node runtime selection

## Context
Baileys (6.7.24 and 7.x) requires Node.js `>=20`. Classic `nodejs-mobile` releases top out at Node 18.20.4 and historically lagged 16 KB page alignment.

## Options
1. Ship Node 18 via official nodejs-mobile — **incompatible** with Baileys engines.
2. Build Node 20+/24 from source ourselves — high cost, delayed POC.
3. Use fogtape/nodejs-mobile Android artifacts (Node 24/26) with published SHA256 — meets engines + 16 KB.

## Decision
Pin **fogtape/nodejs-mobile `v26.10.0-0` lite** for Android `libnode.so`, verify checksum and LOAD alignment, package inside the APK (no runtime download).

## Consequences
- APK size grows (~70+ MB per ABI for libnode).
- Lite build has no ICU/`Intl`; acceptable for Baileys if we avoid ICU-dependent formatting in the engine JS.
- Must set `TMPDIR`, `HOME`, compile cache, and chdir before `node::Start` (fogtape EMBEDDING.md).
- Attribution and Node NOTICE must ship in `THIRD_PARTY_NOTICES.md` / licenses.

## License/Security impact
MIT-style Node license; ship notices. No unaudited binary — checksum pinned. Credentials stay in `no_backup` paths.

## Rollback plan
Retarget to a newer fogtape tag or a self-built Node LTS once provenance is equal or better; keep JNI surface (`NodeBridge`) stable.
