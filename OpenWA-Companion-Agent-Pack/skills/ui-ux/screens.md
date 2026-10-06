# UI Acceptance

Setiap screen harus memiliki state:
- loading
- empty
- populated
- error
- offline/reconnecting jika relevan
- capability unavailable jika upstream tidak mendukung

Tidak boleh mengisi release UI dengan fake sample conversations.

## Latest-feature awareness
Saat pack ini dibuat, WhatsApp resmi Android masih aktif menambah:
- multi-account management
- group history sharing
- richer group polls/@all
- call transfer/waiting-room features
- passkey/security context
Agent harus mengecek ulang fitur yang benar-benar relevan untuk companion engine saat implementasi.
