package org.ekatra.alfred.viewmodel

import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.ekatra.alfred.data.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized Firebase Analytics event logging.
 * Injected via Hilt — all ViewModels share the same instance.
 */
@Singleton
class AnalyticsHelper @Inject constructor(
    private val analytics: FirebaseAnalytics
) {
    fun logSessionStart(subject: String?, modelId: String) {
        analytics.logEvent("session_start", Bundle().apply {
            putString("subject", subject ?: "general")
            putString("model_id", modelId)
            putString("device_model", Build.MODEL)
        })
    }

    fun logSessionEnd(durationSec: Long, messageCount: Int, subject: String?) {
        analytics.logEvent("session_end", Bundle().apply {
            putLong("duration_sec", durationSec)
            putInt("message_count", messageCount)
            putString("subject", subject ?: "general")
        })
    }

    fun logMessageSent(subject: String?, charCount: Int) {
        analytics.logEvent("message_sent", Bundle().apply {
            putString("subject", subject ?: "general")
            putInt("char_count", charCount)
        })
    }

    fun logResponseReceived(responseTimeMs: Long, tokenCount: Int, modelId: String) {
        analytics.logEvent("response_received", Bundle().apply {
            putLong("response_time_ms", responseTimeMs)
            putInt("token_count", tokenCount)
            putString("model_id", modelId)
        })
    }

    fun logResponseRated(rating: String, sessionId: String) {
        analytics.logEvent("response_rated", Bundle().apply {
            putString("rating", rating) // "up" or "down"
            putString("session_id", sessionId)
        })
    }

    fun logAnswerSaved(subject: String?) {
        analytics.logEvent("answer_saved", Bundle().apply {
            putString("subject", subject ?: "general")
        })
    }

    fun logAnswerShared(subject: String?, shareTarget: String?) {
        analytics.logEvent("answer_shared", Bundle().apply {
            putString("subject", subject ?: "general")
            putString("share_target", shareTarget ?: "unknown")
        })
    }

    fun logModelSelected(modelId: String, sizeMb: Long) {
        analytics.logEvent("model_selected", Bundle().apply {
            putString("model_id", modelId)
            putLong("model_size_mb", sizeMb)
        })
    }

    fun logProfileCompleted(country: String, grade: String) {
        analytics.logEvent("profile_completed", Bundle().apply {
            putString("country", country)
            putString("grade", grade)
        })
    }

    fun setUserProperties(profile: UserProfile) {
        analytics.setUserId(profile.uid)
        analytics.setUserProperty("country", profile.country)
        analytics.setUserProperty("grade", profile.grade)
    }

    fun setModelProperty(modelId: String) {
        analytics.setUserProperty("model_id", modelId)
    }
}
