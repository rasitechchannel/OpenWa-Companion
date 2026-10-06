package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "lid_mapping",
    indices = [Index(value=["accountId","lid"], unique=true)])
data class LidMappingEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val lid: String,
    val jid: String,
    val updatedAt: Long,

)
