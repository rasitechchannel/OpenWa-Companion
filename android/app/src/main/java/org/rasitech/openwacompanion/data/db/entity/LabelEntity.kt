package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "labels",
    indices = [Index(value=["accountId","labelId"], unique=true)])
data class LabelEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val labelId: String,
    val name: String?,
    val color: Int?,
    val deleted: Boolean,
    val updatedAt: Long,

)
