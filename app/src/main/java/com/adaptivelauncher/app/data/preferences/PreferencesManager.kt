package com.adaptivelauncher.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val GRID_ROWS = intPreferencesKey("grid_rows")
        val ICON_SIZE = intPreferencesKey("icon_size")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DOCK_ENABLED = booleanPreferencesKey("dock_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
        val SWIPE_DOWN_ACTION = stringPreferencesKey("swipe_down_action")
        val SWAP_THRESHOLD = intPreferencesKey("swap_threshold")
        val HOME_PAGE_COUNT = intPreferencesKey("home_page_count")
        val CURRENT_HOME_PAGE = intPreferencesKey("current_home_page")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val SORT_ORDER_DRAWER = stringPreferencesKey("sort_order_drawer")
    }

    val gridColumns: Flow<Int> = context.dataStore.data.map { it[Keys.GRID_COLUMNS] ?: 4 }
    val gridRows: Flow<Int> = context.dataStore.data.map { it[Keys.GRID_ROWS] ?: 5 }
    val iconSize: Flow<Int> = context.dataStore.data.map { it[Keys.ICON_SIZE] ?: 0 }
    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val dockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.DOCK_ENABLED] ?: true }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }
    val swipeUpAction: Flow<String> = context.dataStore.data.map { it[Keys.SWIPE_UP_ACTION] ?: "drawer" }
    val swipeDownAction: Flow<String> = context.dataStore.data.map { it[Keys.SWIPE_DOWN_ACTION] ?: "none" }
    val swapThreshold: Flow<Int> = context.dataStore.data.map { it[Keys.SWAP_THRESHOLD] ?: 5 }
    val homePageCount: Flow<Int> = context.dataStore.data.map { it[Keys.HOME_PAGE_COUNT] ?: 3 }
    val currentHomePage: Flow<Int> = context.dataStore.data.map { it[Keys.CURRENT_HOME_PAGE] ?: 0 }
    val reducedMotion: Flow<Boolean> = context.dataStore.data.map { it[Keys.REDUCED_MOTION] ?: false }
    val drawerSortOrder: Flow<String> = context.dataStore.data.map { it[Keys.SORT_ORDER_DRAWER] ?: "alphabetical" }

    suspend fun setGridColumns(columns: Int) {
        context.dataStore.edit { it[Keys.GRID_COLUMNS] = columns }
    }

    suspend fun setGridRows(rows: Int) {
        context.dataStore.edit { it[Keys.GRID_ROWS] = rows }
    }

    suspend fun setIconSize(size: Int) {
        context.dataStore.edit { it[Keys.ICON_SIZE] = size }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setDockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DOCK_ENABLED] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSwipeUpAction(action: String) {
        context.dataStore.edit { it[Keys.SWIPE_UP_ACTION] = action }
    }

    suspend fun setSwipeDownAction(action: String) {
        context.dataStore.edit { it[Keys.SWIPE_DOWN_ACTION] = action }
    }

    suspend fun setSwapThreshold(threshold: Int) {
        context.dataStore.edit { it[Keys.SWAP_THRESHOLD] = threshold }
    }

    suspend fun setHomePageCount(count: Int) {
        context.dataStore.edit { it[Keys.HOME_PAGE_COUNT] = count }
    }

    suspend fun setCurrentHomePage(page: Int) {
        context.dataStore.edit { it[Keys.CURRENT_HOME_PAGE] = page }
    }

    suspend fun setReducedMotion(reduced: Boolean) {
        context.dataStore.edit { it[Keys.REDUCED_MOTION] = reduced }
    }

    suspend fun setDrawerSortOrder(order: String) {
        context.dataStore.edit { it[Keys.SORT_ORDER_DRAWER] = order }
    }
}
