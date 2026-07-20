package com.adaptivelauncher.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeScreenDao {

    @Query("SELECT * FROM home_screen_items WHERE isDock = 0 ORDER BY page ASC, position ASC")
    fun getHomeScreenItems(): Flow<List<HomeScreenItemEntity>>

    @Query("SELECT * FROM home_screen_items WHERE isDock = 1 ORDER BY position ASC")
    fun getDockItems(): Flow<List<HomeScreenItemEntity>>

    @Query("SELECT * FROM home_screen_items WHERE page = :page AND isDock = 0 ORDER BY position ASC")
    fun getItemsOnPage(page: Int): Flow<List<HomeScreenItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HomeScreenItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<HomeScreenItemEntity>)

    @Update
    suspend fun updateItem(item: HomeScreenItemEntity)

    @Update
    suspend fun updateItems(items: List<HomeScreenItemEntity>)

    @Delete
    suspend fun deleteItem(item: HomeScreenItemEntity)

    @Query("DELETE FROM home_screen_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("DELETE FROM home_screen_items WHERE packageName = :packageName AND folderId IS NULL")
    suspend fun deleteItemsByPackage(packageName: String)

    @Query("SELECT MAX(page) FROM home_screen_items")
    suspend fun getMaxPage(): Int?

    @Query("SELECT MAX(position) FROM home_screen_items WHERE page = :page AND isDock = :isDock")
    suspend fun getMaxPosition(page: Int, isDock: Boolean): Int?
}
