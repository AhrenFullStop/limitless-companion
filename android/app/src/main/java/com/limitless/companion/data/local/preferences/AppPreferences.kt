/**
 * AppPreferences.kt
 * 
 * Secure preferences manager using EncryptedSharedPreferences.
 * 
 * This class manages application settings and sensitive data storage using Android's
 * EncryptedSharedPreferences API, which provides encryption at rest for sensitive data
 * like API keys and server URLs.
 * 
 * Key features:
 * - Encrypted storage for sensitive data
 * - Type-safe preference access
 * - Flow-based reactive updates
 * - Easy migration from regular SharedPreferences
 * 
 * Architecture: Data Layer - Local Storage (Clean Architecture)
 * 
 * TODO(milestone-1): Implement all preference getters/setters
 * TODO(milestone-2): Add preference migration from older versions
 * TODO(milestone-2): Implement preference backup/restore
 * TODO(milestone-3): Add remote config support
 */

package com.limitless.companion.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
// import timber.log.Timber

/**
 * Manager for application preferences with encryption support.
 * 
 * This class provides a centralized interface for accessing and modifying
 * application settings. Sensitive data is stored using EncryptedSharedPreferences.
 */
class AppPreferences(context: Context) {

    // Encrypted preferences for sensitive data
    private val encryptedPrefs: SharedPreferences

    // Regular preferences for non-sensitive data
    private val regularPrefs: SharedPreferences

