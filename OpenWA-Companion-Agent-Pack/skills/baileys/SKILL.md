# Skill: Baileys Full Coverage

## Core rule
Jangan memakai hard-coded daftar capability saja. Setelah dependency Baileys dipin:
1. Parse/inspect `BaileysEventMap`.
2. Enumerate exported socket methods.
3. Enumerate message/proto types yang relevan.
4. Generate/update `BAILEYS_COVERAGE_MATRIX.md`.
5. Tidak boleh ada event yang belum diklasifikasikan.

## Event handling
Prefer batched `sock.ev.process(...)` bila sesuai dengan versi upstream karena membantu konsistensi state pada event batch besar.

## Event classifications
- IMPLEMENTED_UI
- IMPLEMENTED_BACKGROUND
- CAPTURED_ONLY
- UNSUPPORTED_UPSTREAM_ACTION
- NOT_APPLICABLE
- DEPRECATED

Setiap `CAPTURED_ONLY` wajib disimpan/ditangani tanpa crash dan ditampilkan di diagnostics developer mode dengan payload yang sudah diredaksi.

## Version policy
- Gunakan stable release jika tersedia dan kompatibel.
- RC/edge hanya bila ada alasan teknis kuat.
- Pin exact version/commit.
- Catat migration notes.
