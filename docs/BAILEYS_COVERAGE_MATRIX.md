# Baileys Coverage Matrix

Pinned: `@whiskeysockets/baileys@6.7.24`  
Audited: 2026-10-06 against `lib/Types/Events.d.ts`, `lib/Socket/index.d.ts`, WAProto `IMessage` keys.

Status values: `IMPLEMENTED_UI` | `IMPLEMENTED_BACKGROUND` | `CAPTURED_ONLY` | `UNSUPPORTED_UPSTREAM_ACTION` | `NOT_APPLICABLE` | `PENDING_PHYSICAL_DEVICE_TEST` | `DEPRECATED`

No unclassified rows. No silent drops.

## Events (`BaileysEventMap`)

| Event/Capability | Adapter | DB/State | UI | Tests | Status | Notes |
|---|---:|---:|---:|---:|---|---|
| `connection.update` | ☑ | ☑ | ☑ | ☑ | PENDING_PHYSICAL_DEVICE_TEST | Device: qr/connecting/close/reconnect + pairing-code proven; `open` awaits primary phone link |
| `creds.update` | ☑ | ☑ | ☐ | ☑ | IMPLEMENTED_BACKGROUND | Persist via multi-file auth; Keystore wraps session dir |
| `messaging-history.set` | ☑ | ☑ | ☐ | ☑ | IMPLEMENTED_BACKGROUND | Transactional Room ingest; UI sync progress |
| `messaging-history.status` | ☐ | ☐ | ☐ | ☐ | NOT_APPLICABLE | Absent from BaileysEventMap in @whiskeysockets/baileys@6.7.24 |
| `chats.upsert` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI | Chats list |
| `chats.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI | Mute/pin/archive/unread |
| `chats.phoneNumberShare` | ☑ | ☑ | ☐ | ☑ | IMPLEMENTED_BACKGROUND | Maps into lid_mapping |
| `lid-mapping.update` | ☐ | ☐ | ☐ | ☐ | NOT_APPLICABLE | Absent in 6.7.24 EventMap; covered via chats.phoneNumberShare + onWhatsApp |
| `chats.delete` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `presence.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI | Conversation presence indicator |
| `contacts.upsert` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `contacts.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `messages.delete` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `messages.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `messages.media-update` | ☑ | ☑ | ☐ | ☑ | IMPLEMENTED_BACKGROUND | File-path media pipeline |
| `messages.upsert` | ☑ | ☑ | ☑ | ☑ | PENDING_PHYSICAL_DEVICE_TEST | UI wired; real send/receive needs device |
| `messages.reaction` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `message-receipt.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI | Ticks / message info |
| `groups.upsert` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `groups.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `group-participants.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `group.join-request` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `group.member-tag.update` | ☐ | ☐ | ☐ | ☐ | NOT_APPLICABLE | Absent from EventMap in 6.7.24 |
| `blocklist.set` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI | Privacy/block list |
| `blocklist.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `call` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI | Metadata/history only; live media UNSUPPORTED_UPSTREAM_ACTION |
| `labels.edit` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `labels.association` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `newsletter.reaction` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `newsletter.view` | ☑ | ☑ | Diag | ☑ | CAPTURED_ONLY | Diagnostics until dedicated analytics UI |
| `newsletter-participants.update` | ☑ | ☑ | ☑ | ☑ | IMPLEMENTED_UI |  |
| `newsletter-settings.update` | ☑ | ☑ | Diag | ☑ | CAPTURED_ONLY |  |
| `message-capping.update` | ☐ | ☐ | ☐ | ☐ | NOT_APPLICABLE | Absent from EventMap in 6.7.24 |
| `chats.lock` | ☐ | ☐ | ☐ | ☐ | NOT_APPLICABLE | Absent from EventMap in 6.7.24 |
| `settings.update` | ☐ | ☐ | ☐ | ☐ | NOT_APPLICABLE | Absent from EventMap; privacy via fetchPrivacySettings + update*Privacy |
| `* (unknown/new)` | ☑ | ☑ | Diag | ☑ | CAPTURED_ONLY | event_journal catch-all; never drop silently |

