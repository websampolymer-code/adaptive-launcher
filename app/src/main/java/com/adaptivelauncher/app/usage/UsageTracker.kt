package com.adaptivelauncher.app.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import com.adaptivelauncher.app.data.db.UsageSummaryEntity
import com.adaptivelauncher.app.data.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsageTracker(
    private val context: Context,
    private val usageRepository: UsageRepository
) {
    private val usageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    fun hasUsagePermission(): Boolean {
        return usageStatsManager != null
    }

    suspend fun collectUsageStats(sinceTimestamp: Long) = withContext(Dispatchers.IO) {
        val manager = usageStatsManager ?: return@withContext

        val endTime = System.currentTimeMillis()
        val usageStats = try {
            manager.queryAndAggregateUsageStats(sinceTimestamp, endTime)
        } catch (e: Exception) {
            return@withContext
        }

        val summaries = usageStats.map { (packageName, stats) ->
            val launchCount = stats.totalTimesForegrounded
            val duration = stats.totalTimeInForeground
            val lastUsed = stats.lastTimeUsed

            val activeDays = calculateActiveDays(sinceTimestamp, endTime)

            UsageSummaryEntity(
                packageName = packageName,
                periodStart = sinceTimestamp,
                periodEnd = endTime,
                launchCount = launchCount,
                foregroundDuration = duration,
                launcherLaunchCount = 0,
                activeDayCount = activeDays,
                lastUsedTimestamp = lastUsed,
                calculationTimestamp = System.currentTimeMillis()
            )
        }

        usageRepository.insertUsages(summaries)
    }

    suspend fun getUsageStats(sinceTimestamp: Long): Map<String, UsageStatData> = withContext(Dispatchers.IO) {
        val manager = usageStatsManager ?: return@withContext emptyMap()

        val endTime = System.currentTimeMillis()
        val usageStats = try {
            manager.queryAndAggregateUsageStats(sinceTimestamp, endTime)
        } catch (e: Exception) {
            return@withContext emptyMap()
        }

        usageStats.mapValues { (_, stats) ->
            UsageStatData(
                packageName = stats.packageName,
                launchCount = stats.totalTimesForegrounded,
                foregroundDuration = stats.totalTimeInForeground,
                lastUsedTimestamp = stats.lastTimeUsed,
                firstTimeStamp = stats.firstTimeStamp,
                lastTimeStamp = stats.lastTimeStamp
            )
        }
    }

    suspend fun incrementLauncherLaunch(packageName: String) {
        val existing = usageRepository.getLatestUsage(packageName, System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
        if (existing != null) {
            usageRepository.insertUsage(
                existing.copy(
                    launcherLaunchCount = existing.launcherLaunchCount + 1,
                    calculationTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun calculateActiveDays(startTime: Long, endTime: Long): Int {
        val dayMs = 24 * 60 * 60 * 1000L
        return ((endTime - startTime) / dayMs).toInt().coerceAtLeast(1)
    }

    data class UsageStatData(
        val packageName: String,
        val launchCount: Int,
        val foregroundDuration: Long,
        val lastUsedTimestamp: Long,
        val firstTimeStamp: Long,
        val lastTimeStamp: Long
    )
}
