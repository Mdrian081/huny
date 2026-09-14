package com.autopay.manager

import android.app.Application

class AutoPayApplication : Application() {
    // Firebase is initialized dynamically per-user in FirebaseConfigManager,
    // once they connect their own google-services.json inside the app -
    // no build-time Firebase config is required here.
    override fun onCreate() {
        super.onCreate()
    }
}
