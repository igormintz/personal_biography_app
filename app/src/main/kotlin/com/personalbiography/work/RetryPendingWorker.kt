package com.personalbiography.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.personalbiography.BiographyApp
import com.personalbiography.data.db.EntryEntity
import java.util.concurrent.TimeUnit

/**
 * Background pass that scans for entries whose initial structuring failed
 * (`status = needs_structuring`) and re-runs the LLM pipeline on them.
 *
 * Direct port of `retry_pending_loop` in `app/background.py` — the
 * difference is we let WorkManager do the scheduling instead of an
 * in-process asyncio loop, since this is a phone, not a long-lived server.
 */
class RetryPendingWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? BiographyApp ?: return Result.success()
        val container = app.container
        val secure = container.secureSettings
        if (!secure.hasApiKey()) return Result.success()

        val pending = container.entryRepository.pendingStructuring(limit = 20)
        if (pending.isEmpty()) return Result.success()

        var anyFailure = false
        for (entry in pending) {
            try {
                val result = container.pipeline.restructure(entry.shortId)
                if (result?.entry?.status != EntryEntity.STATUS_OK) anyFailure = true
            } catch (_: Throwable) {
                anyFailure = true
            }
        }
        return if (anyFailure) Result.retry() else Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "retry_pending_structuring"

        fun schedule(context: Context) {
            val req =
                PeriodicWorkRequestBuilder<RetryPendingWorker>(30, TimeUnit.MINUTES)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    )
                    .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req,
            )
        }
    }
}
