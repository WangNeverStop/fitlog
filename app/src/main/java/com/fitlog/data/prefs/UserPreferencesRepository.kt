package com.fitlog.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fitlog_prefs")

/** Body profile data for the body-data screen (stored per user in DataStore). */
data class BodyData(
    val heightCm: Double? = null,
    val age: Int? = null,
    val gender: String? = null,
) {
    val isComplete: Boolean
        get() = heightCm?.let { it > 0.0 } == true && age?.let { it > 0 } == true
}

/**
 * Lightweight settings via DataStore: current user, unit & rest-timer settings, per-user
 * body data, and per-user calendar memos (keyed by MM-dd so anniversaries recur yearly).
 */
class UserPreferencesRepository(private val context: Context) {

    private val currentUserIdKey = longPreferencesKey("current_user_id")
    private val unitKey = stringPreferencesKey("weight_unit") // "kg" / "lb"
    private val restSecondsKey = intPreferencesKey("rest_seconds")

    val currentUserId: Flow<Long?> = context.dataStore.data.map { it[currentUserIdKey] }

    suspend fun setCurrentUser(id: Long) {
        context.dataStore.edit { it[currentUserIdKey] = id }
    }

    suspend fun clearCurrentUser() {
        context.dataStore.edit { it.remove(currentUserIdKey) }
    }

    // --- training settings (global) ---
    val unit: Flow<String> = context.dataStore.data.map { it[unitKey] ?: "kg" }
    val restSeconds: Flow<Int> = context.dataStore.data.map { it[restSecondsKey] ?: 90 }

    suspend fun setUnit(unit: String) {
        context.dataStore.edit { it[unitKey] = unit }
    }

    suspend fun setRestSeconds(seconds: Int) {
        context.dataStore.edit { it[restSecondsKey] = seconds }
    }

    // --- body data (per user) ---
    fun bodyData(userId: Long): Flow<BodyData> = context.dataStore.data.map { prefs ->
        BodyData(
            heightCm = prefs[stringPreferencesKey("body_height_$userId")]?.toDoubleOrNull(),
            age = prefs[intPreferencesKey("body_age_$userId")],
            gender = prefs[stringPreferencesKey("body_gender_$userId")],
        )
    }

    suspend fun setBodyData(userId: Long, heightCm: Double?, age: Int?, gender: String?) {
        context.dataStore.edit { prefs ->
            if (heightCm != null) prefs[stringPreferencesKey("body_height_$userId")] = heightCm.toString()
            if (age != null) prefs[intPreferencesKey("body_age_$userId")] = age
            if (!gender.isNullOrBlank()) prefs[stringPreferencesKey("body_gender_$userId")] = gender
        }
    }

    // --- calendar memos (per user, keyed by MM-dd so they recur each year) ---
    // Each entry is encoded as "MM-dd|text".
    fun memos(userId: Long): Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[stringSetPreferencesKey("memos_$userId")] ?: emptySet()
    }

    suspend fun addMemo(userId: Long, mmdd: String, text: String) {
        if (text.isBlank()) return
        context.dataStore.edit { prefs ->
            val key = stringSetPreferencesKey("memos_$userId")
            val current = prefs[key] ?: emptySet()
            prefs[key] = current + "$mmdd|${text.trim()}"
        }
    }

    suspend fun removeMemo(userId: Long, entry: String) {
        context.dataStore.edit { prefs ->
            val key = stringSetPreferencesKey("memos_$userId")
            val current = prefs[key] ?: emptySet()
            prefs[key] = current - entry
        }
    }
}
