package com.jarves.mh.update

import android.content.Context
import androidx.work.WorkManager

/** Cancels periodic checks created by older app versions; updates are now manual-only. */
object AppUpdateScheduler {
    private const val UNIQUE_WORK_NAME = "mobile-harness-stable-update-check"

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
