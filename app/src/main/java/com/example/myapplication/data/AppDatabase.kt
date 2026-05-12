package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AssetEntity::class,
        ConditionHistoryEntity::class,
        IssueLogEntity::class,
        HealthCheckSessionEntity::class,
        SchoolProfileEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun conditionHistoryDao(): ConditionHistoryDao
    abstract fun issueLogDao(): IssueLogDao
    abstract fun healthCheckSessionDao(): HealthCheckSessionDao
    abstract fun schoolProfileDao(): SchoolProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create temporary table for assets
                database.execSQL("""
                    CREATE TABLE assets_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        serialNumber TEXT,
                        acquisitionDate TEXT NOT NULL,
                        purchaseValue REAL,
                        photoPath TEXT,
                        currentCondition TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Copy data from old to new. Map conditions: Working->GREEN, Needs Repair->YELLOW, Broken->RED
                database.execSQL("""
                    INSERT INTO assets_new (
                        id, name, category, serialNumber, acquisitionDate,
                        purchaseValue, photoPath, currentCondition, isActive,
                        createdAt, updatedAt
                    )
                    SELECT 
                        id, name, 'OTHER', serialNumber, '',
                        NULL, photoUri,
                        CASE condition
                            WHEN 'Working' THEN 'GREEN'
                            WHEN 'Needs Repair' THEN 'YELLOW'
                            WHEN 'Broken' THEN 'RED'
                            ELSE 'GREEN'
                        END, 1, lastChecked, lastChecked
                    FROM assets
                """.trimIndent())

                database.execSQL("DROP TABLE assets")
                database.execSQL("ALTER TABLE assets_new RENAME TO assets")

                // Create other tables
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS condition_histories (
                        historyId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        assetId INTEGER NOT NULL,
                        condition TEXT NOT NULL,
                        note TEXT,
                        photoPath TEXT,
                        recordedBy TEXT NOT NULL,
                        recordedAt INTEGER NOT NULL,
                        FOREIGN KEY(assetId) REFERENCES assets(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_condition_histories_assetId ON condition_histories(assetId)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS issue_logs (
                        issueId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        assetId INTEGER NOT NULL,
                        issueType TEXT NOT NULL,
                        description TEXT NOT NULL,
                        photoPath TEXT,
                        isResolved INTEGER NOT NULL,
                        loggedAt INTEGER NOT NULL,
                        resolvedAt INTEGER
                        , FOREIGN KEY(assetId) REFERENCES assets(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_issue_logs_assetId ON issue_logs(assetId)")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS health_check_sessions (
                        sessionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        conductedBy TEXT NOT NULL,
                        itemsReviewed INTEGER NOT NULL,
                        totalItems INTEGER NOT NULL,
                        startedAt INTEGER NOT NULL,
                        completedAt INTEGER
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS school_profiles (
                        profileId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        schoolName TEXT NOT NULL,
                        diseCode TEXT,
                        district TEXT NOT NULL,
                        block TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE assets ADD COLUMN photoUri TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "asset_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
