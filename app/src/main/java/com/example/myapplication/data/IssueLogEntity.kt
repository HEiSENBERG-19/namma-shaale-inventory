package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "issue_logs",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assetId")]
)
data class IssueLogEntity(
    @PrimaryKey(autoGenerate = true) val issueId: Int = 0,
    val assetId: Int,
    val issueType: IssueType,
    val description: String,
    val photoPath: String?,
    val isResolved: Boolean = false,
    val loggedAt: Long,
    val resolvedAt: Long?
)

