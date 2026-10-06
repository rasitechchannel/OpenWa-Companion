package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "messages",
    indices = [Index(value=["accountId","messageId","chatId"], unique=true), Index(value=["accountId","chatId","timestamp"])])
data class MessageEntity(
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

)
