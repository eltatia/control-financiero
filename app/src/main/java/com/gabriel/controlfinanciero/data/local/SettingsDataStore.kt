package com.gabriel.controlfinanciero.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val ACCOUNT_ID = intPreferencesKey("account_id")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val userName: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.USER_NAME] ?: "Alex"
    }

    val accountId: Flow<Int> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.ACCOUNT_ID] ?: 0
    }

    val isLoggedIn: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: true
    }

    suspend fun setUserName(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.USER_NAME] = name
        }
    }

    suspend fun setAccountId(id: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.ACCOUNT_ID] = id
        }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = isLoggedIn
        }
    }
}
