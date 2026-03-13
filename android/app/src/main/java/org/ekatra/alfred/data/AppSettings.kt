package org.ekatra.alfred.data

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * App settings manager for model selection and preferences
 */
class AppSettings(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ekatra_settings", Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_SELECTED_MODEL_ID = "selected_model_id"
        private const val KEY_PARTIAL_PREFIX = "partial_"
        private const val KEY_ANALYTICS_CONSENT = "analytics_consent"
        private const val KEY_CONSENT_SHOWN = "consent_shown"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
    
    /**
     * Get the currently selected model configuration
     */
    fun getSelectedModel(): ModelConfig {
        val savedModelId = prefs.getString(KEY_SELECTED_MODEL_ID, null)
        return if (savedModelId != null) {
            AvailableModels.getModelById(savedModelId) ?: AvailableModels.getDefaultModel()
        } else {
            AvailableModels.getDefaultModel()
        }
    }
    
    /**
     * Save the selected model ID
     */
    fun setSelectedModel(modelId: String) {
        prefs.edit().putString(KEY_SELECTED_MODEL_ID, modelId).apply()
    }

    /**
     * If the selected model file is missing but another known model file exists on disk,
     * automatically switch the selection to that file to avoid false "model not found" states.
     */
    fun recoverDownloadedModel(context: Context): ModelConfig? {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) return null

        val ggufFiles = modelsDir.listFiles { _, name -> name.endsWith(".gguf") } ?: return null
        // Prefer a known filename from AvailableModels
        val known = ggufFiles.firstNotNullOfOrNull { file ->
            AvailableModels.getModelByFilename(file.name)
        }
        return when {
            known != null -> {
                setSelectedModel(known.id)
                known
            }
            ggufFiles.isNotEmpty() -> {
                // Unknown file: keep selection but still signal presence
                null
            }
            else -> null
        }
    }
    
    /**
     * Check if a model has been downloaded
     */
    fun isModelDownloaded(context: Context, model: ModelConfig): Boolean {
        val modelFile = java.io.File(context.filesDir, "models/${model.filename}")
        return modelFile.exists() && modelFile.length() > 0
    }

    fun savePartialDownload(modelId: String, bytes: Long) {
        prefs.edit().putLong("$KEY_PARTIAL_PREFIX$modelId", bytes).apply()
    }

    fun clearPartialDownload(modelId: String) {
        prefs.edit().remove("$KEY_PARTIAL_PREFIX$modelId").apply()
    }

    fun getPartialDownload(modelId: String): Long = prefs.getLong("$KEY_PARTIAL_PREFIX$modelId", 0L)

    // Analytics consent (GDPR / DPDP Act)
    fun setAnalyticsConsent(consent: Boolean) {
        prefs.edit()
            .putBoolean(KEY_ANALYTICS_CONSENT, consent)
            .putBoolean(KEY_CONSENT_SHOWN, true)
            .apply()
    }

    fun hasAnalyticsConsent(): Boolean = prefs.getBoolean(KEY_ANALYTICS_CONSENT, false)
    fun isConsentShown(): Boolean = prefs.getBoolean(KEY_CONSENT_SHOWN, false)

    // Onboarding
    fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    fun isOnboardingComplete(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
}
