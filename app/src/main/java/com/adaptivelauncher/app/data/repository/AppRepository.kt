package com.adaptivelauncher.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.adaptivelauncher.app.data.db.ApplicationDao
import com.adaptivelauncher.app.data.db.LauncherApplicationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(
    private val context: Context,
    private val applicationDao: ApplicationDao
) {
    private val packageManager: PackageManager = context.packageManager

    fun getAllVisibleApps(): Flow<List<LauncherApplicationEntity>> {
        return applicationDao.getAllVisibleApps()
    }

    fun getAllApps(): Flow<List<LauncherApplicationEntity>> {
        return applicationDao.getAllApps()
    }

    fun getHiddenApps(): Flow<List<LauncherApplicationEntity>> {
        return applicationDao.getHiddenApps()
    }

    suspend fun getAppByPackage(packageName: String): LauncherApplicationEntity? {
        return applicationDao.getAppByPackage(packageName)
    }

    suspend fun refreshInstalledApps() = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.MATCH_DEFAULT_ONLY)
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val existingApps = applicationDao.getAllVisibleAppsList().associateBy { it.packageName }

        val apps = activities.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val activityName = resolveInfo.activityInfo.name
            val label = resolveInfo.loadLabel(packageManager).toString()
            val appInfo = try {
                packageManager.getApplicationInfo(packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                return@mapNotNull null
            }

            val installTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                packageManager.getPackageInfo(packageName, 0).firstInstallTime
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).firstInstallTime
            }

            val updateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                packageManager.getPackageInfo(packageName, 0).lastUpdateTime
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).lastUpdateTime
            }

            val isDisabled = !appInfo.enabled
            val isHidden = existingApps[packageName]?.isHidden ?: false

            LauncherApplicationEntity(
                packageName = packageName,
                label = label,
                activityName = activityName,
                installedAt = installTime,
                lastUpdatedAt = updateTime,
                isHidden = isHidden,
                isDisabled = isDisabled
            )
        }

        applicationDao.insertApps(apps)
    }

    suspend fun hideApp(packageName: String) {
        applicationDao.setAppHidden(packageName, true)
    }

    suspend fun showApp(packageName: String) {
        applicationDao.setAppHidden(packageName, false)
    }

    suspend fun deleteApp(packageName: String) {
        applicationDao.deleteApp(packageName)
    }

    fun getLaunchIntent(packageName: String, activityName: String? = null): Intent? {
        return try {
            if (activityName != null) {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setClassName(packageName, activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUninstallIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_DELETE).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun getAppInfoIntent(packageName: String): Intent {
        return Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
