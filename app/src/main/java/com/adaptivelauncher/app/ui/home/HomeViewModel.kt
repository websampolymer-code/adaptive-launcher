package com.adaptivelauncher.app.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adaptivelauncher.app.LauncherApp
import com.adaptivelauncher.app.data.db.FolderApplicationEntity
import com.adaptivelauncher.app.data.db.FolderEntity
import com.adaptivelauncher.app.data.db.HomeScreenItemEntity
import com.adaptivelauncher.app.data.db.LauncherApplicationEntity
import com.adaptivelauncher.app.data.repository.AppRepository
import com.adaptivelauncher.app.data.repository.FolderRepository
import com.adaptivelauncher.app.data.repository.HomeScreenRepository
import com.adaptivelauncher.app.data.repository.UsageRepository
import com.adaptivelauncher.app.usage.UsageTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as LauncherApp
    private val appRepository = AppRepository(application, app.database.applicationDao())
    private val folderRepository = FolderRepository(app.database.folderDao())
    private val homeScreenRepository = HomeScreenRepository(app.database.homeScreenDao())
    private val usageRepository = UsageRepository(app.database.usageDao())
    private val usageTracker = UsageTracker(application, usageRepository)

    val homeScreenItems: Flow<List<HomeScreenItemEntity>> = homeScreenRepository.getHomeScreenItems()
    val dockItems: Flow<List<HomeScreenItemEntity>> = homeScreenRepository.getDockItems()
    val folders: Flow<List<FolderEntity>> = folderRepository.getAllFolders()
    val allApps: Flow<List<LauncherApplicationEntity>> = appRepository.getAllVisibleApps()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _pageCount = MutableStateFlow(3)
    val pageCount: StateFlow<Int> = _pageCount

    init {
        viewModelScope.launch {
            try {
                appRepository.refreshInstalledApps()
            } catch (e: Exception) {
                // Handle gracefully
            }
        }

        viewModelScope.launch {
            try {
                usageTracker.collectUsageStats(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                // Usage access not granted - launcher still works
            }
        }
    }

    fun getLaunchIntent(packageName: String, activityName: String?): Intent? {
        return appRepository.getLaunchIntent(packageName, activityName)
    }

    fun trackLauncherLaunch(packageName: String) {
        viewModelScope.launch {
            usageTracker.incrementLauncherLaunch(packageName)
        }
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    fun addToHomeScreen(item: LauncherApplicationEntity) {
        viewModelScope.launch {
            val page = _currentPage.value
            homeScreenRepository.addAppToHomeScreen(
                packageName = item.packageName,
                activityName = item.activityName,
                label = item.label,
                page = page
            )
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val page = _currentPage.value
            val folderId = folderRepository.createFolder(name, 0)
            homeScreenRepository.addFolderToHomeScreen(folderId, name, page)
        }
    }

    fun addAppToFolder(folderId: Long, packageName: String) {
        viewModelScope.launch {
            folderRepository.addAppToFolder(folderId, packageName)
        }
    }

    fun removeItem(item: HomeScreenItemEntity) {
        viewModelScope.launch {
            homeScreenRepository.removeItem(item)
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            appRepository.refreshInstalledApps()
        }
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        viewModelScope.launch {
            if (hidden) {
                appRepository.hideApp(packageName)
                homeScreenRepository.removeAppFromHomeScreen(packageName)
            } else {
                appRepository.showApp(packageName)
            }
        }
    }

    fun uninstallApp(packageName: String): Intent {
        return appRepository.getUninstallIntent(packageName)
    }

    fun getAppInfo(packageName: String): Intent {
        return appRepository.getAppInfoIntent(packageName)
    }
}
