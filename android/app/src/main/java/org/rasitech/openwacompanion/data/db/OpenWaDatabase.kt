package org.rasitech.openwacompanion.data.db

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
                    
                    .build()
                    .also { instance = it }
            }
    }
}
