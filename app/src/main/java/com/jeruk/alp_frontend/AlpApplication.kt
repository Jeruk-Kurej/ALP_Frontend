package com.jeruk.alp_frontend

import android.app.Application
import com.jeruk.alp_frontend.data.container.AppContainer

class AlpApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inisialisasi AppContainer dengan Context aplikasi
        AppContainer.initialize(this)
    }
}