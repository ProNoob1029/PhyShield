package com.example.polihackplm2

import android.app.Application
import com.example.polihackplm2.db.AppDatabase
import com.example.polihackplm2.functionality.BlocklistManager
import com.example.polihackplm2.functionality.HomeRefreshManager

class PhishShieldApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // Initialize managers with database DAOs
        BlocklistManager.init(database.blocklistDao())
        HomeRefreshManager.init(database.scanHistoryDao(), database.blocklistDao())
    }
}
