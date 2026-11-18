package com.jarves.mh.runtime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.system.Os
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jarves.mh.MainActivity
import com.jarves.mh.R
import com.jarves.mh.data.ApiKeyVault
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.security.SecureRandom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FreeLlmApiPhase { IDLE, INSTALLING, INSTALLED, STARTING, RUNNING, ERROR }

data class FreeLlmApiSnapshot(
    val phase: FreeLlmApiPhase = FreeLlmApiPhase.IDLE,
    val installed: Boolean = false,
    val running: Boolean = false,
    val message: String = "FreeLLMAPI is not installed",
    val error: String? = null,
)

object FreeLlmApiController {
    private val mutableSnapshot = MutableStateFlow(FreeLlmApiSnapshot())
    val snapshot: StateFlow<FreeLlmApiSnapshot> = mutableSnapshot.asStateFlow()

    @Volatile internal var gatewayProcess: Process? = null
    @Volatile internal var serviceActive: Boolean = false

    @Synchronized
    fun refresh(context: Context) {
        val installed = isInstalled(context)
        val running = gatewayProcess?.isAlive == true
        val current = mutableSnapshot.value
        val phase = when {
            running -> FreeLlmApiPhase.RUNNING
            serviceActive -> current.phase
            current.phase == FreeLlmApiPhase.ERROR -> FreeLlmApiPhase.ERROR
            installed -> FreeLlmApiPhase.INSTALLED
            else -> FreeLlmApiPhase.IDLE
        }
        mutableSnapshot.value = current.copy(
            phase = phase,
            installed = installed,
            running = running,
            message = when (phase) {
                FreeLlmApiPhase.IDLE -> "Install the gateway once; no Termux setup is needed"
                FreeLlmApiPhase.INSTALLING -> current.message
                FreeLlmApiPhase.INSTALLED -> "Installed · ready to start"
                FreeLlmApiPhase.STARTING -> "Starting local gateway…"
                FreeLlmApiPhase.RUNNING -> "Running on this device · 127.0.0.1:3001"
                FreeLlmApiPhase.ERROR -> current.message
            },
            error = if (phase == FreeLlmApiPhase.ERROR) current.error else null,
        )
    }

    fun isInstalled(context: Context): Boolean {
        val home = File(context.filesDir, "freellmapi/freellmapi")
        val marker = runCatching { File(home, ".mobile-harness-version").readText().trim() }.getOrNull()
        return marker == FreeLlmApiRuntimeConfig.PINNED_COMMIT &&
            File(home, "server/dist/index.js").isFile &&
            File(home, "client/dist/index.html").isFile
    }

    internal fun update(context: Context, snapshot: FreeLlmApiSnapshot) {
        mutableSnapshot.value = snapshot.copy(installed = isInstalled(context))
    }
}

