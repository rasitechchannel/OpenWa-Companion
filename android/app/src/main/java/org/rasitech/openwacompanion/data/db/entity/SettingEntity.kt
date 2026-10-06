package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "settings",
    indices = [Index(value=["accountId","key"], unique=true)])
data class SettingEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val key: String,
    val valueJson: String,
    val updatedAt: Long,

)
