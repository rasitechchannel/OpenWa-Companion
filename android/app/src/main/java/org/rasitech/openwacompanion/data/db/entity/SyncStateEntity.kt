package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val accountId: String,
    val lastHistoryProgress: Int?,
    val lastSyncAt: Long?,
    val connectionState: String?,
    val notes: String?,

)
