package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_profiles")
data class SchoolProfileEntity(
    @PrimaryKey(autoGenerate = true) val profileId: Int = 0,
    val schoolName: String,
    val diseCode: String?,
    val district: String,
    val block: String,
    val createdAt: Long
)

