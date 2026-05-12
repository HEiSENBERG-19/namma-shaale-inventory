package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: Category,
    val serialNumber: String?,
    val acquisitionDate: String,
    val purchaseValue: Double?,
    val photoPath: String?,
    val photoUri: String? = null,
    val currentCondition: Condition,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)