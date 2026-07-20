package com.adaptivelauncher.app

import com.adaptivelauncher.app.data.db.FolderApplicationEntity
import com.adaptivelauncher.app.data.db.FolderEntity
import com.adaptivelauncher.app.data.db.RankingPreset
import com.adaptivelauncher.app.data.db.RankingPeriod
import com.adaptivelauncher.app.data.db.SortMode
import com.adaptivelauncher.app.data.db.UsageSummaryEntity
import com.adaptivelauncher.app.ranking.RankingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RankingEngineTest {

    private lateinit var engine: RankingEngine

    @Before
    fun setup() {
        engine = RankingEngine()
    }

    @Test
    fun `frequency score with zero launches returns zero`() {
        val score = engine.calculateFrequencyScore(0, 100)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `frequency score with equal launches returns 100`() {
        val score = engine.calculateFrequencyScore(50, 50)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun `frequency score uses logarithmic normalization`() {
        val score50 = engine.calculateFrequencyScore(50, 100)
        val score100 = engine.calculateFrequencyScore(100, 100)
        assertTrue("Score should increase with more launches", score50 < score100)
        assertEquals(100f, score100, 0.01f)
    }

    @Test
    fun `duration score with zero duration returns zero`() {
        val score = engine.calculateDurationScore(0.0, 100.0)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `duration score with equal duration returns 100`() {
        val score = engine.calculateDurationScore(50.0, 50.0)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun `recency score decreases over time`() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 1000 * 60 * 60
        val oneDayAgo = now - 1000 * 60 * 60 * 24

        val score1h = engine.calculateRecencyScore(oneHourAgo, 24.0)
        val score1d = engine.calculateRecencyScore(oneDayAgo, 24.0)

        assertTrue("Recent use should score higher", score1h > score1d)
        assertTrue("Recent use should score near 100", score1h > 90f)
    }

    @Test
    fun `recency score for zero timestamp returns zero`() {
        val score = engine.calculateRecencyScore(0L, 24.0)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `consistency score with zero active days returns zero`() {
        val score = engine.calculateConsistencyScore(0, 30)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `consistency score with all active days returns 100`() {
        val score = engine.calculateConsistencyScore(30, 30)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun `consistency score is proportional`() {
        val score15 = engine.calculateConsistencyScore(15, 30)
        val score30 = engine.calculateConsistencyScore(30, 30)
        assertEquals(50f, score15, 0.01f)
        assertEquals(100f, score30, 0.01f)
    }

    @Test
    fun `launcher score with zero launches returns zero`() {
        val score = engine.calculateLauncherScore(0, 10)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `launcher score with max launches returns 100`() {
        val score = engine.calculateLauncherScore(5, 5)
        assertEquals(100f, score, 0.01f)
    }

    @Test
    fun `preset weights sum to 100`() {
        val presets = listOf(
            RankingEngine.BALANCED_WEIGHTS,
            RankingEngine.MOST_FREQUENT_WEIGHTS,
            RankingEngine.MOST_TIME_WEIGHTS,
            RankingEngine.RECENTLY_USED_WEIGHTS,
            RankingEngine.HABIT_BASED_WEIGHTS
        )

        presets.forEach { weights ->
            assertTrue("Weights should sum to 1.0, got ${weights.total()}", weights.isValid())
        }
    }

    @Test
    fun `getPresetWeights returns correct weights`() {
        assertEquals(RankingEngine.BALANCED_WEIGHTS, engine.getPresetWeights(RankingPreset.BALANCED))
        assertEquals(RankingEngine.MOST_FREQUENT_WEIGHTS, engine.getPresetWeights(RankingPreset.MOST_FREQUENTLY_OPENED))
        assertEquals(RankingEngine.MOST_TIME_WEIGHTS, engine.getPresetWeights(RankingPreset.MOST_TIME_SPENT))
        assertEquals(RankingEngine.RECENTLY_USED_WEIGHTS, engine.getPresetWeights(RankingPreset.RECENTLY_USED))
        assertEquals(RankingEngine.HABIT_BASED_WEIGHTS, engine.getPresetWeights(RankingPreset.HABIT_BASED))
    }

    @Test
    fun `should swap when score difference exceeds threshold`() {
        val current = RankingEngine.ScoreBreakdown("a", 50f, 50f, 50f, 50f, 50f, 50f, 1)
        val next = RankingEngine.ScoreBreakdown("b", 44f, 44f, 44f, 44f, 44f, 44f, 2)

        assertTrue("Should swap when difference > threshold", engine.shouldSwap(current, next, 5f))
    }

    @Test
    fun `should not swap when score difference below threshold`() {
        val current = RankingEngine.ScoreBreakdown("a", 50f, 50f, 50f, 50f, 50f, 50f, 1)
        val next = RankingEngine.ScoreBreakdown("b", 48f, 48f, 48f, 48f, 48f, 48f, 2)

        assertFalse("Should not swap when difference < threshold", engine.shouldSwap(current, next, 5f))
    }

    @Test
    fun `rank folder with no excluded apps ranks all apps`() {
        val folder = createFolder(smartEnabled = true)
        val apps = listOf(
            createFolderApp("com.app1"),
            createFolderApp("com.app2"),
            createFolderApp("com.app3")
        )
        val usageData = mapOf(
            "com.app1" to createUsage("com.app1", launchCount = 100, duration = 5000),
            "com.app2" to createUsage("com.app2", launchCount = 50, duration = 3000),
            "com.app3" to createUsage("com.app3", launchCount = 10, duration = 1000)
        )

        val rankings = engine.rankFolder(folder, apps, usageData, 30)

        assertEquals(3, rankings.size)
        assertEquals("com.app1", rankings[0].packageName)
        assertEquals("com.app2", rankings[1].packageName)
        assertEquals("com.app3", rankings[2].packageName)
    }

    @Test
    fun `pinned apps are not included in ranking`() {
        val folder = createFolder(smartEnabled = true)
        val apps = listOf(
            createFolderApp("com.app1", isPinned = true, pinnedPosition = 0),
            createFolderApp("com.app2"),
            createFolderApp("com.app3")
        )
        val usageData = mapOf(
            "com.app1" to createUsage("com.app1", launchCount = 100),
            "com.app2" to createUsage("com.app2", launchCount = 50),
            "com.app3" to createUsage("com.app3", launchCount = 10)
        )

        val rankings = engine.rankFolder(folder, apps, usageData, 30)

        assertFalse("Pinned app should not be in rankings", rankings.any { it.packageName == "com.app1" })
        assertEquals(2, rankings.size)
    }

    @Test
    fun `excluded apps are not included in ranking`() {
        val folder = createFolder(smartEnabled = true)
        val apps = listOf(
            createFolderApp("com.app1", excludedFromRanking = true),
            createFolderApp("com.app2"),
            createFolderApp("com.app3")
        )
        val usageData = mapOf(
            "com.app1" to createUsage("com.app1", launchCount = 100),
            "com.app2" to createUsage("com.app2", launchCount = 50),
            "com.app3" to createUsage("com.app3", launchCount = 10)
        )

        val rankings = engine.rankFolder(folder, apps, usageData, 30)

        assertFalse("Excluded app should not be in rankings", rankings.any { it.packageName == "com.app1" })
        assertEquals(2, rankings.size)
    }

    @Test
    fun `disabled smart mode returns empty rankings`() {
        val folder = createFolder(smartEnabled = false)
        val apps = listOf(createFolderApp("com.app1"))
        val usageData = emptyMap<String, UsageSummaryEntity>()

        val rankings = engine.rankFolder(folder, apps, usageData, 30)

        assertTrue("Should return empty rankings when smart mode disabled", rankings.isEmpty())
    }

    @Test
    fun `ranking is isolated per folder`() {
        val folder1 = createFolder(id = 1, smartEnabled = true)
        val folder2 = createFolder(id = 2, smartEnabled = true)

        val apps1 = listOf(
            createFolderApp("com.app1", folderId = 1),
            createFolderApp("com.app2", folderId = 1)
        )
        val apps2 = listOf(
            createFolderApp("com.app1", folderId = 2),
            createFolderApp("com.app2", folderId = 2)
        )

        val usageData = mapOf(
            "com.app1" to createUsage("com.app1", launchCount = 100),
            "com.app2" to createUsage("com.app2", launchCount = 50)
        )

        val rankings1 = engine.rankFolder(folder1, apps1, usageData, 30)
        val rankings2 = engine.rankFolder(folder2, apps2, usageData, 30)

        assertEquals("com.app1", rankings1[0].packageName)
        assertEquals("com.app2", rankings1[1].packageName)
        assertEquals("com.app1", rankings2[0].packageName)
        assertEquals("com.app2", rankings2[1].packageName)
    }

    @Test
    fun `apply ranking preserves pinned apps at correct positions`() {
        val folderApps = listOf(
            createFolderApp("com.app1", isPinned = true, pinnedPosition = 0, manualPosition = 0),
            createFolderApp("com.app2", manualPosition = 1),
            createFolderApp("com.app3", manualPosition = 2)
        )

        val scores = listOf(
            RankingEngine.ScoreBreakdown("com.app3", 90f, 90f, 90f, 90f, 90f, 90f, 1),
            RankingEngine.ScoreBreakdown("com.app2", 50f, 50f, 50f, 50f, 50f, 50f, 2)
        )

        val result = engine.applyRanking(folderApps, scores)

        assertEquals("com.app1", result[0].packageName)
        assertTrue("Pinned app should remain pinned", result[0].isPinned)
    }

    @Test
    fun `apps without usage data appear after ranked apps`() {
        val folderApps = listOf(
            createFolderApp("com.app1"),
            createFolderApp("com.app2"),
            createFolderApp("com.app3")
        )

        val scores = listOf(
            RankingEngine.ScoreBreakdown("com.app1", 80f, 80f, 80f, 80f, 80f, 80f, 1)
        )

        val result = engine.applyRanking(folderApps, scores)

        assertEquals("com.app1", result[0].packageName)
    }

    @Test
    fun `event weight decreases with age`() {
        val now = System.currentTimeMillis()
        val recentWeight = engine.calculateEventWeight(now - 1000 * 60 * 60)
        val oldWeight = engine.calculateEventWeight(now - 1000 * 60 * 60 * 24 * 10)

        assertTrue("Recent events should have higher weight", recentWeight > oldWeight)
    }

    @Test
    fun `event weights match expected ranges`() {
        val now = System.currentTimeMillis()
        assertEquals(5f, engine.calculateEventWeight(now - 1000 * 60 * 60), 0.01f)
        assertEquals(3f, engine.calculateEventWeight(now - 1000 * 60 * 60 * 48), 0.01f)
        assertEquals(2f, engine.calculateEventWeight(now - 1000 * 60 * 60 * 100), 0.01f)
        assertEquals(1f, engine.calculateEventWeight(now - 1000 * 60 * 60 * 200), 0.01f)
    }

    @Test
    fun `get half life hours for different periods`() {
        assertEquals(4.0, engine.getHalfLifeHours(RankingPeriod.TODAY), 0.01)
        assertEquals(24.0, engine.getHalfLifeHours(RankingPeriod.LAST_7_DAYS), 0.01)
        assertEquals(48.0, engine.getHalfLifeHours(RankingPeriod.LAST_30_DAYS), 0.01)
        assertEquals(72.0, engine.getHalfLifeHours(RankingPeriod.ADAPTIVE_HISTORY), 0.01)
    }

    // Helper functions
    private fun createFolder(
        id: Long = 1,
        smartEnabled: Boolean = false,
        preset: RankingPreset = RankingPreset.BALANCED
    ) = FolderEntity(
        id = id,
        name = "Test Folder",
        sortMode = if (smartEnabled) SortMode.SMART else SortMode.MANUAL,
        rankingPreset = preset,
        smartModeEnabled = smartEnabled,
        rankingPeriod = RankingPeriod.LAST_30_DAYS
    )

    private fun createFolderApp(
        packageName: String,
        folderId: Long = 1,
        isPinned: Boolean = false,
        pinnedPosition: Int? = null,
        excludedFromRanking: Boolean = false,
        manualPosition: Int = 0
    ) = FolderApplicationEntity(
        folderId = folderId,
        packageName = packageName,
        manualPosition = manualPosition,
        pinnedPosition = pinnedPosition,
        isPinned = isPinned,
        excludedFromRanking = excludedFromRanking
    )

    private fun createUsage(
        packageName: String,
        launchCount: Int = 0,
        duration: Long = 0,
        launcherLaunchCount: Int = 0,
        activeDays: Int = 30,
        lastUsedTimestamp: Long = System.currentTimeMillis()
    ) = UsageSummaryEntity(
        packageName = packageName,
        periodStart = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000,
        periodEnd = System.currentTimeMillis(),
        launchCount = launchCount,
        foregroundDuration = duration,
        launcherLaunchCount = launcherLaunchCount,
        activeDayCount = activeDays,
        lastUsedTimestamp = lastUsedTimestamp,
        calculationTimestamp = System.currentTimeMillis()
    )
}
