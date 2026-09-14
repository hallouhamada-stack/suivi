package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("site_inspect_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PROVIDER = "ai_provider"
        private const val KEY_GEMINI_KEY = "gemini_api_key"
        private const val KEY_OPENAI_KEY = "openai_api_key"
        private const val KEY_LANGUAGE = "defect_language"
        private const val KEY_INSPECTOR_NAME = "inspector_name"
        private const val KEY_CONTRACTOR_NAME = "contractor_name"
        private const val KEY_SITE_NAME = "site_name"
    }

    var aiProvider: String
        get() = prefs.getString(KEY_PROVIDER, "GEMINI") ?: "GEMINI"
        set(value) = prefs.edit().putString(KEY_PROVIDER, value).apply()

    val activeApiKey: String
        get() = if (aiProvider == "OPENAI") openaiApiKey else geminiApiKey

    var geminiApiKey: String
        get() {
            val stored = prefs.getString(KEY_GEMINI_KEY, "") ?: ""
            if (stored.isNotBlank()) return stored
            return try {
                // Fallback to BuildConfig if present and not a placeholder
                val buildKey = BuildConfig.GEMINI_API_KEY
                if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY")) buildKey else ""
            } catch (_: Throwable) {
                ""
            }
        }
        set(value) = prefs.edit().putString(KEY_GEMINI_KEY, value).apply()

    var openaiApiKey: String
        get() = prefs.getString(KEY_OPENAI_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_OPENAI_KEY, value).apply()

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "fr") ?: "fr" // Default: French as requested
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var inspectorName: String
        get() = prefs.getString(KEY_INSPECTOR_NAME, "Jean-Marc Dupont (Contrôle Technique)") ?: "Jean-Marc Dupont"
        set(value) = prefs.edit().putString(KEY_INSPECTOR_NAME, value).apply()

    var contractorName: String
        get() = prefs.getString(KEY_CONTRACTOR_NAME, "BTP Bâtiment Général SAS") ?: "BTP Bâtiment Général SAS"
        set(value) = prefs.edit().putString(KEY_CONTRACTOR_NAME, value).apply()

    var siteName: String
        get() = prefs.getString(KEY_SITE_NAME, "Chantier Résidence Les Terrasses du Parc") ?: "Chantier Résidence Les Terrasses du Parc"
        set(value) = prefs.edit().putString(KEY_SITE_NAME, value).apply()
}
