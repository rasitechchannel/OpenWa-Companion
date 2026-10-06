# OpenWA Companion — AI Agent Pack

Paket ini adalah specification/skill pack untuk membangun **OpenWA Companion**, aplikasi Android open-source, local-first, unofficial companion client yang menggunakan Baileys dan embedded Node.js di dalam APK.

## Cara pakai
1. AI agent WAJIB membaca `MASTER_SKILL.md`, `PROJECT_RULES.md`, `specs/ARCHITECTURE.md`, dan `FINAL_CHECKLIST.md`.
2. Kerjakan project secara batch sesuai urutan file dalam folder `prompts/`.
3. Jangan melompat ke polishing UI sebelum POC embedded Node.js + Baileys benar-benar berjalan di device Android fisik.
4. Jangan membuat ulang komponen yang sudah tersedia dan lisensinya kompatibel. Reuse lebih diprioritaskan daripada reimplementasi.
5. Jangan membuat fitur palsu, mock backend, fake network state, atau tombol yang tidak melakukan fungsi nyata pada release build.
6. Setiap dependency, fork, source UI, icon pack, atau asset eksternal wajib dicatat dalam `THIRD_PARTY_NOTICES.md`.

## Target produk
- Nama: **OpenWA Companion**
- Deskripsi: **Unofficial open-source companion client**
- Package default: `org.rasitech.openwacompanion`
- Platform: Android native
- UI: Kotlin + Jetpack Compose
- Engine: Embedded Node.js + Baileys
- Backend eksternal: **TIDAK ADA**
- Database: Room/SQLite lokal
- Credential protection: Android Keystore
- License project utama: pilih license open-source yang kompatibel setelah audit dependency; jangan mengunci pilihan sebelum audit.

## Prinsip terpenting
`Local-first, transparent, reusable, auditable, no fake capability.`