class FreeLlmApiService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var operationJob: Job? = null
    private var installProcess: Process? = null
    private var serverProcess: Process? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val workspace by lazy { File(filesDir, "freellmapi").apply { mkdirs() } }
    private val installer by lazy { RuntimeInstaller(this) }

    override fun onCreate() {
        super.onCreate()
        ensureNotificationChannel()
        FreeLlmApiController.refresh(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopGateway()
            ACTION_INSTALL -> beginInstall()
            ACTION_START, null -> beginStart()
            else -> stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    private fun beginInstall() {
        if (operationJob?.isActive == true || serverProcess?.isAlive == true) return
        startForeground(NOTIFICATION_ID, notification("Preparing FreeLLMAPI installation"))
        acquireWakeLock()
        FreeLlmApiController.serviceActive = true
        FreeLlmApiController.update(
            this,
            FreeLlmApiSnapshot(phase = FreeLlmApiPhase.INSTALLING, message = "Checking the embedded Linux runtime…"),
        )
        operationJob = scope.launch {
            try {
                check(installer.isInstalled()) {
                    "Install the Mobile Harness Linux runtime first, then install FreeLLMAPI here."
                }
                val runtime = installer.installedRuntime()
                val script = File(workspace, ".mh-install-freellmapi.sh")
                assets.open("freellmapi/install.sh").use { input ->
                    script.outputStream().use { output -> input.copyTo(output) }
                }
                Os.chmod(script.absolutePath, 0b111101101)
                val log = File(workspace, "install.log")
                log.delete()
                val process = installer.process(
                    runtime.proot,
                    runtime.rootfs,
                    workspace,
                    emptyMap(),
                    listOf("/usr/bin/env", "bash", "/workspace/.mh-install-freellmapi.sh"),
                    emulateHardLinks = false,
                    outputFile = log,
                )
                installProcess = process
                FreeLlmApiController.update(
                    this@FreeLlmApiService,
                    FreeLlmApiSnapshot(
                        phase = FreeLlmApiPhase.INSTALLING,
                        message = "Downloading source, installing dependencies and building the dashboard…",
                    ),
                )
                withTimeout(INSTALL_TIMEOUT_MS) {
                    while (process.isAlive && isActive) {
                        updateInstallNotification(log)
                        delay(1_000)
                    }
                }
                check(!process.isAlive) { "FreeLLMAPI installation was interrupted." }
                check(process.exitValue() == 0) {
                    "FreeLLMAPI installation failed. " + log.readFreeLlmApiTailText(1_200).takeLast(800)
                }
                check(FreeLlmApiController.isInstalled(this@FreeLlmApiService)) {
                    "FreeLLMAPI build finished but its server/dashboard files are incomplete."
                }
                log.delete()
                script.delete()
                FreeLlmApiController.update(
                    this@FreeLlmApiService,
                    FreeLlmApiSnapshot(
                        phase = FreeLlmApiPhase.INSTALLED,
                        installed = true,
                        message = "Installed · ready to start",
                    ),
                )
                showResult("FreeLLMAPI installed", "The local gateway is ready. Start it or use the FreeLLMAPI provider preset.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                FreeLlmApiController.update(
                    this@FreeLlmApiService,
                    FreeLlmApiSnapshot(
                        phase = FreeLlmApiPhase.ERROR,
                        message = "Installation failed",
                        error = error.message?.take(500) ?: "Unknown installation error",
                    ),
                )
                showResult("FreeLLMAPI needs attention", error.message?.take(180) ?: "Installation failed")
            } finally {
                installProcess?.takeIf { it.isAlive }?.destroyForcibly()
                installProcess = null
                FreeLlmApiController.serviceActive = false
                FreeLlmApiController.refresh(this@FreeLlmApiService)
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun beginStart() {
        if (serverProcess?.isAlive == true || operationJob?.isActive == true) return
        startForeground(NOTIFICATION_ID, notification("Starting local FreeLLMAPI gateway"))
        acquireWakeLock()
        FreeLlmApiController.serviceActive = true
        FreeLlmApiController.update(
            this,
            FreeLlmApiSnapshot(phase = FreeLlmApiPhase.STARTING, message = "Starting local gateway…"),
        )
        operationJob = scope.launch {
            try {
                check(FreeLlmApiController.isInstalled(this@FreeLlmApiService)) {
                    "Install FreeLLMAPI once from Settings before starting the gateway."
                }
                val runtime = installer.installedRuntime()
                val encryptionKey = loadOrCreateEncryptionKey()
                val log = File(workspace, "server.log")
                log.delete()
                val process = installer.process(
                    runtime.proot,
                    runtime.rootfs,
                    workspace,
                    FreeLlmApiRuntimeConfig.serverEnvironment(encryptionKey),
                    listOf(
                        "/usr/bin/env",
                        "bash",
                        "-lc",
                        "mkdir -p /workspace/data && exec /usr/local/bin/node /workspace/freellmapi/server/dist/index.js",
                    ),
                    emulateHardLinks = false,
                    outputFile = log,
                )
                serverProcess = process
                FreeLlmApiController.gatewayProcess = process
                val ready = withTimeout(START_TIMEOUT_MS) {
                    while (isActive && process.isAlive) {
                        if (isHealthy()) return@withTimeout true
                        delay(450)
                    }
                    false
                }
                check(ready) {
                    val detail = log.readFreeLlmApiTailText(1_400).takeLast(900).ifBlank {
                        "The server exited before becoming ready."
                    }
                    "FreeLLMAPI did not start: " + detail
                }
                FreeLlmApiController.update(
                    this@FreeLlmApiService,
                    FreeLlmApiSnapshot(
                        phase = FreeLlmApiPhase.RUNNING,
                        installed = true,
                        running = true,
                        message = "Running on this device · 127.0.0.1:3001",
                    ),
                )
                updateRunningNotification()
                while (isActive && process.isAlive) delay(1_000)
                if (isActive) {
                    val code = runCatching { process.exitValue() }.getOrDefault(-1)
                    FreeLlmApiController.gatewayProcess = null
                    FreeLlmApiController.update(
                        this@FreeLlmApiService,
                        FreeLlmApiSnapshot(
                            phase = FreeLlmApiPhase.ERROR,
                            installed = true,
                            message = "Gateway stopped unexpectedly",
                            error = "FreeLLMAPI exited (code " + code + "). Start it again from Settings.",
                        ),
                    )
                    showResult("FreeLLMAPI stopped", "The gateway exited unexpectedly (code " + code + ").")
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                serverProcess?.takeIf { it.isAlive }?.destroyForcibly()
                serverProcess = null
                FreeLlmApiController.gatewayProcess = null
                FreeLlmApiController.update(
                    this@FreeLlmApiService,
                    FreeLlmApiSnapshot(
                        phase = FreeLlmApiPhase.ERROR,
                        installed = FreeLlmApiController.isInstalled(this@FreeLlmApiService),
                        message = "Gateway failed to start",
                        error = error.message?.take(500) ?: "Unknown startup error",
                    ),
                )
                showResult("FreeLLMAPI needs attention", error.message?.take(180) ?: "Gateway failed to start")
            } finally {
                if (FreeLlmApiController.snapshot.value.phase != FreeLlmApiPhase.RUNNING) {
                    FreeLlmApiController.gatewayProcess = null
                    serverProcess = null
                    FreeLlmApiController.serviceActive = false
                    FreeLlmApiController.refresh(this@FreeLlmApiService)
                    releaseWakeLock()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
    }

    private fun stopGateway() {
        operationJob?.cancel(CancellationException("Stopped by user"))
        installProcess?.takeIf { it.isAlive }?.destroyForcibly()
        serverProcess?.takeIf { it.isAlive }?.destroyForcibly()
        serverProcess = null
        installProcess = null
        FreeLlmApiController.gatewayProcess = null
        FreeLlmApiController.serviceActive = false
        FreeLlmApiController.refresh(this)
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun loadOrCreateEncryptionKey(): String {
        val vault = ApiKeyVault(this)
        vault.get(FreeLlmApiRuntimeConfig.ENCRYPTION_KEY_STORAGE_ID)?.let { return it }
        val database = File(workspace, "data/freeapi.db")
        check(!database.isFile || database.length() == 0L) {
            "The FreeLLMAPI encryption key is missing but its database still exists. Refusing to replace it and lose saved credentials."
        }
        val bytes = ByteArray(32).also(SecureRandom()::nextBytes)
        val generated = bytes.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        vault.put(FreeLlmApiRuntimeConfig.ENCRYPTION_KEY_STORAGE_ID, generated)
        return vault.get(FreeLlmApiRuntimeConfig.ENCRYPTION_KEY_STORAGE_ID)
            ?: error("Could not persist the FreeLLMAPI encryption key in Android Keystore storage.")
    }

    private fun isHealthy(): Boolean = isGatewayHealthy()

    private fun updateInstallNotification(log: File) {
        val marker = log.readFreeLlmApiTailText(4096).lineSequence()
            .map(String::trim)
            .lastOrNull { it.startsWith("[MH]") }
            ?.removePrefix("[MH]")?.trim()
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            notification(marker ?: "Installing FreeLLMAPI dependencies…"),
        )
    }

    private fun updateRunningNotification() {
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            notification("Gateway running · 127.0.0.1:3001"),
        )
    }

    private fun notification(detail: String): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            201,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getService(
            this,
            202,
            Intent(this, FreeLlmApiService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FreeLLMAPI · Mobile Harness")
            .setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setContentIntent(openApp)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, "Stop", stop)
            .build()
    }

    private fun showResult(title: String, detail: String) {
        val result = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(detail)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    203,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        getSystemService(NotificationManager::class.java).notify(RESULT_NOTIFICATION_ID, result)
    }

    private fun ensureNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "FreeLLMAPI gateway",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Local FreeLLMAPI install and gateway status" },
        )
    }

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.jarves.mh:freellmapi-gateway")
            .apply { acquire(MAX_WAKE_LOCK_MS) }
    }

    private fun releaseWakeLock() {
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        installProcess?.takeIf { it.isAlive }?.destroyForcibly()
        serverProcess?.takeIf { it.isAlive }?.destroyForcibly()
        FreeLlmApiController.gatewayProcess = null
        FreeLlmApiController.serviceActive = false
        FreeLlmApiController.refresh(this)
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_INSTALL = "com.jarves.mh.FREELLMAPI_INSTALL"
        const val ACTION_START = "com.jarves.mh.FREELLMAPI_START"
        const val ACTION_STOP = "com.jarves.mh.FREELLMAPI_STOP"
        private const val CHANNEL_ID = "freellmapi-gateway"
        private const val NOTIFICATION_ID = 61
        private const val RESULT_NOTIFICATION_ID = 62
        private const val INSTALL_TIMEOUT_MS = 50L * 60L * 1000L
        private const val START_TIMEOUT_MS = 50_000L
        private const val MAX_WAKE_LOCK_MS = 6L * 60L * 60L * 1000L

        fun install(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FreeLlmApiService::class.java).setAction(ACTION_INSTALL),
            )
        }

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FreeLlmApiService::class.java).setAction(ACTION_START),
            )
        }

        fun stop(context: Context) {
            context.startService(Intent(context, FreeLlmApiService::class.java).setAction(ACTION_STOP))
        }

        suspend fun ensureRunning(context: Context) {
            val appContext = context.applicationContext
            if (isGatewayHealthy()) return
            FreeLlmApiController.refresh(appContext)
            check(FreeLlmApiController.isInstalled(appContext)) {
                "Embedded FreeLLMAPI is not installed. Open Settings → FreeLLMAPI and install the gateway once."
            }
            start(appContext)
            withTimeout(START_TIMEOUT_MS + 10_000L) {
                while (!isGatewayHealthy()) {
                    val current = FreeLlmApiController.snapshot.value
                    if (current.phase == FreeLlmApiPhase.ERROR) {
                        error(current.error ?: "FreeLLMAPI could not start.")
                    }
                    delay(500)
                }
            }
        }

        private fun isGatewayHealthy(): Boolean = runCatching {
            val connection = URL(FreeLlmApiRuntimeConfig.API_URL + "/api/ping")
                .openConnection(Proxy.NO_PROXY) as HttpURLConnection
            connection.connectTimeout = 800
            connection.readTimeout = 800
            connection.requestMethod = "GET"
            try {
                connection.responseCode == 200
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(false)
    }
}

private fun File.readFreeLlmApiTailText(maxBytes: Int): String = runCatching {
    if (!isFile || length() <= 0L) return@runCatching ""
    val bytes = minOf(length(), maxBytes.toLong()).toInt()
    RandomAccessFile(this, "r").use { file ->
        file.seek(file.length() - bytes)
        ByteArray(bytes).also(file::readFully).toString(Charsets.UTF_8)
    }
}.getOrDefault("")
