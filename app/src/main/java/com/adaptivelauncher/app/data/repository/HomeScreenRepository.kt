package com.adaptivelauncher.app.data.repository

import com.adaptivelauncher.app.data.db.HomeScreenDao
import com.adaptivelauncher.app.data.db.HomeScreenItemEntity
import kotlinx.coroutines.flow.Flow

class HomeScreenRepository(private val homeScreenDao: HomeScreenDao) {

    fun getHomeScreenItems(): Flow<List<HomeScreenItemEntity>> {
        return homeScreenDao.getHomeScreenItems()
    }

    fun getDockItems(): Flow<List<HomeScreenItemEntity>> {
        return homeScreenDao.getDockItems()
    }

    fun getItemsOnPage(page: Int): Flow<List<HomeScreenItemEntity>> {
        return homeScreenDao.getItemsOnPage(page)
    }

    suspend fun addAppToHomeScreen(
        packageName: String,
        activityName: String?,
        label: String,
        page: Int,
        position: Int? = null,
        isDock: Boolean = false,
        folderId: Long? = null
    ): Long {
        val pos = position ?: (homeScreenDao.getMaxPosition(page, isDock) ?: -1) + 1
        val item = HomeScreenItemEntity(
            packageName = packageName,
            activityName = activityName,
            page = page,
            position = pos,
            isDock = isDock,
            folderId = folderId,
            label = label
        )
        return homeScreenDao.insertItem(item)
    }

    suspend fun addFolderToHomeScreen(folderId: Long, folderName: String, page: Int, position: Int? = null): Long {
        val pos = position ?: (homeScreenDao.getMaxPosition(page, false) ?: -1) + 1
        val item = HomeScreenItemEntity(
            packageName = "folder_$folderId",
            page = page,
            position = pos,
            isDock = false,
            folderId = folderId,
            label = folderName
        )
        return homeScreenDao.insertItem(item)
    }

    suspend fun removeItem(item: HomeScreenItemEntity) {
        homeScreenDao.deleteItem(item)
    }

    suspend fun removeItemById(itemId: Long) {
        homeScreenDao.deleteItemById(itemId)
    }

    suspend fun updateItem(item: HomeScreenItemEntity) {
        homeScreenDao.updateItem(item)
    }

    suspend fun updateItems(items: List<HomeScreenItemEntity>) {
        homeScreenDao.updateItems(items)
    }

    suspend fun removeAppFromHomeScreen(packageName: String) {
        homeScreenDao.deleteItemsByPackage(packageName)
    }

    suspend fun getMaxPage(): Int {
        return homeScreenDao.getMaxPage() ?: 0
    }

    suspend fun reorderItemsOnPage(page: Int, items: List<HomeScreenItemEntity>) {
        val dockItems = if (page == 0) {
            items.filter { it.isDock }.mapIndexed { index, item ->
                item.copy(position = index)
            }
        } else emptyList()

        val pageItems = items.filter { !it.isDock }.mapIndexed { index, item ->
            item.copy(position = index)
        }

        val allUpdates = dockItems + pageItems
        homeScreenDao.updateItems(allUpdates)
    }
}
