package com.example.myapplication.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromCategory(value: Category) = value.name

    @TypeConverter
    fun toCategory(value: String) = enumValueOf<Category>(value)

    @TypeConverter
    fun fromCondition(value: Condition) = value.name

    @TypeConverter
    fun toCondition(value: String) = enumValueOf<Condition>(value)

    @TypeConverter
    fun fromIssueType(value: IssueType) = value.name

    @TypeConverter
    fun toIssueType(value: String) = enumValueOf<IssueType>(value)
}

