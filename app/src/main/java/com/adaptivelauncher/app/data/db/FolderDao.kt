package com.adaptivelauncher.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY position ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)

    @Query("SELECT * FROM folder_applications WHERE folderId = :folderId ORDER BY manualPosition ASC")
    fun getFolderApps(folderId: Long): Flow<List<FolderApplicationEntity>>

    @Query("SELECT * FROM folder_applications WHERE folderId = :folderId ORDER BY manualPosition ASC")
    suspend fun getFolderAppsList(folderId: Long): List<FolderApplicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderApp(app: FolderApplicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderApps(apps: List<FolderApplicationEntity>)

    @Update
    suspend fun updateFolderApp(app: FolderApplicationEntity)

    @Update
    suspend fun updateFolderApps(apps: List<FolderApplicationEntity>)

    @Delete
    suspend fun deleteFolderApp(app: FolderApplicationEntity)

    @Query("DELETE FROM folder_applications WHERE folderId = :folderId AND packageName = :packageName")
    suspend fun deleteFolderAppByPackage(folderId: Long, packageName: String)

    @Query("SELECT * FROM folder_applications WHERE packageName = :packageName")
    suspend fun getAppInFolders(packageName: String): List<FolderApplicationEntity>

    @Query("SELECT * FROM folder_applications WHERE packageName = :packageName")
    fun getAppsInFoldersFlow(packageName: String): Flow<List<FolderApplicationEntity>>

    @Query("SELECT MAX(manualPosition) FROM folder_applications WHERE folderId = :folderId")
    suspend fun getMaxPositionInFolder(folderId: Long): Int?
}
