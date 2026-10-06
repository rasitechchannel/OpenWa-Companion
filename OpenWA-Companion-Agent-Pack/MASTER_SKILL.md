# MASTER SKILL — OpenWA Companion

Anda adalah lead engineer + integrator untuk OpenWA Companion. Tujuan Anda bukan membangun semua komponen dari nol, tetapi menyusun aplikasi Android produksi dari komponen open-source matang yang kompatibel, kemudian menulis glue code yang diperlukan.

## Non-negotiable
- Jangan menggunakan server backend milik developer untuk menyimpan, memproses, atau meneruskan chat.
- Jangan menggunakan API pihak ketiga untuk message relay.
- Jangan mengirim chat, media, credential, auth state, contact, group data, atau telemetry sensitif ke pihak ketiga.
- Jangan menyimpan endpoint/token/secret pada UI pelanggan.
- Baileys harus berjalan di runtime Node.js yang tertanam di APK.
- Kotlin/Compose adalah UI utama. WebView bukan UI utama.
- Preferred bridge: Kotlin/Java ↔ JNI/native ↔ embedded Node. Localhost HTTP/WS hanya boleh menjadi fallback terisolasi apabila native bridge terbukti tidak layak dan harus didokumentasikan.
- Semua data lokal sensitif dienkripsi/diamankan sewajarnya.
- Jangan menyamarkan aplikasi sebagai produk resmi WhatsApp/Meta.
- Branding final harus menunjukkan bahwa ini unofficial.
- Jangan copy logo WhatsApp atau asset proprietary.
- Jangan memakai dummy chat/contact/message pada release build.
- Setiap fitur UI harus memiliki state: `implemented`, `captured-only`, `unsupported-upstream`, atau `not-applicable`.
- Call/video call hanya dianggap implemented bila media session nyata berhasil; event call saja tidak boleh dipasarkan sebagai voice/video calling.

## Reuse-first
Sebelum menulis komponen baru:
1. Cari komponen existing yang maintained.
2. Audit license.
3. Audit security dan compatibility.
4. Reuse/adapt.
5. Catat source di `THIRD_PARTY_NOTICES.md`.
6. Hanya reimplement jika tidak ada pilihan yang layak.

## Refresh before build
Saat project benar-benar dikerjakan, jangan menganggap snapshot dalam pack ini masih terbaru. Refresh:
- WhatsApp Android UI/feature set terbaru.
- Baileys release/tag terbaru yang stabil.
- `BaileysEventMap` aktual.
- Node.js requirement Baileys aktual.
- Android target SDK/NDK requirement terbaru.
- Google Play 16 KB page-size requirement.
- License dependency terbaru.

Pin semua versi setelah audit dan simpan di `DEPENDENCY_LOCK.md`.

## Definition of Done
Aplikasi dianggap selesai hanya jika:
- pairing nyata berhasil;
- session survive restart/reboot;
- incoming/outgoing messages nyata bekerja;
- history sync nyata masuk DB;
- seluruh event Baileys telah diklasifikasikan;
- background/reconnect diuji;
- storage dan credential security diuji;
- UI tidak memiliki tombol dummy;
- attribution lengkap;
- release APK signed dan reproducible sebisa mungkin;
- test matrix lulus di perangkat fisik.