## Socket methods (134)

| Method | Status | Notes |
|---|---|---|
| `addChatLabel` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `addLabel` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `addMessageLabel` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `addOrEditContact` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `appPatch` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `assertSessions` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `chatModify` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `cleanDirtyBits` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `communityAcceptInvite` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityAcceptInviteV4` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityCreate` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityFetchAllParticipating` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityGetInviteInfo` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityInviteCode` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityJoinApprovalMode` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityLeave` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityMemberAddMode` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityMetadata` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityParticipantsUpdate` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityRequestParticipantsList` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityRequestParticipantsUpdate` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityRevokeInvite` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityRevokeInviteV4` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communitySettingUpdate` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityToggleEphemeral` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityUpdateDescription` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `communityUpdateSubject` | CAPTURED_ONLY | Communities tab reflects participating metadata; admin actions gated |
| `createParticipantNodes` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `end` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `executeUSyncQuery` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `fetchBlocklist` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `fetchDisappearingDuration` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `fetchMessageHistory` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `fetchPrivacySettings` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `fetchStatus` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `generateMessageTag` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `getBotListV2` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `getBusinessProfile` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `getCatalog` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `getCollections` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `getOrderDetails` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `getPrivacyTokens` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `getUSyncDevices` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `groupAcceptInvite` | IMPLEMENTED_UI | Group info/admin |
| `groupAcceptInviteV4` | IMPLEMENTED_UI | Group info/admin |
| `groupCreate` | IMPLEMENTED_UI | Group info/admin |
| `groupFetchAllParticipating` | IMPLEMENTED_UI | Group info/admin |
| `groupGetInviteInfo` | IMPLEMENTED_UI | Group info/admin |
| `groupInviteCode` | IMPLEMENTED_UI | Group info/admin |
| `groupJoinApprovalMode` | IMPLEMENTED_UI | Group info/admin |
| `groupLeave` | IMPLEMENTED_UI | Group info/admin |
| `groupMemberAddMode` | IMPLEMENTED_UI | Group info/admin |
| `groupMetadata` | IMPLEMENTED_UI | Group info/admin |
| `groupParticipantsUpdate` | IMPLEMENTED_UI | Group info/admin |
| `groupRequestParticipantsList` | IMPLEMENTED_UI | Group info/admin |
| `groupRequestParticipantsUpdate` | IMPLEMENTED_UI | Group info/admin |
| `groupRevokeInvite` | IMPLEMENTED_UI | Group info/admin |
| `groupRevokeInviteV4` | IMPLEMENTED_UI | Group info/admin |
| `groupSettingUpdate` | IMPLEMENTED_UI | Group info/admin |
| `groupToggleEphemeral` | IMPLEMENTED_UI | Group info/admin |
| `groupUpdateDescription` | IMPLEMENTED_UI | Group info/admin |
| `groupUpdateSubject` | IMPLEMENTED_UI | Group info/admin |
| `logout` | PENDING_PHYSICAL_DEVICE_TEST | Onboarding/session screens wired |
| `newsletterAdminCount` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterChangeOwner` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterCreate` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterDelete` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterDemote` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterFetchMessages` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterFollow` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterMetadata` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterMute` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterReactMessage` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterRemovePicture` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterSubscribers` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterUnfollow` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterUnmute` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterUpdate` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterUpdateDescription` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterUpdateName` | IMPLEMENTED_UI | Channels/Updates |
| `newsletterUpdatePicture` | IMPLEMENTED_UI | Channels/Updates |
| `onUnexpectedError` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `onWhatsApp` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `presenceSubscribe` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `productCreate` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `productDelete` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `productUpdate` | CAPTURED_ONLY | Business/catalog; no dedicated consumer UI yet |
| `profilePictureUrl` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `query` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `readMessages` | PENDING_PHYSICAL_DEVICE_TEST | Wired; real WA verification pending |
| `refreshMediaConn` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `rejectCall` | IMPLEMENTED_BACKGROUND | Reject signaling only; no media session |
| `relayMessage` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `removeChatLabel` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `removeContact` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `removeMessageLabel` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `removeProfilePicture` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `requestPairingCode` | PENDING_PHYSICAL_DEVICE_TEST | Device issued code + status persistence verified; primary WhatsApp entry pending |
| `requestPlaceholderResend` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `resyncAppState` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `sendMessage` | PENDING_PHYSICAL_DEVICE_TEST | Wired; real WA verification pending |
| `sendMessageAck` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `sendNode` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `sendPeerDataOperationMessage` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `sendPresenceUpdate` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `sendRawMessage` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `sendReceipt` | PENDING_PHYSICAL_DEVICE_TEST | Wired; real WA verification pending |
| `sendReceipts` | PENDING_PHYSICAL_DEVICE_TEST | Wired; real WA verification pending |
| `sendRetryRequest` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `sendWAMBuffer` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `star` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `subscribeNewsletterUpdates` | IMPLEMENTED_UI | Channels/Updates |
| `updateBlockStatus` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateCallPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateDefaultDisappearingMode` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateDisableLinkPreviewsPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateGroupsAddPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateLastSeenPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateMediaMessage` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `updateMessagesPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateOnlinePrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateProfileName` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateProfilePicture` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateProfilePicturePrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateProfileStatus` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateReadReceiptsPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `updateStatusPrivacy` | IMPLEMENTED_BACKGROUND | Exposed via command bus when UI/action requires |
| `uploadPreKeys` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `uploadPreKeysToServerIfRequired` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `upsertMessage` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `waitForConnectionUpdate` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `waitForMessage` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `waitForSocketOpen` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |
| `waUploadToServer` | IMPLEMENTED_BACKGROUND | Engine-internal / sync plumbing |

## Message / proto content types (86)

| Type | Status | Notes |
|---|---|---|
| `conversation` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `senderKeyDistributionMessage` | IMPLEMENTED_BACKGROUND | Protocol/control |
| `imageMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `contactMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `locationMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `extendedTextMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `documentMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `audioMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `videoMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `call` | IMPLEMENTED_BACKGROUND | Protocol/control; live A/V = UNSUPPORTED_UPSTREAM_ACTION without CallProvider media |
| `chat` | IMPLEMENTED_BACKGROUND | Protocol/control |
| `protocolMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `contactsArrayMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `highlyStructuredMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `fastRatchetKeySenderKeyDistributionMessage` | IMPLEMENTED_BACKGROUND | Protocol/control |
| `sendPaymentMessage` | CAPTURED_ONLY | Generic capture path |
| `liveLocationMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `requestPaymentMessage` | CAPTURED_ONLY | Generic capture path |
| `declinePaymentRequestMessage` | CAPTURED_ONLY | Generic capture path |
| `cancelPaymentRequestMessage` | CAPTURED_ONLY | Generic capture path |
| `templateMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `stickerMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `groupInviteMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `templateButtonReplyMessage` | CAPTURED_ONLY | Generic capture path |
| `productMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `deviceSentMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `messageContextInfo` | IMPLEMENTED_BACKGROUND | Protocol/control |
| `listMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `viewOnceMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `orderMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `listResponseMessage` | CAPTURED_ONLY | Generic capture path |
| `ephemeralMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `invoiceMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `buttonsMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `buttonsResponseMessage` | CAPTURED_ONLY | Generic capture path |
| `paymentInviteMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `interactiveMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `reactionMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `stickerSyncRmrMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `interactiveResponseMessage` | CAPTURED_ONLY | Generic capture path |
| `pollCreationMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `pollUpdateMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `keepInChatMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `documentWithCaptionMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `requestPhoneNumberMessage` | CAPTURED_ONLY | Generic capture path |
| `viewOnceMessageV2` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `encReactionMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `editedMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `viewOnceMessageV2Extension` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `pollCreationMessageV2` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `scheduledCallCreationMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `groupMentionedMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `pinInChatMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `pollCreationMessageV3` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `scheduledCallEditMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `ptvMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `botInvokeMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `callLogMesssage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `messageHistoryBundle` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `encCommentMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `bcallMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `lottieStickerMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `eventMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `encEventResponseMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `commentMessage` | CAPTURED_ONLY | Generic capture path |
| `newsletterAdminInviteMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `placeholderMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `secretEncryptedMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `albumMessage` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `eventCoverImage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `stickerPackMessage` | CAPTURED_ONLY | Generic capture path |
| `statusMentionMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `pollResultSnapshotMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `pollCreationOptionImageMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `associatedChildMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `groupStatusMentionMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `pollCreationMessageV4` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `pollCreationMessageV5` | IMPLEMENTED_UI | Normalized + conversation renderer |
| `statusAddYours` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `groupStatusMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `richResponseMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `statusNotificationMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `limitSharingMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `botTaskMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `questionMessage` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |
| `messageHistoryNotice` | CAPTURED_ONLY | Stored in messages.content_type + journal; specialized UI later |

## Cross-cutting capabilities

| Capability | Status | Notes |
|---|---|---|
| QR pairing | PENDING_PHYSICAL_DEVICE_TEST | Live QR on Xiaomi arm64; `connection=open` blocked (DUT WA is linked companion, needs primary) |
| Pairing code | PENDING_PHYSICAL_DEVICE_TEST | Issued on device (e.g. HL92FZ6S); status merge bug fixed; primary entry pending |
| Session persistence | PENDING_PHYSICAL_DEVICE_TEST | Unpaired process recovery DONE; paired persistence needs open session |
| Multi-account isolation | IMPLEMENTED_BACKGROUND | Separate accountId auth/DB ownership |
| History sync | IMPLEMENTED_BACKGROUND | PENDING_PHYSICAL_DEVICE_TEST for volume |
| Media download/upload path | IMPLEMENTED_BACKGROUND | App-private files; no giant Base64 bridge |
| FTS message search | IMPLEMENTED_UI | Room FTS4 |
| App lock / biometric | IMPLEMENTED_UI | AndroidX Biometric + Keystore gate |
| Keystore credential protection | IMPLEMENTED_BACKGROUND | EncryptedFile / MasterKey |
| Foreground sync service | IMPLEMENTED_BACKGROUND | dataSync FGS |
| Live voice/video media | UNSUPPORTED_UPSTREAM_ACTION | CallProvider stub; event metadata only |
| Call reject signaling | IMPLEMENTED_BACKGROUND | rejectCall |
| LID mapping | IMPLEMENTED_BACKGROUND | chats.phoneNumberShare + onWhatsApp lid field |
| Privacy settings read/write | IMPLEMENTED_UI | fetchPrivacySettings + update*Privacy |
| Status/Updates broadcast | CAPTURED_ONLY | Status types captured; full composer gated on upstream |
| Newsletters/Channels | IMPLEMENTED_UI | Socket newsletter* + events |
| Communities | CAPTURED_ONLY | community* methods; UI shows available metadata |

## Classification counts (this matrix)

- **CAPTURED_ONLY**: 79
- **IMPLEMENTED_BACKGROUND**: 79
- **IMPLEMENTED_UI**: 97
- **NOT_APPLICABLE**: 6
- **PENDING_PHYSICAL_DEVICE_TEST**: 11
- **UNSUPPORTED_UPSTREAM_ACTION**: 1

TOTAL_CLASSIFIED_ROWS: 273
TODO_REMAINING: 0
