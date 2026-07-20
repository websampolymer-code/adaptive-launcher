package com.adaptivelauncher.app.data.repository

import com.adaptivelauncher.app.data.db.FolderApplicationEntity
import com.adaptivelauncher.app.data.db.FolderDao
import com.adaptivelauncher.app.data.db.FolderEntity
import com.adaptivelauncher.app.data.db.SortMode
import kotlinx.coroutines.flow.Flow

class FolderRepository(private val folderDao: FolderDao) {

    fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    suspend fun getFolderById(id: Long): FolderEntity? = folderDao.getFolderById(id)

    suspend fun createFolder(name: String, position: Int): Long {
        val folder = FolderEntity(
            name = name,
            position = position,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return folderDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: FolderEntity) {
        folderDao.updateFolder(folder.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteFolder(folderId: Long) {
        folderDao.deleteFolderById(folderId)
    }

    fun getFolderApps(folderId: Long): Flow<List<FolderApplicationEntity>> {
        return folderDao.getFolderApps(folderId)
    }

    suspend fun getFolderAppsList(folderId: Long): List<FolderApplicationEntity> {
        return folderDao.getFolderAppsList(folderId)
    }

    suspend fun addAppToFolder(folderId: Long, packageName: String): Long {
        val maxPos = folderDao.getMaxPositionInFolder(folderId) ?: -1
        val app = FolderApplicationEntity(
            folderId = folderId,
            packageName = packageName,
            manualPosition = maxPos + 1,
            addedAt = System.currentTimeMillis()
        )
        return folderDao.insertFolderApp(app)
    }

    suspend fun removeAppFromFolder(folderId: Long, packageName: String) {
        folderDao.deleteFolderAppByPackage(folderId, packageName)
    }

    suspend fun updateFolderApp(app: FolderApplicationEntity) {
        folderDao.updateFolderApp(app)
    }

    suspend fun updateFolderApps(apps: List<FolderApplicationEntity>) {
        folderDao.updateFolderApps(apps)
    }

    suspend fun getAppInFolders(packageName: String): List<FolderApplicationEntity> {
        return folderDao.getAppInFolders(packageName)
    }

    suspend fun setAppPinned(folderId: Long, packageName: String, pinned: Boolean, position: Int? = null) {
        val apps = folderDao.getFolderAppsList(folderId)
        val app = apps.find { it.packageName == packageName } ?: return
        folderDao.updateFolderApp(
            app.copy(
                isPinned = pinned,
                pinnedPosition = if (pinned) position else null,
                manualPosition = position ?: app.manualPosition
            )
        )
    }

    suspend fun setAppExcludedFromRanking(folderId: Long, packageName: String, excluded: Boolean) {
        val apps = folderDao.getFolderAppsList(folderId)
        val app = apps.find { it.packageName == packageName } ?: return
        folderDao.updateFolderApp(app.copy(excludedFromRanking = excluded))
    }

    suspend fun toggleFolderSortMode(folderId: Long) {
        val folder = folderDao.getFolderById(folderId) ?: return
        val newMode = if (folder.sortMode == SortMode.MANUAL) SortMode.SMART else SortMode.MANUAL
        folderDao.updateFolder(folder.copy(sortMode = newMode, updatedAt = System.currentTimeMillis()))
    }
}
