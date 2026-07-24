package com.example.crattendance.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val ATTENDANCE_THRESHOLD = intPreferencesKey("attendance_threshold") // E.g., 75
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val TIMETABLE_PDF_PATH = stringPreferencesKey("timetable_pdf_path")
        val THEME_MODE = intPreferencesKey("theme_mode") // 0=System, 1=Light, 2=Dark
        val PERIODS_PER_DAY = intPreferencesKey("periods_per_day") // Allowed: 6, 7, 8
    }

    val isSetupCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SETUP_COMPLETED] ?: false
    }

    val attendanceThreshold: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ATTENDANCE_THRESHOLD] ?: 75
    }

    suspend fun setSetupCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SETUP_COMPLETED] = completed
        }
    }

    suspend fun setAttendanceThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[ATTENDANCE_THRESHOLD] = threshold
        }
    }

    val timetablePdfPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TIMETABLE_PDF_PATH]
    }

    suspend fun setTimetablePdfPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path != null) {
                preferences[TIMETABLE_PDF_PATH] = path
            } else {
                preferences.remove(TIMETABLE_PDF_PATH)
            }
        }
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: 0
    }

    val periodsPerDay: Flow<Int> = context.dataStore.data.map { preferences ->
        val value = preferences[PERIODS_PER_DAY] ?: 6
        if (value in 6..8) value else 6
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setPeriodsPerDay(periods: Int) {
        context.dataStore.edit { preferences ->
            preferences[PERIODS_PER_DAY] = when (periods) {
                7 -> 7
                8 -> 8
                else -> 6
            }
        }
    }
}
