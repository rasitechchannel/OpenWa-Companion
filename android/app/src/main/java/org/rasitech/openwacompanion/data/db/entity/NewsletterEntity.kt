package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "newsletters",
    indices = [Index(value=["accountId","newsletterId"], unique=true)])
data class NewsletterEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val newsletterId: String,
    val name: String?,
    val description: String?,
    val subscribers: Long?,
    val updatedAt: Long,

)
