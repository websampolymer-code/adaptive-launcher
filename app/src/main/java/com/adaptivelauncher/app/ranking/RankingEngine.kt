package com.adaptivelauncher.app.ranking

import com.adaptivelauncher.app.data.db.FolderApplicationEntity
import com.adaptivelauncher.app.data.db.FolderEntity
import com.adaptivelauncher.app.data.db.RankingPreset
import com.adaptivelauncher.app.data.db.RankingPeriod
import com.adaptivelauncher.app.data.db.SortMode
import com.adaptivelauncher.app.data.db.UsageSummaryEntity
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

class RankingEngine {

    companion object {
        const val SCORE_DIFF_THRESHOLD = 5f

        val BALANCED_WEIGHTS = RankingWeights(0.35f, 0.25f, 0.25f, 0.10f, 0.05f)
        val MOST_FREQUENT_WEIGHTS = RankingWeights(0.60f, 0.10f, 0.20f, 0.05f, 0.05f)
        val MOST_TIME_WEIGHTS = RankingWeights(0.15f, 0.60f, 0.15f, 0.05f, 0.05f)
        val RECENTLY_USED_WEIGHTS = RankingWeights(0.15f, 0.10f, 0.60f, 0.10f, 0.05f)
        val HABIT_BASED_WEIGHTS = RankingWeights(0.25f, 0.15f, 0.20f, 0.35f, 0.05f)
    }

    data class RankingWeights(
        val frequency: Float,
        val duration: Float,
        val recency: Float,
        val consistency: Float,
        val launcherInteraction: Float
    ) {
        fun total(): Float = frequency + duration + recency + consistency + launcherInteraction

        fun isValid(): Boolean {
            val total = total()
            return kotlin.math.abs(total - 1.0f) < 0.01f
        }
    }

    data class ScoreBreakdown(
        val packageName: String,
        val totalScore: Float,
        val frequencyScore: Float,
        val durationScore: Float,
        val recencyScore: Float,
        val consistencyScore: Float,
        val launcherScore: Float,
        val rank: Int
    )

    fun getPresetWeights(preset: RankingPreset): RankingWeights {
        return when (preset) {
            RankingPreset.BALANCED -> BALANCED_WEIGHTS
            RankingPreset.MOST_FREQUENTLY_OPENED -> MOST_FREQUENT_WEIGHTS
            RankingPreset.MOST_TIME_SPENT -> MOST_TIME_WEIGHTS
            RankingPreset.RECENTLY_USED -> RECENTLY_USED_WEIGHTS
            RankingPreset.HABIT_BASED -> HABIT_BASED_WEIGHTS
            RankingPreset.CUSTOM -> BALANCED_WEIGHTS
        }
    }

    fun getWeightsForFolder(folder: FolderEntity): RankingWeights {
        return if (folder.rankingPreset == RankingPreset.CUSTOM) {
            RankingWeights(
                frequency = folder.customFrequencyWeight,
                duration = folder.customDurationWeight,
                recency = folder.customRecencyWeight,
                consistency = folder.customConsistencyWeight,
                launcherInteraction = folder.customLauncherWeight
            )
        } else {
            getPresetWeights(folder.rankingPreset)
        }
    }

    fun getHalfLifeHours(period: RankingPeriod): Double {
        return when (period) {
            RankingPeriod.TODAY -> 4.0
            RankingPeriod.LAST_7_DAYS -> 24.0
            RankingPeriod.LAST_30_DAYS -> 48.0
            RankingPeriod.ADAPTIVE_HISTORY -> 72.0
        }
    }

    fun calculateRecencyScore(lastUsedTimestamp: Long, halfLifeHours: Double): Float {
        if (lastUsedTimestamp == 0L) return 0f

        val now = System.currentTimeMillis()
        val hoursSinceLastUse = (now - lastUsedTimestamp) / (1000.0 * 60.0 * 60.0)
        return (100.0 * exp(-hoursSinceLastUse / halfLifeHours)).toFloat().coerceIn(0f, 100f)
    }

    fun calculateFrequencyScore(launchCount: Int, maxLaunchCount: Int): Float {
        if (maxLaunchCount == 0) return 0f
        val logApp = ln(1.0 + launchCount)
        val logMax = ln(1.0 + maxLaunchCount)
        return if (logMax == 0.0) 0f else (100.0 * logApp / logMax).toFloat()
    }

    fun calculateDurationScore(durationMinutes: Double, maxDurationMinutes: Double): Float {
        if (maxDurationMinutes == 0.0) return 0f
        val logApp = ln(1.0 + durationMinutes)
        val logMax = ln(1.0 + maxDurationMinutes)
        return if (logMax == 0.0) 0f else (100.0 * logApp / logMax).toFloat()
    }

    fun calculateConsistencyScore(activeDays: Int, totalDays: Int): Float {
        if (totalDays <= 0) return 0f
        return (100.0 * activeDays / totalDays).toFloat().coerceIn(0f, 100f)
    }

