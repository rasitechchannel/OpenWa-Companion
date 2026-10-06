# Skill: Build & Release

- release signing configured outside source control
- no secrets in repo
- R8/minification tested with embedded runtime
- deterministic dependency lock
- SBOM generated
- APK/AAB page-size compatibility checked
- `arm64-v8a` production artifact
- optional `x86_64` debug artifact
- version info includes Baileys + embedded Node version in diagnostics
- GitHub/F-Droid style open-source distribution may be supported after policy/license audit
- auto-update must be opt-in and cryptographically verified if implemented
