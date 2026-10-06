package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reactions",
    indices = [Index(value=["accountId","messageId"])])
data class ReactionEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val reactorJid: String?,
    val text: String?,
    val timestamp: Long,

)
