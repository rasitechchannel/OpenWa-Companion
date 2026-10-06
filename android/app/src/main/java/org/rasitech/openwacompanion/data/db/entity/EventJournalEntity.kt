package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "event_journal",
    indices = [Index(value=["accountId","seq"]), Index(value=["accountId","timestamp"])])
data class EventJournalEntity(
    @PrimaryKey val eventId: String,
    val accountId: String,
    val seq: Long,
    val eventType: String,
    val rawType: String?,
    val classification: String,
    val schemaVersion: Int,
    val timestamp: Long,
    val payloadJson: String,

)
