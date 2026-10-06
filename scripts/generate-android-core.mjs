/**
 * Generates Room entities/DAOs and core Kotlin architecture files for OpenWA.
 */
import fs from "fs";
import path from "path";

const root = "E:/OpenWA/android/app/src/main/java/org/rasitech/openwacompanion";

function write(rel, content) {
  const full = path.join(root, rel);
  fs.mkdirSync(path.dirname(full), { recursive: true });
  fs.writeFileSync(full, content.replace(/\n/g, "\r\n"));
  console.log("wrote", rel);
}

// --- Domain models ---
write(
  "domain/model/Models.kt",
  `package org.rasitech.openwacompanion.domain.model

enum class CapabilityStatus {
    IMPLEMENTED_UI,
    IMPLEMENTED_BACKGROUND,
    CAPTURED_ONLY,
    UNSUPPORTED_UPSTREAM_ACTION,
    NOT_APPLICABLE,
    PENDING_PHYSICAL_DEVICE_TEST,
    DEPRECATED,
}

data class Account(
    val accountId: String,
    val displayName: String?,
    val jid: String?,
    val isActive: Boolean,
    val createdAt: Long,
)

data class ChatSummary(
    val accountId: String,
    val chatId: String,
    val title: String,
    val lastMessagePreview: String?,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val pinned: Boolean,
    val archived: Boolean,
    val mutedUntil: Long?,
    val isGroup: Boolean,
)

data class MessageItem(
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val fromMe: Boolean,
    val senderJid: String?,
    val contentType: String,
    val text: String?,
    val timestamp: Long,
    val status: Int?,
    val mediaPath: String?,
    val quotedId: String?,
)

data class CallItem(
    val accountId: String,
    val callId: String,
    val chatId: String?,
    val fromJid: String?,
    val isVideo: Boolean,
    val status: String,
    val timestamp: Long,
    val liveMediaSupported: Boolean = false,
)
`,
);

write(
  "domain/event/DomainEvent.kt",
  `package org.rasitech.openwacompanion.domain.event

import org.rasitech.openwacompanion.domain.model.CapabilityStatus

data class DomainEvent(
    val eventId: String,
    val seq: Long,
    val accountId: String,
    val eventType: String,
    val rawType: String?,
    val classification: CapabilityStatus,
    val schemaVersion: Int,
    val timestamp: Long,
    val payloadJson: String,
)
`,
);

// --- Room entities ---
const entities = [
  [
    "AccountEntity",
    `accounts`,
    `
    @PrimaryKey val accountId: String,
    val displayName: String?,
    val jid: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
`,
  ],
  [
    "ContactEntity",
    `contacts`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val contactId: String,
    val name: String?,
    val notify: String?,
    val verifiedName: String?,
    val lid: String?,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","contactId"], unique=true)]',
  ],
  [
    "ChatEntity",
    `chats`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val chatId: String,
    val title: String?,
    val lastMessagePreview: String?,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val pinned: Long,
    val archived: Boolean,
    val mutedUntil: Long?,
    val isGroup: Boolean,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","chatId"], unique=true), Index(value=["accountId","lastTimestamp"])]',
  ],
  [
    "MessageEntity",
    `messages`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val fromMe: Boolean,
    val senderJid: String?,
    val contentType: String,
    val text: String?,
    val timestamp: Long,
    val status: Int?,
    val mediaPath: String?,
    val quotedId: String?,
    val rawJson: String?,
    val deleted: Boolean,
`,
    'indices = [Index(value=["accountId","messageId","chatId"], unique=true), Index(value=["accountId","chatId","timestamp"])]',
  ],
  [
    "ReceiptEntity",
    `message_receipts`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val userJid: String?,
    val receiptType: String?,
    val timestamp: Long,
`,
    'indices = [Index(value=["accountId","messageId"])]',
  ],
  [
    "ReactionEntity",
    `reactions`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val reactorJid: String?,
    val text: String?,
    val timestamp: Long,
`,
    'indices = [Index(value=["accountId","messageId"])]',
  ],
  [
    "MediaEntity",
    `media`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val localPath: String,
    val mimeHint: String?,
    val byteSize: Long,
    val createdAt: Long,
`,
  ],
  [
    "GroupEntity",
    `groups`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val groupId: String,
    val subject: String?,
    val description: String?,
    val owner: String?,
    val creation: Long?,
    val restrict: Boolean,
    val announce: Boolean,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","groupId"], unique=true)]',
  ],
  [
    "GroupParticipantEntity",
    `group_participants`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val groupId: String,
    val participantJid: String,
    val role: String?,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","groupId","participantJid"], unique=true)]',
  ],
  [
    "JoinRequestEntity",
    `join_requests`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val groupId: String,
    val participantJid: String,
    val authorJid: String?,
    val action: String?,
    val method: String?,
    val updatedAt: Long,
`,
  ],
  [
    "LabelEntity",
    `labels`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val labelId: String,
    val name: String?,
    val color: Int?,
    val deleted: Boolean,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","labelId"], unique=true)]',
  ],
  [
    "LabelAssociationEntity",
    `label_associations`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val labelId: String,
    val chatId: String?,
    val messageId: String?,
    val type: String,
    val updatedAt: Long,
`,
  ],
  [
    "NewsletterEntity",
    `newsletters`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val newsletterId: String,
    val name: String?,
    val description: String?,
    val subscribers: Long?,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","newsletterId"], unique=true)]',
  ],
  [
    "NewsletterParticipantEntity",
    `newsletter_participants`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val newsletterId: String,
    val userJid: String,
    val role: String?,
    val updatedAt: Long,
`,
  ],
  [
    "CallEntity",
    `calls`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val callId: String,
    val chatId: String?,
    val fromJid: String?,
    val isVideo: Boolean,
    val isGroup: Boolean,
    val status: String,
    val offline: Boolean,
    val timestamp: Long,
    val liveMediaSupported: Boolean,
`,
    'indices = [Index(value=["accountId","timestamp"])]',
  ],
  [
    "SettingEntity",
    `settings`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val key: String,
    val valueJson: String,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","key"], unique=true)]',
  ],
  [
    "SyncStateEntity",
    `sync_state`,
    `
    @PrimaryKey val accountId: String,
    val lastHistoryProgress: Int?,
    val lastSyncAt: Long?,
    val connectionState: String?,
    val notes: String?,
`,
  ],
  [
    "LidMappingEntity",
    `lid_mapping`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val lid: String,
    val jid: String,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","lid"], unique=true)]',
  ],
  [
    "EventJournalEntity",
    `event_journal`,
    `
    @PrimaryKey val eventId: String,
    val accountId: String,
    val seq: Long,
    val eventType: String,
    val rawType: String?,
    val classification: String,
    val schemaVersion: Int,
    val timestamp: Long,
    val payloadJson: String,
`,
    'indices = [Index(value=["accountId","seq"]), Index(value=["accountId","timestamp"])]',
  ],
  [
    "CapabilityStateEntity",
    `capability_state`,
    `
    @PrimaryKey val rowId: String,
    val accountId: String,
    val capabilityKey: String,
    val status: String,
    val notes: String?,
    val updatedAt: Long,
`,
    'indices = [Index(value=["accountId","capabilityKey"], unique=true)]',
  ],
];

