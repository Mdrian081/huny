package com.autopay.manager.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

/**
 * Lets the app connect to WHICHEVER Firebase project the user provides,
 * instead of one baked in at build time. The user picks their own
 * google-services.json in the app; we parse the handful of fields we need
 * and start a named FirebaseApp instance from them.
 */
object FirebaseConfigManager {

    private const val PREFS = "firebase_dynamic_config"
    private const val APP_NAME = "user_project"

    data class Config(
        val projectId: String,
        val applicationId: String,
        val apiKey: String,
        val storageBucket: String?
    )

    /** Parses a raw google-services.json file's text content. */
    fun parse(jsonText: String): Config? {
        return try {
            val root = JSONObject(jsonText)
            val projectInfo = root.getJSONObject("project_info")
            val projectId = projectInfo.getString("project_id")
            val storageBucket = projectInfo.optString("storage_bucket", null)

            val clientArray = root.getJSONArray("client")
            val client = clientArray.getJSONObject(0)
            val applicationId = client.getJSONObject("client_info")
                .getString("mobilesdk_app_id")
            val apiKey = client.getJSONArray("api_key")
                .getJSONObject(0)
                .getString("current_key")

            Config(projectId, applicationId, apiKey, storageBucket)
        } catch (e: Exception) {
            null
        }
    }

    fun save(context: Context, config: Config) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("project_id", config.projectId)
            .putString("application_id", config.applicationId)
            .putString("api_key", config.apiKey)
            .putString("storage_bucket", config.storageBucket)
            .apply()
    }

    fun load(context: Context): Config? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val projectId = prefs.getString("project_id", null) ?: return null
        val applicationId = prefs.getString("application_id", null) ?: return null
        val apiKey = prefs.getString("api_key", null) ?: return null
        val storageBucket = prefs.getString("storage_bucket", null)
        return Config(projectId, applicationId, apiKey, storageBucket)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun isConnected(context: Context): Boolean = load(context) != null

    /**
     * Returns a Firestore instance backed by the saved config, initializing
     * the named FirebaseApp the first time it's needed.
     */
    fun getFirestore(context: Context): FirebaseFirestore? {
        val config = load(context) ?: return null

        val existingApp = try {
            FirebaseApp.getInstance(APP_NAME)
        } catch (e: IllegalStateException) {
            null
        }

        val app = existingApp ?: run {
            val options = FirebaseOptions.Builder()
                .setProjectId(config.projectId)
                .setApplicationId(config.applicationId)
                .setApiKey(config.apiKey)
                .apply { config.storageBucket?.let { setStorageBucket(it) } }
                .build()
            FirebaseApp.initializeApp(context, options, APP_NAME)
        }

        return FirebaseFirestore.getInstance(app)
    }
}
