package com.adaptivelauncher.app.data.repository

import com.adaptivelauncher.app.data.db.UsageDao
import com.adaptivelauncher.app.data.db.UsageSummaryEntity
import kotlinx.coroutines.flow.Flow

class UsageRepository(private val usageDao: UsageDao) {

    fun getUsageForPackage(packageName: String, since: Long): Flow<List<UsageSummaryEntity>> {
        return usageDao.getUsageForPackage(packageName, since)
    }

    suspend fun getLatestUsage(packageName: String, since: Long): UsageSummaryEntity? {
        return usageDao.getLatestUsageForPackage(packageName, since)
    }

    suspend fun getAllUsageSince(since: Long): List<UsageSummaryEntity> {
        return usageDao.getAllUsageSince(since)
    }

    fun getAllUsageSinceFlow(since: Long): Flow<List<UsageSummaryEntity>> {
        return usageDao.getAllUsageSinceFlow(since)
    }

    suspend fun insertUsage(usage: UsageSummaryEntity) {
        usageDao.insertUsage(usage)
    }

    suspend fun insertUsages(usages: List<UsageSummaryEntity>) {
        usageDao.insertUsages(usages)
    }

    suspend fun cleanupOldUsage(before: Long) {
        usageDao.deleteOldUsage(before)
    }
}
