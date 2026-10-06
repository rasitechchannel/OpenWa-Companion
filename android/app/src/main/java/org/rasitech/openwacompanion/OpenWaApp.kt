package org.rasitech.openwacompanion

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.rasitech.openwacompanion.data.repo.OpenWaRepository
import org.rasitech.openwacompanion.engine.BridgeEventIngester
import org.rasitech.openwacompanion.engine.EngineForegroundService
import org.rasitech.openwacompanion.security.CredentialVault

class OpenWaApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var repository: OpenWaRepository
        private set
    lateinit var vault: CredentialVault
        private set
    lateinit var ingester: BridgeEventIngester
        private set

    override fun onCreate() {
        super.onCreate()
        repository = OpenWaRepository(this)
        vault = CredentialVault(this)
        ingester = BridgeEventIngester(this)
        ensureNotificationChannels()
        appScope.launch {
            repository.ensureAccount("default")
        }
        ingester.start(appScope)
    }

    private fun ensureNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val sync = NotificationChannel(
            EngineForegroundService.CHANNEL_ID,
            getString(R.string.sync_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { description = getString(R.string.sync_channel_description) }
        val privacy = NotificationChannel(
            "openwa_privacy",
            "Privacy",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "Local privacy and session notices" }
        manager.createNotificationChannel(sync)
        manager.createNotificationChannel(privacy)
    }
}
