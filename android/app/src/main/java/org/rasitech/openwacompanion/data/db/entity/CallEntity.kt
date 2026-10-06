package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "calls",
    indices = [Index(value=["accountId","timestamp"])])
data class CallEntity(
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

)
