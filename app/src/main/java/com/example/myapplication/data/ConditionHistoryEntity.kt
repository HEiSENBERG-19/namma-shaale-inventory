package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "condition_histories",
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
data class ConditionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Int = 0,
    val assetId: Int,
    val condition: Condition,
    val note: String?,
    val photoPath: String?,
    val recordedBy: String,
    val recordedAt: Long
)

