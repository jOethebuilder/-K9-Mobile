package com.joethebuilder.k9.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "k9_settings")

/**
 * Replaces the firmware's NVS namespaces: "k9wifi" (not needed on a phone —
 * Android manages its own WiFi, see SettingsMenuScreen's "Open WiFi Settings"
 * deep link instead of a rebuilt SSID/password picker) and "k9u1" (u1Host,
 * still needed here).
 */
class PrefsRepository(private val context: Context) {

    private val U1_HOST_KEY = stringPreferencesKey("u1_host")

    val u1Host: Flow<String> = context.dataStore.data.map { it[U1_HOST_KEY] ?: "" }

    suspend fun saveU1Host(host: String) {
        context.dataStore.edit { it[U1_HOST_KEY] = host }
    }

    /** Port of firmware's SCR_FACTORY_CONFIRM -> prefs.clear() on both NVS namespaces. */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
