package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val accountId: String,
    val displayName: String?,
    val jid: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,

)
