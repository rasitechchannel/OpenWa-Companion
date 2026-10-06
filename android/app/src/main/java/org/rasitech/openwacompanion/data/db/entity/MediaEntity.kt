package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val messageId: String,
    val chatId: String,
    val localPath: String,
    val mimeHint: String?,
    val byteSize: Long,
    val createdAt: Long,

)
