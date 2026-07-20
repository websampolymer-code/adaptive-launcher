package com.adaptivelauncher.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SortMode {
    MANUAL, SMART
}

enum class RankingPreset {
    BALANCED, MOST_FREQUENTLY_OPENED, MOST_TIME_SPENT, RECENTLY_USED, HABIT_BASED, CUSTOM
}

enum class RankingPeriod {
    TODAY, LAST_7_DAYS, LAST_30_DAYS, ADAPTIVE_HISTORY
}

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "Folder",
    val color: Int = 0,
    val icon: String? = null,
    val sortMode: SortMode = SortMode.MANUAL,
    val rankingPreset: RankingPreset = RankingPreset.BALANCED,
    val customFrequencyWeight: Float = 0.35f,
    val customDurationWeight: Float = 0.25f,
    val customRecencyWeight: Float = 0.25f,
    val customConsistencyWeight: Float = 0.10f,
    val customLauncherWeight: Float = 0.05f,
    val rankingPeriod: RankingPeriod = RankingPeriod.LAST_30_DAYS,
    val smartModeEnabled: Boolean = false,
    val lockOrder: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "folder_applications",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderId"), Index("packageName")]
)
data class FolderApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long,
    val packageName: String,
    val manualPosition: Int = 0,
    val pinnedPosition: Int? = null,
    val isPinned: Boolean = false,
    val excludedFromRanking: Boolean = false,
    val previousRank: Int = 0,
    val currentRank: Int = 0,
    val currentScore: Float = 0f,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "launcher_applications")
data class LauncherApplicationEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val activityName: String,
    val installedAt: Long = 0L,
    val lastUpdatedAt: Long = 0L,
    val isHidden: Boolean = false,
    val isDisabled: Boolean = false
)

@Entity(tableName = "home_screen_items")
data class HomeScreenItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val activityName: String? = null,
    val page: Int = 0,
    val position: Int = 0,
    val isDock: Boolean = false,
    val folderId: Long? = null,
    val label: String = ""
)

@Entity(tableName = "usage_summaries")
data class UsageSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val periodStart: Long,
    val periodEnd: Long,
    val launchCount: Int = 0,
    val foregroundDuration: Long = 0,
    val launcherLaunchCount: Int = 0,
    val activeDayCount: Int = 0,
    val lastUsedTimestamp: Long = 0L,
    val totalScore: Float = 0f,
    val frequencyScore: Float = 0f,
    val durationScore: Float = 0f,
    val recencyScore: Float = 0f,
    val consistencyScore: Float = 0f,
    val launcherScore: Float = 0f,
    val calculationTimestamp: Long = 0L
)
