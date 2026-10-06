package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "message_receipts",
    indices = [Index(value=["accountId","messageId"])])
data class ReceiptEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val userJid: String?,
    val receiptType: String?,
    val timestamp: Long,

)
