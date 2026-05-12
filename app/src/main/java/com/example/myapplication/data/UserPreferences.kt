package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val ACTIVE_TEACHER = stringPreferencesKey("active_teacher")
        val USER_PIN = stringPreferencesKey("user_pin")
    }

    val activeTeacherFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ACTIVE_TEACHER]
        }

    val userPinFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_PIN]
        }

    suspend fun saveActiveTeacher(name: String) {
        context.dataStore.edit { preferences ->
            preferences[ACTIVE_TEACHER] = name
        }
    }

    suspend fun saveUserPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_PIN] = pin
        }
    }
}

