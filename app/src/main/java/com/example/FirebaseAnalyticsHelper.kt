package com.example

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object FirebaseAnalyticsHelper {
    private const val TAG = "FirebaseAnalyticsHelper"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
            Log.d(TAG, "Firebase Analytics initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase Analytics: ${e.message}")
        }
    }

    fun logEvent(eventName: String, params: Bundle? = null) {
        try {
            firebaseAnalytics?.logEvent(eventName, params)
            Log.d(TAG, "Logged event: $eventName with params: $params")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log event $eventName: ${e.message}")
        }
    }

    fun logAppOpen() {
        logEvent(FirebaseAnalytics.Event.APP_OPEN)
    }

    fun logStartConversion(fileCount: Int, format: String) {
        val bundle = Bundle().apply {
            putInt("file_count", fileCount)
            putString("output_format", format)
        }
        logEvent("start_conversion", bundle)
    }

    fun logConversionSuccess(successCount: Int, failedCount: Int, format: String) {
        val bundle = Bundle().apply {
            putInt("success_count", successCount)
            putInt("failed_count", failedCount)
            putString("output_format", format)
        }
        logEvent("conversion_success", bundle)
    }

    fun logSelectImages(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("select_images", bundle)
    }
}
