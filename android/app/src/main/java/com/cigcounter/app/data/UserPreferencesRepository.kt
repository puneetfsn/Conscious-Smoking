package com.cigcounter.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smokeledger_preferences")

data class UserSettings(
    val pricePerCigarette: Double = 10.0,
    val wishlistName: String = "",
    val wishlistCost: Double = 0.0,
    val widgetTheme: String = "NOTHING_DARK", // "NOTHING_DARK" or "NOTHING_WHITE"
    val pinCode: String = "",
    val eveningNotificationEnabled: Boolean = true,
    val lastMilestoneShown: Int = 0,
    val isDarkMode: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private val priceKey = doublePreferencesKey("price_per_cigarette_units")
    private val wishlistNameKey = stringPreferencesKey("wishlist_name")
    private val wishlistCostKey = doublePreferencesKey("wishlist_cost")
    private val widgetThemeKey = stringPreferencesKey("widget_theme")
    private val pinCodeKey = stringPreferencesKey("pin_code")
    private val eveningNotificationKey = booleanPreferencesKey("evening_notification_enabled")
    private val lastMilestoneShownKey = intPreferencesKey("last_milestone_shown")
    private val darkModeKey = booleanPreferencesKey("is_dark_mode")

    val settingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserSettings(
                pricePerCigarette = preferences[priceKey] ?: 10.0,
                wishlistName = preferences[wishlistNameKey] ?: "",
                wishlistCost = preferences[wishlistCostKey] ?: 0.0,
                widgetTheme = preferences[widgetThemeKey] ?: "NOTHING_DARK",
                pinCode = preferences[pinCodeKey] ?: "",
                eveningNotificationEnabled = preferences[eveningNotificationKey] ?: true,
                lastMilestoneShown = preferences[lastMilestoneShownKey] ?: 0,
                isDarkMode = preferences[darkModeKey] ?: true
            )
        }

    val pricePerCigaretteFlow: Flow<Double> = settingsFlow.map { it.pricePerCigarette }

    suspend fun getSettings(): UserSettings = settingsFlow.first()

    suspend fun setPricePerCigarette(newPrice: Double) {
        context.dataStore.edit { it[priceKey] = newPrice }
    }

    suspend fun setWishlist(name: String, cost: Double) {
        context.dataStore.edit {
            it[wishlistNameKey] = name
            it[wishlistCostKey] = cost
        }
    }

    suspend fun clearWishlist() {
        context.dataStore.edit {
            it[wishlistNameKey] = ""
            it[wishlistCostKey] = 0.0
        }
    }

    suspend fun setLastMilestoneShown(milestone: Int) {
        context.dataStore.edit {
            it[lastMilestoneShownKey] = milestone
        }
    }

    suspend fun setWidgetTheme(theme: String) {
        context.dataStore.edit { it[widgetThemeKey] = theme }
    }

    suspend fun setPinCode(pin: String) {
        context.dataStore.edit { it[pinCodeKey] = pin }
    }

    suspend fun setEveningNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[eveningNotificationKey] = enabled }
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { it[darkModeKey] = isDark }
    }
}
