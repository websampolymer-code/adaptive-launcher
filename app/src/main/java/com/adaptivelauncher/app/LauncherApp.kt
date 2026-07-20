package com.adaptivelauncher.app

import android.app.Application
import com.adaptivelauncher.app.data.db.AppDatabase
import com.adaptivelauncher.app.data.preferences.PreferencesManager

class LauncherApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LauncherApp
            private set
    }
}