    fun calculateLauncherScore(launcherLaunchCount: Int, maxLauncherLaunchCount: Int): Float {
        if (maxLauncherLaunchCount == 0) return 0f
        return (100.0 * launcherLaunchCount / maxLauncherLaunchCount).toFloat()
    }

    fun calculateEventWeight(timestamp: Long): Float {
        val now = System.currentTimeMillis()
        val hoursAgo = (now - timestamp) / (1000.0 * 60.0 * 60.0)
        return when {
            hoursAgo < 24 -> 5f
            hoursAgo < 72 -> 3f
            hoursAgo < 168 -> 2f
            else -> 1f
        }
    }

    fun rankFolder(
        folder: FolderEntity,
        folderApps: List<FolderApplicationEntity>,
        usageData: Map<String, UsageSummaryEntity>,
        periodDays: Int
    ): List<ScoreBreakdown> {
        if (folder.sortMode != SortMode.SMART || !folder.smartModeEnabled) {
            return emptyList()
        }

        val weights = getWeightsForFolder(folder)
        val halfLifeHours = getHalfLifeHours(folder.rankingPeriod)

        val nonExcludedApps = folderApps.filter { !it.excludedFromRanking && !it.isPinned }

        if (nonExcludedApps.isEmpty()) return emptyList()

        val maxLaunchCount = nonExcludedApps.maxOfOrNull { usageData[it.packageName]?.launchCount ?: 0 } ?: 1
        val maxDuration = nonExcludedApps.maxOfOrNull {
            (usageData[it.packageName]?.foregroundDuration ?: 0L) / 60000.0
        } ?: 1.0
        val maxLauncherLaunches = nonExcludedApps.maxOfOrNull {
            usageData[it.packageName]?.launcherLaunchCount ?: 0
        } ?: 1

        return nonExcludedApps.map { app ->
            val usage = usageData[app.packageName]
            val launchCount = usage?.launchCount ?: 0
            val durationMinutes = (usage?.foregroundDuration ?: 0L) / 60000.0
            val lastUsed = usage?.lastUsedTimestamp ?: 0L
            val activeDays = usage?.activeDayCount ?: 0
            val launcherLaunches = usage?.launcherLaunchCount ?: 0

            val frequencyScore = calculateFrequencyScore(launchCount, maxLaunchCount)
            val durationScore = calculateDurationScore(durationMinutes, maxDuration)
            val recencyScore = calculateRecencyScore(lastUsed, halfLifeHours)
            val consistencyScore = calculateConsistencyScore(activeDays, periodDays)
            val launcherScore = calculateLauncherScore(launcherLaunches, maxLauncherLaunches)

            val totalScore = (
                weights.frequency * frequencyScore +
                    weights.duration * durationScore +
                    weights.recency * recencyScore +
                    weights.consistency * consistencyScore +
                    weights.launcherInteraction * launcherScore
                )

            ScoreBreakdown(
                packageName = app.packageName,
                totalScore = totalScore,
                frequencyScore = frequencyScore,
                durationScore = durationScore,
                recencyScore = recencyScore,
                consistencyScore = consistencyScore,
                launcherScore = launcherScore,
                rank = 0
            )
        }.sortedByDescending { it.totalScore }
            .mapIndexed { index, breakdown ->
                breakdown.copy(rank = index + 1)
            }
    }

    fun shouldSwap(current: ScoreBreakdown, next: ScoreBreakdown, threshold: Float = SCORE_DIFF_THRESHOLD): Boolean {
        return current.totalScore - next.totalScore > threshold
    }

    fun applyRanking(
        folderApps: List<FolderApplicationEntity>,
        scores: List<ScoreBreakdown>,
        threshold: Float = SCORE_DIFF_THRESHOLD
    ): List<FolderApplicationEntity> {
        val scoreMap = scores.associateBy { it.packageName }
        val pinnedApps = folderApps.filter { it.isPinned }.sortedBy { it.pinnedPosition ?: it.manualPosition }
        val rankedApps = folderApps.filter { !it.isPinned }

        val rankedSorted = rankedApps.sortedByDescending { scoreMap[it.packageName]?.totalScore ?: 0f }

        val stableSorted = mutableListOf<FolderApplicationEntity>()
        for (i in rankedSorted.indices) {
            val app = rankedSorted[i]
            val score = scoreMap[app.packageName]?.totalScore ?: 0f

            if (stableSorted.isNotEmpty()) {
                val prev = stableSorted.last()
                val prevScore = scoreMap[prev.packageName]?.totalScore ?: 0f
                if (kotlin.math.abs(score - prevScore) <= threshold) {
                    if (app.manualPosition > prev.manualPosition) {
                        stableSorted.add(stableSorted.lastIndex, app)
                        continue
                    }
                }
            }
            stableSorted.add(app)
        }

        val allSorted = pinnedApps + stableSorted

        return allSorted.mapIndexed { index, app ->
            app.copy(
                manualPosition = index,
                currentRank = index,
                currentScore = scoreMap[app.packageName]?.totalScore ?: app.currentScore
            )
        }
    }
}
