package com.adaptivelauncher.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    @Query("SELECT * FROM usage_summaries WHERE packageName = :packageName AND periodStart >= :since ORDER BY periodStart DESC")
    fun getUsageForPackage(packageName: String, since: Long): Flow<List<UsageSummaryEntity>>

    @Query("SELECT * FROM usage_summaries WHERE packageName = :packageName AND periodStart >= :since ORDER BY periodStart DESC LIMIT 1")
    suspend fun getLatestUsageForPackage(packageName: String, since: Long): UsageSummaryEntity?

    @Query("SELECT * FROM usage_summaries WHERE periodStart >= :since")
    suspend fun getAllUsageSince(since: Long): List<UsageSummaryEntity>

    @Query("SELECT * FROM usage_summaries WHERE periodStart >= :since")
    fun getAllUsageSinceFlow(since: Long): Flow<List<UsageSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: UsageSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsages(usages: List<UsageSummaryEntity>)

    @Query("DELETE FROM usage_summaries WHERE periodStart < :before")
    suspend fun deleteOldUsage(before: Long)

    @Query("SELECT * FROM usage_summaries WHERE packageName = :packageName")
    suspend fun getAllUsageForPackage(packageName: String): List<UsageSummaryEntity>
}
