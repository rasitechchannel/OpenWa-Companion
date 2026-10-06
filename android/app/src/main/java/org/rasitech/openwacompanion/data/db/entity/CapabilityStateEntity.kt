package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "capability_state",
    indices = [Index(value=["accountId","capabilityKey"], unique=true)])
data class CapabilityStateEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val capabilityKey: String,
    val status: String,
    val notes: String?,
    val updatedAt: Long,

)
