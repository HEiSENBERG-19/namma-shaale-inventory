package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthCheckSessionDao {
    @Query("SELECT * FROM health_check_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<HealthCheckSessionEntity>>

    @Query("SELECT * FROM health_check_sessions ORDER BY startedAt DESC LIMIT 1")
    fun getLastSession(): Flow<HealthCheckSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HealthCheckSessionEntity): Long

    @Update
    suspend fun updateSession(session: HealthCheckSessionEntity)
}

