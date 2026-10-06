package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "newsletter_participants")
data class NewsletterParticipantEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val newsletterId: String,
    val userJid: String,
    val role: String?,
    val updatedAt: Long,

)