for (const ent of entities) {
  const [name, table, fields, indices] = ent;
  const indexArg = indices ? `,\n    indices = ${indices.replace(/^indices = /, "")}` : "";
  write(
    `data/db/entity/${name}.kt`,
    `package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "${table}"${indexArg})
data class ${name}(${fields}
)
`,
  );
}

write(
  "data/db/entity/MessageFtsEntity.kt",
  `package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = MessageEntity::class)
@Entity(tableName = "messages_fts")
data class MessageFtsEntity(
    val text: String?,
    val chatId: String,
    val accountId: String,
)
`,
);

// DAOs - consolidated
write(
  "data/db/dao/OpenWaDaos.kt",
  `package org.rasitech.openwacompanion.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.rasitech.openwacompanion.data.db.entity.*

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AccountEntity)

    @Query("UPDATE accounts SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE accounts SET isActive = 1 WHERE accountId = :accountId")
    suspend fun setActive(accountId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE accountId = :accountId AND archived = 0 ORDER BY pinned DESC, lastTimestamp DESC")
    fun observeChats(accountId: String): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE accountId = :accountId AND chatId = :chatId LIMIT 1")
    suspend fun get(accountId: String, chatId: String): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ChatEntity>)

    @Query("DELETE FROM chats WHERE accountId = :accountId AND chatId IN (:ids)")
    suspend fun delete(accountId: String, ids: List<String>)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE accountId = :accountId AND chatId = :chatId AND deleted = 0 ORDER BY timestamp ASC")
    fun observeMessages(accountId: String, chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MessageEntity>)

    @Query("UPDATE messages SET deleted = 1 WHERE accountId = :accountId AND messageId IN (:ids)")
    suspend fun markDeleted(accountId: String, ids: List<String>)

    @Query("""
        SELECT m.* FROM messages m
        JOIN messages_fts fts ON m.rowid = fts.rowid
        WHERE messages_fts MATCH :query AND m.accountId = :accountId
        ORDER BY m.timestamp DESC LIMIT :limit
    """)
    suspend fun search(accountId: String, query: String, limit: Int = 50): List<MessageEntity>
}

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ContactEntity>)

    @Query("SELECT * FROM contacts WHERE accountId = :accountId ORDER BY name ASC")
    fun observe(accountId: String): Flow<List<ContactEntity>>
}

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ReceiptEntity>)

    @Query("SELECT * FROM message_receipts WHERE accountId = :accountId AND messageId = :messageId")
    fun observe(accountId: String, messageId: String): Flow<List<ReceiptEntity>>
}

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ReactionEntity>)

    @Query("SELECT * FROM reactions WHERE accountId = :accountId AND messageId = :messageId")
    fun observe(accountId: String, messageId: String): Flow<List<ReactionEntity>>
}

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MediaEntity)
}

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GroupEntity>)

    @Query("SELECT * FROM groups WHERE accountId = :accountId ORDER BY subject ASC")
    fun observe(accountId: String): Flow<List<GroupEntity>>
}

@Dao
interface GroupParticipantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<GroupParticipantEntity>)

    @Query("SELECT * FROM group_participants WHERE accountId = :accountId AND groupId = :groupId")
    fun observe(accountId: String, groupId: String): Flow<List<GroupParticipantEntity>>
}

@Dao
interface JoinRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: JoinRequestEntity)

    @Query("SELECT * FROM join_requests WHERE accountId = :accountId AND groupId = :groupId")
    fun observe(accountId: String, groupId: String): Flow<List<JoinRequestEntity>>
}

@Dao
interface LabelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LabelEntity)

    @Query("SELECT * FROM labels WHERE accountId = :accountId AND deleted = 0")
    fun observe(accountId: String): Flow<List<LabelEntity>>
}

@Dao
interface LabelAssociationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LabelAssociationEntity)
}

@Dao
interface NewsletterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NewsletterEntity)

    @Query("SELECT * FROM newsletters WHERE accountId = :accountId ORDER BY name ASC")
    fun observe(accountId: String): Flow<List<NewsletterEntity>>
}

@Dao
interface NewsletterParticipantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NewsletterParticipantEntity)
}

@Dao
interface CallDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CallEntity>)

    @Query("SELECT * FROM calls WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun observe(accountId: String): Flow<List<CallEntity>>
}

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SettingEntity)

    @Query("SELECT * FROM settings WHERE accountId = :accountId")
    fun observe(accountId: String): Flow<List<SettingEntity>>

    @Query("SELECT * FROM settings WHERE accountId = :accountId AND key = :key LIMIT 1")
    suspend fun get(accountId: String, key: String): SettingEntity?
}

@Dao
interface SyncStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SyncStateEntity)

    @Query("SELECT * FROM sync_state WHERE accountId = :accountId LIMIT 1")
    fun observe(accountId: String): Flow<SyncStateEntity?>
}

@Dao
interface LidMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LidMappingEntity)

    @Query("SELECT * FROM lid_mapping WHERE accountId = :accountId")
    fun observe(accountId: String): Flow<List<LidMappingEntity>>
}

@Dao
interface EventJournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EventJournalEntity)

    @Query("SELECT * FROM event_journal WHERE accountId = :accountId ORDER BY seq DESC LIMIT :limit")
    fun observeRecent(accountId: String, limit: Int = 100): Flow<List<EventJournalEntity>>

    @Query("SELECT MAX(seq) FROM event_journal WHERE accountId = :accountId")
    suspend fun maxSeq(accountId: String): Long?
}

@Dao
interface CapabilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CapabilityStateEntity)

    @Query("SELECT * FROM capability_state WHERE accountId = :accountId ORDER BY capabilityKey ASC")
    fun observe(accountId: String): Flow<List<CapabilityStateEntity>>
}
`,
);

