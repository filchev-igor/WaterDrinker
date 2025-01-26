package com.example.waterdrinker

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object DataStoreManager {

    // Define the DataStore instance
    private val Context.dataStore by preferencesDataStore("water_tracker")

    // Save a value to the DataStore
    suspend fun saveValue(context: Context, key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // Read a value from the DataStore with a default fallback
    suspend fun readValue(context: Context, key: Preferences.Key<Int>, defaultValue: Int): Int {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.first()
    }
}
