package org.rasitech.openwacompanion.data.db.dao

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

    @Query("SELECT * FROM chats WHERE accountId = :accountId AND archived = 1 ORDER BY lastTimestamp DESC")
    fun observeArchived(accountId: String): Flow<List<ChatEntity>>

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

    @Query(
        """
        SELECT * FROM messages
        WHERE accountId = :accountId
          AND deleted = 0
          AND text IS NOT NULL
          AND text LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
        LIMIT :limit
        """,
    )
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
