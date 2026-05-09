package com.personalbiography

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.personalbiography.di.AppContainer
import com.personalbiography.work.RetryPendingWorker

/**
 * Single Application class that owns the lazily-built [AppContainer]
 * (manual DI — no Hilt for MVP), forces the app locale to Hebrew so all
 * `values-iw/` strings + Compose RTL rendering kick in regardless of the
 * device language, and schedules the periodic
 * [RetryPendingWorker] for needs_structuring entries.
 */
class BiographyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.create(applicationContext)

        // Force the app locale to Hebrew — overrides device locale so the
        // entire UI (and `values-iw/` string lookups) come up in RTL.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("iw"))

        RetryPendingWorker.schedule(this)
    }
}
