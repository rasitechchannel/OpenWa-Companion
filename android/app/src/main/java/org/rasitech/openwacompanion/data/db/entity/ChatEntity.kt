package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chats",
    indices = [Index(value=["accountId","chatId"], unique=true), Index(value=["accountId","lastTimestamp"])])
data class ChatEntity(
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

)
