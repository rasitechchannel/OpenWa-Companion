package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "group_participants",
    indices = [Index(value=["accountId","groupId","participantJid"], unique=true)])
data class GroupParticipantEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val groupId: String,
    val participantJid: String,
    val role: String?,
    val updatedAt: Long,

)
