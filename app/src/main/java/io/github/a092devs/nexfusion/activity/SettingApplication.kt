package io.github.a092devs.nexfusion.activity

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import io.github.a092devs.nexfusion.core.CacheManager
import io.github.a092devs.nexfusion.updates.UpdateNotifications
import io.github.a092devs.nexfusion.updates.UpdateWorker
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.Volatile

class SettingApplication : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        @Volatile
        var mService: XposedService? = null
            private set
        private val serviceStateListeners =
            CopyOnWriteArraySet<ServiceStateListener>()

        private fun dispatchServiceState(
            listener: ServiceStateListener,
            service: XposedService?
        ) {
            if (serviceStateListeners.contains(listener)) {
                listener.onServiceStateChanged(service)
            }
        }

        fun addServiceStateListener(
            listener: ServiceStateListener,
            notifyImmediately: Boolean
        ) {
            serviceStateListeners.add(listener)
            if (notifyImmediately) {
                dispatchServiceState(listener, mService)
            }
        }

        fun removeServiceStateListener(listener: ServiceStateListener) {
            serviceStateListeners.remove(listener)
        }
    }

    private fun notifyServiceStateChanged(service: XposedService?) {
        for (listener in serviceStateListeners) {
            dispatchServiceState(listener, service)
        }
    }

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(io.github.a092devs.nexfusion.core.AppLocale.wrap(base))
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
        UpdateNotifications.createChannels(this)
        UpdateWorker.schedule(this)
        if (io.github.a092devs.nexfusion.updates.AppSettings(this).autoUpdatePatches) {
            UpdateWorker.checkNow(this)
        }
        CacheManager.sanitizeAndPruneAsync(this)
    }

    interface ServiceStateListener {
        fun onServiceStateChanged(service: XposedService?)
    }

    override fun onServiceBind(service: XposedService) {
        mService = service
        notifyServiceStateChanged(mService)
    }

    override fun onServiceDied(service: XposedService) {
        mService = null
        notifyServiceStateChanged(mService)
    }
}