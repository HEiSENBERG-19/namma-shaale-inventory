package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_check_sessions")
data class HealthCheckSessionEntity(
    @PrimaryKey(autoGenerate = true) val sessionId: Int = 0,
    val conductedBy: String,
    val itemsReviewed: Int,
    val totalItems: Int,
    val startedAt: Long,
    val completedAt: Long?
)

