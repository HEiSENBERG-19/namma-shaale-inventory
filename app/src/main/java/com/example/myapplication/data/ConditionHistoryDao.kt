package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConditionHistoryDao {
    @Query("SELECT * FROM condition_histories WHERE assetId = :assetId ORDER BY recordedAt DESC")
    fun getHistoryForAsset(assetId: Int): Flow<List<ConditionHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ConditionHistoryEntity)
}

