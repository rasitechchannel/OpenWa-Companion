package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "label_associations")
data class LabelAssociationEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val labelId: String,
    val chatId: String?,
    val messageId: String?,
    val type: String,
    val updatedAt: Long,

)
