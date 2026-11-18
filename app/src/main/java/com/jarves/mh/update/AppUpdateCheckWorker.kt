package com.jarves.mh.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jarves.mh.data.GitHubAccessTokenStore
import com.jarves.mh.MainActivity
import kotlinx.coroutines.CancellationException

/** Background stable-release check. It never silently downloads or installs an APK. */
class AppUpdateCheckWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val tokenStore = GitHubAccessTokenStore(applicationContext)
        val accessToken = tokenStore.read() ?: return Result.success()
        val update = try {
            AppUpdater(applicationContext, accessToken = accessToken).check()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: java.io.IOException) {
            if (error.message?.contains("Reconnect GitHub", ignoreCase = true) == true) {
                tokenStore.clear()
                return Result.success()
            }
            return Result.retry()
        } catch (_: Exception) {
            return Result.retry()
        } ?: return Result.success()

        val preferences = applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        val lastNotifiedVersion = preferences.getLong(KEY_LAST_NOTIFIED_VERSION, 0L)
        if (update.versionCode <= lastNotifiedVersion) return Result.success()

        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return Result.success()

        createNotificationChannel()
        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Mobile Harness update available")
            .setContentText("Version ${update.versionName} is ready to review in the app.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(update.notes.ifBlank {
                "Open Mobile Harness to download and install the latest verified update."
            }))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        return runCatching {
            manager.notify(NOTIFICATION_ID, notification)
            preferences.edit().putLong(KEY_LAST_NOTIFIED_VERSION, update.versionCode).apply()
            Result.success()
        }.getOrElse { Result.retry() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "App updates",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notifies when a verified Mobile Harness update is available."
        }
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "mobile_harness_app_updates"
        const val NOTIFICATION_ID = 11801
        const val PREFERENCES = "mobile_harness_update_worker"
        const val KEY_LAST_NOTIFIED_VERSION = "last_notified_version_code"
    }
}
