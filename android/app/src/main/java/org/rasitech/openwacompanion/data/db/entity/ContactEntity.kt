package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "contacts",
    indices = [Index(value=["accountId","contactId"], unique=true)])
data class ContactEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val contactId: String,
    val name: String?,
    val notify: String?,
    val verifiedName: String?,
    val lid: String?,
    val updatedAt: Long,

)
