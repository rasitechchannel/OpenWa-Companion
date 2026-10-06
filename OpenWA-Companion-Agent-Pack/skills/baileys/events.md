# Baileys Event Baseline Snapshot

Snapshot dari `BaileysEventMap` yang terlihat pada source terbaru saat pack dibuat. Agent WAJIB refresh terhadap versi yang benar-benar dipin.

- connection.update
- creds.update
- messaging-history.set
- messaging-history.status
- chats.upsert
- chats.update
- lid-mapping.update
- chats.delete
- presence.update
- contacts.upsert
- contacts.update
- messages.delete
- messages.update
- messages.media-update
- messages.upsert
- messages.reaction
- message-receipt.update
- groups.upsert
- groups.update
- group-participants.update
- group.join-request
- group.member-tag.update
- blocklist.set
- blocklist.update
- call
- labels.edit
- labels.association
- newsletter.reaction
- newsletter.view
- newsletter-participants.update
- newsletter-settings.update
- message-capping.update
- chats.lock
- settings.update

Catatan:
`settings.update` sendiri memiliki beberapa subtype dan harus dipetakan per setting.