write(
  "data/db/OpenWaDatabase.kt",
  `package org.rasitech.openwacompanion.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.rasitech.openwacompanion.data.db.dao.*
import org.rasitech.openwacompanion.data.db.entity.*

@Database(
    entities = [
        AccountEntity::class,
        ContactEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        MessageFtsEntity::class,
        ReceiptEntity::class,
        ReactionEntity::class,
        MediaEntity::class,
        GroupEntity::class,
        GroupParticipantEntity::class,
        JoinRequestEntity::class,
        LabelEntity::class,
        LabelAssociationEntity::class,
        NewsletterEntity::class,
        NewsletterParticipantEntity::class,
        CallEntity::class,
        SettingEntity::class,
        SyncStateEntity::class,
        LidMappingEntity::class,
        EventJournalEntity::class,
        CapabilityStateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class OpenWaDatabase : RoomDatabase() {
    abstract fun accounts(): AccountDao
    abstract fun chats(): ChatDao
    abstract fun messages(): MessageDao
    abstract fun contacts(): ContactDao
    abstract fun receipts(): ReceiptDao
    abstract fun reactions(): ReactionDao
    abstract fun media(): MediaDao
    abstract fun groups(): GroupDao
    abstract fun groupParticipants(): GroupParticipantDao
    abstract fun joinRequests(): JoinRequestDao
    abstract fun labels(): LabelDao
    abstract fun labelAssociations(): LabelAssociationDao
    abstract fun newsletters(): NewsletterDao
    abstract fun newsletterParticipants(): NewsletterParticipantDao
    abstract fun calls(): CallDao
    abstract fun settings(): SettingDao
    abstract fun syncState(): SyncStateDao
    abstract fun lidMapping(): LidMappingDao
    abstract fun eventJournal(): EventJournalDao
    abstract fun capabilities(): CapabilityDao

    companion object {
        @Volatile private var instance: OpenWaDatabase? = null

        // Placeholder for future non-destructive migrations.
        val MIGRATION_PLACEHOLDER = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // no-op template
            }
        }

        fun get(context: Context): OpenWaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OpenWaDatabase::class.java,
                    "openwa.db",
                )
                    .fallbackToDestructiveMigrationOnDowngrade(false)
                    .build()
                    .also { instance = it }
            }
    }
}
`,
);

console.log("core db generated");
