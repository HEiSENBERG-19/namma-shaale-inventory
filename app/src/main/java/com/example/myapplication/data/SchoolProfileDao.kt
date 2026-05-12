package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolProfileDao {
    @Query("SELECT * FROM school_profiles LIMIT 1")
    fun getProfile(): Flow<SchoolProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SchoolProfileEntity)
}

