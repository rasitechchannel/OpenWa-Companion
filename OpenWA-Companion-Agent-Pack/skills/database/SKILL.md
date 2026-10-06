# Skill: Local Database

Use Room/SQLite as normalized app state.

## Suggested entities
- accounts
- contacts
- chats
- messages
- message_receipts
- reactions
- media
- groups
- group_participants
- join_requests
- labels
- label_associations
- newsletters
- newsletter_participants
- calls
- sync_state
- capability_state
- event_journal (redacted)
- settings

## Rules
- index jid/account/timestamp/messageId
- FTS for local message search
- migrations mandatory
- no destructive migration in production
- transaction history-sync batches
- dedupe by stable message key
- raw proto storage only when justified and versioned

## Retention
Provide settings for media cache and event diagnostics retention.
Message/history retention follows data received from linked-device sync; do not invent missing history.
