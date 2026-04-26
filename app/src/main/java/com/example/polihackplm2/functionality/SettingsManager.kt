package com.example.polihackplm2.functionality

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsManager {
    private val SENSITIVITY_KEY = intPreferencesKey("sensitivity_level")
    private val AUTO_BLOCK_KEY = booleanPreferencesKey("auto_block")
    private val LIVE_REFRESH_KEY = booleanPreferencesKey("live_refresh")
    private val JAVASCRIPT_ENABLED_KEY = booleanPreferencesKey("javascript_enabled")

    fun getSensitivity(context: Context): Flow<Int> = context.dataStore.data.map { it[SENSITIVITY_KEY] ?: 1 }
    fun getAutoBlock(context: Context): Flow<Boolean> = context.dataStore.data.map { it[AUTO_BLOCK_KEY] ?: false }
    fun getLiveRefresh(context: Context): Flow<Boolean> = context.dataStore.data.map { it[LIVE_REFRESH_KEY] ?: true }
    fun getJavaScriptEnabled(context: Context): Flow<Boolean> = context.dataStore.data.map { it[JAVASCRIPT_ENABLED_KEY] ?: false }

    suspend fun setSensitivity(context: Context, level: Int) {
        context.dataStore.edit { it[SENSITIVITY_KEY] = level }
    }

    suspend fun setAutoBlock(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[AUTO_BLOCK_KEY] = enabled }
    }

    suspend fun setLiveRefresh(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[LIVE_REFRESH_KEY] = enabled }
    }

    suspend fun setJavaScriptEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[JAVASCRIPT_ENABLED_KEY] = enabled }
    }
}
