package org.rasitech.openwacompanion.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "groups",
    indices = [Index(value=["accountId","groupId"], unique=true)])
data class GroupEntity(
    @PrimaryKey val rowId: String,
    val accountId: String,
    val groupId: String,
    val subject: String?,
    val description: String?,
    val owner: String?,
    val creation: Long?,
    val restrict: Boolean,
    val announce: Boolean,
    val updatedAt: Long,

)