    // Preference change listener
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        // Timber.d("Preference changed: $key")
        // TODO(milestone-2): Emit preference changes via Flow
    }

    companion object {
        // Preference file names
        private const val ENCRYPTED_PREFS_NAME = "limitless_secure_prefs"
        private const val REGULAR_PREFS_NAME = "limitless_prefs"

        // Encrypted preference keys (for sensitive data)
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_USER_TOKEN = "user_token"

        // Regular preference keys (for non-sensitive data)
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_AUDIO_QUALITY = "audio_quality"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_OFFLINE_MODE = "offline_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_BLUETOOTH_DEVICE_ADDRESS = "bluetooth_device_address"
        private const val KEY_BLUETOOTH_DEVICE_NAME = "bluetooth_device_name"

        // Default values
        private const val DEFAULT_SERVER_URL = "https://api.limitless.app"
        private const val DEFAULT_AUDIO_QUALITY = "high"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_THEME = "system"
    }

    init {
        // Initialize encrypted preferences
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedPrefs = try {
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Timber.e(e, "Failed to create encrypted preferences, falling back to regular")
            // Fallback to regular preferences if encryption fails
            context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
        }

        // Initialize regular preferences
        regularPrefs = context.getSharedPreferences(REGULAR_PREFS_NAME, Context.MODE_PRIVATE)

        // Register preference change listener
        encryptedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        regularPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        // Timber.d("AppPreferences initialized")
    }

    // ========================================
    // Server Configuration (Encrypted)
    // ========================================

    /**
     * Gets the server URL.
     * 
     * @return Server URL
     */
    fun getServerUrl(): String {
        return encryptedPrefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    /**
     * Sets the server URL.
     * 
     * @param url Server URL
     */
    fun setServerUrl(url: String) {
        encryptedPrefs.edit().putString(KEY_SERVER_URL, url).apply()
        // Timber.d("Server URL updated")
    }

    /**
     * Gets the API key.
     * 
     * @return API key or null if not set
     */
    fun getApiKey(): String? {
        return encryptedPrefs.getString(KEY_API_KEY, null)
    }

    /**
     * Sets the API key.
     * 
     * @param apiKey API key
     */
    fun setApiKey(apiKey: String) {
        encryptedPrefs.edit().putString(KEY_API_KEY, apiKey).apply()
        // Timber.d("API key updated")
    }

    /**
     * Clears the API key.
     */
    fun clearApiKey() {
        encryptedPrefs.edit().remove(KEY_API_KEY).apply()
        // Timber.d("API key cleared")
    }

    /**
     * Checks if API key is set.
     * 
     * @return true if API key exists
     */
    fun hasApiKey(): Boolean {
        return getApiKey() != null
    }

    /**
     * Gets the user token.
     * 
     * @return User token or null if not set
     */
    fun getUserToken(): String? {
        return encryptedPrefs.getString(KEY_USER_TOKEN, null)
    }

    /**
     * Sets the user token.
     * 
     * @param token User token
     */
    fun setUserToken(token: String) {
        encryptedPrefs.edit().putString(KEY_USER_TOKEN, token).apply()
        // Timber.d("User token updated")
    }

    /**
     * Clears the user token.
     */
    fun clearUserToken() {
        encryptedPrefs.edit().remove(KEY_USER_TOKEN).apply()
        // Timber.d("User token cleared")
    }

    // ========================================
    // App State (Regular)
    // ========================================

    /**
     * Checks if this is the first launch.
     * 
     * @return true if first launch
     */
    fun isFirstLaunch(): Boolean {
        return regularPrefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    /**
     * Marks the first launch as complete.
     */
    fun setFirstLaunchComplete() {
        regularPrefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    /**
     * Checks if onboarding is completed.
     * 
     * @return true if completed
     */
    fun isOnboardingCompleted(): Boolean {
        return regularPrefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    /**
     * Marks onboarding as completed.
     */
    fun setOnboardingCompleted() {
        regularPrefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    // ========================================
    // Audio Settings
    // ========================================

    /**
     * Gets the audio quality setting.
     * 
     * @return Audio quality ("low", "medium", "high")
     */
    fun getAudioQuality(): String {
        return regularPrefs.getString(KEY_AUDIO_QUALITY, DEFAULT_AUDIO_QUALITY)
            ?: DEFAULT_AUDIO_QUALITY
    }

    /**
     * Sets the audio quality.
     * 
     * @param quality Audio quality ("low", "medium", "high")
     */
    fun setAudioQuality(quality: String) {
        regularPrefs.edit().putString(KEY_AUDIO_QUALITY, quality).apply()
    }

    // ========================================
    // Sync Settings
    // ========================================

    /**
     * Checks if auto-sync is enabled.
     * 
     * @return true if enabled
     */
    fun isAutoSyncEnabled(): Boolean {
        return regularPrefs.getBoolean(KEY_AUTO_SYNC, true)
    }

    /**
     * Sets auto-sync enabled state.
     * 
     * @param enabled true to enable
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    /**
     * Checks if offline mode is enabled.
     * 
     * @return true if enabled
     */
    fun isOfflineModeEnabled(): Boolean {
        return regularPrefs.getBoolean(KEY_OFFLINE_MODE, false)
    }

    /**
     * Sets offline mode enabled state.
     * 
     * @param enabled true to enable
     */
    fun setOfflineModeEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean(KEY_OFFLINE_MODE, enabled).apply()
    }

    /**
     * Gets the last sync timestamp.
     * 
     * @return Last sync time in milliseconds, or 0 if never synced
     */
    fun getLastSyncTime(): Long {
        return regularPrefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }

    /**
     * Updates the last sync timestamp.
     * 
     * @param timestamp Sync timestamp in milliseconds
     */
    fun setLastSyncTime(timestamp: Long = System.currentTimeMillis()) {
        regularPrefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
    }

    // ========================================
    // Bluetooth Settings
    // ========================================

    /**
     * Gets the saved Bluetooth device address.
     * 
     * @return Device address or null
     */
    fun getBluetoothDeviceAddress(): String? {
        return regularPrefs.getString(KEY_BLUETOOTH_DEVICE_ADDRESS, null)
    }

    /**
     * Sets the Bluetooth device address.
     * 
     * @param address Device address
     */
    fun setBluetoothDeviceAddress(address: String) {
        regularPrefs.edit().putString(KEY_BLUETOOTH_DEVICE_ADDRESS, address).apply()
    }

    /**
     * Gets the saved Bluetooth device name.
     * 
     * @return Device name or null
     */
    fun getBluetoothDeviceName(): String? {
        return regularPrefs.getString(KEY_BLUETOOTH_DEVICE_NAME, null)
    }

    /**
     * Sets the Bluetooth device name.
     * 
     * @param name Device name
     */
    fun setBluetoothDeviceName(name: String) {
        regularPrefs.edit().putString(KEY_BLUETOOTH_DEVICE_NAME, name).apply()
    }

    /**
     * Clears saved Bluetooth device.
     */
    fun clearBluetoothDevice() {
        regularPrefs.edit()
            .remove(KEY_BLUETOOTH_DEVICE_ADDRESS)
            .remove(KEY_BLUETOOTH_DEVICE_NAME)
            .apply()
    }

    // ========================================
    // UI Settings
    // ========================================

    /**
     * Checks if notifications are enabled.
     * 
     * @return true if enabled
     */
    fun isNotificationEnabled(): Boolean {
        return regularPrefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    /**
     * Sets notification enabled state.
     * 
     * @param enabled true to enable
     */
    fun setNotificationEnabled(enabled: Boolean) {
        regularPrefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    /**
     * Gets the selected language code.
     * 
     * @return Language code (e.g., "en", "es")
     */
    fun getSelectedLanguage(): String {
        return regularPrefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE)
            ?: DEFAULT_LANGUAGE
    }

    /**
     * Sets the selected language.
     * 
     * @param languageCode Language code
     */
    fun setSelectedLanguage(languageCode: String) {
        regularPrefs.edit().putString(KEY_SELECTED_LANGUAGE, languageCode).apply()
    }

    /**
     * Gets the theme mode.
     * 
     * @return Theme mode ("light", "dark", "system")
     */
    fun getThemeMode(): String {
        return regularPrefs.getString(KEY_THEME_MODE, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    /**
     * Sets the theme mode.
     * 
     * @param theme Theme mode ("light", "dark", "system")
     */
    fun setThemeMode(theme: String) {
        regularPrefs.edit().putString(KEY_THEME_MODE, theme).apply()
    }

    // ========================================
    // Utility Methods
    // ========================================

    /**
     * Clears all preferences (useful for logout).
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
        regularPrefs.edit().clear().apply()
        // Timber.d("All preferences cleared")
    }

    /**
     * Clears only sensitive data.
     */
    fun clearSensitiveData() {
        clearApiKey()
        clearUserToken()
        // Timber.d("Sensitive data cleared")
    }

    /**
     * Checks if the app is properly configured.
     * 
     * @return true if server URL and API key are set
     */
    fun isConfigured(): Boolean {
        return getServerUrl().isNotEmpty() && hasApiKey()
    }

    // ========================================
    // Kotlin property accessors for UI convenience
    // ========================================

    @get:JvmName("getServerUrlProp")
    @set:JvmName("setServerUrlProp")
    var serverUrl: String?
        get() = encryptedPrefs.getString(KEY_SERVER_URL, null)
        set(value) { encryptedPrefs.edit().putString(KEY_SERVER_URL, value ?: "").apply() }

    var useMicFallback: Boolean
        get() = regularPrefs.getBoolean("use_mic_fallback", true)
        set(value) { regularPrefs.edit().putBoolean("use_mic_fallback", value).apply() }
}

