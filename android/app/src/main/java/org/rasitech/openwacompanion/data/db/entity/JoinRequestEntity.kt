package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "join_requests")
data class JoinRequestEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val groupId: String,
    val participantJid: String,
    val authorJid: String?,
    val action: String?,
    val method: String?,
    val updatedAt: Long,

)
