package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueLogDao {
    @Query("SELECT * FROM issue_logs ORDER BY loggedAt DESC")
    fun getAllIssues(): Flow<List<IssueLogEntity>>

    @Query("SELECT * FROM issue_logs WHERE isResolved = 0 ORDER BY loggedAt DESC")
    fun getOpenIssues(): Flow<List<IssueLogEntity>>

    @Query("SELECT * FROM issue_logs WHERE assetId = :assetId ORDER BY loggedAt DESC")
    fun getIssuesByAsset(assetId: Int): Flow<List<IssueLogEntity>>

    @Query("SELECT * FROM issue_logs WHERE issueId = :issueId")
    fun getIssueById(issueId: Int): Flow<IssueLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: IssueLogEntity)

    @Update
    suspend fun updateIssue(issue: IssueLogEntity)
}
