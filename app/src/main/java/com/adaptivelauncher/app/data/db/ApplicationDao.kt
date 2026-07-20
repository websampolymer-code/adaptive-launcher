package com.adaptivelauncher.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {

    @Query("SELECT * FROM launcher_applications WHERE isHidden = 0 AND isDisabled = 0 ORDER BY label ASC")
    fun getAllVisibleApps(): Flow<List<LauncherApplicationEntity>>

    @Query("SELECT * FROM launcher_applications ORDER BY label ASC")
    fun getAllApps(): Flow<List<LauncherApplicationEntity>>

    @Query("SELECT * FROM launcher_applications WHERE isHidden = 0 AND isDisabled = 0")
    suspend fun getAllVisibleAppsList(): List<LauncherApplicationEntity>

    @Query("SELECT * FROM launcher_applications WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): LauncherApplicationEntity?

    @Query("SELECT * FROM launcher_applications WHERE isHidden = 1")
    fun getHiddenApps(): Flow<List<LauncherApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: LauncherApplicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<LauncherApplicationEntity>)

    @Update
    suspend fun updateApp(app: LauncherApplicationEntity)

    @Query("UPDATE launcher_applications SET isHidden = :hidden WHERE packageName = :packageName")
    suspend fun setAppHidden(packageName: String, hidden: Boolean)

    @Query("UPDATE launcher_applications SET isDisabled = :disabled WHERE packageName = :packageName")
    suspend fun setAppDisabled(packageName: String, disabled: Boolean)

    @Query("DELETE FROM launcher_applications WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)
}
