package github.forest.tool.tws.io

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Binds to the real libxposed XposedService so the launcher UI can write the
 * module's remote preferences (which the hook process reads via
 * XposedModule.getRemotePreferences). Mirrors libxposed/example's App class.
 */
class App : Application(), XposedServiceHelper.OnServiceListener {

    interface ServiceStateListener {
        fun onServiceStateChanged(service: XposedService?)
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        setService(service)
        listeners.forEach { it.onServiceStateChanged(service) }
    }

    override fun onServiceDied(service: XposedService) {
        setService(null)
        listeners.forEach { it.onServiceStateChanged(null) }
    }

    companion object {
        @Volatile
        var service: XposedService? = null
            private set

        private val listeners = CopyOnWriteArraySet<ServiceStateListener>()

        private fun setService(s: XposedService?) {
            service = s
        }

        fun addListener(l: ServiceStateListener, notifyNow: Boolean) {
            listeners.add(l)
            if (notifyNow) l.onServiceStateChanged(service)
        }

        fun removeListener(l: ServiceStateListener) {
            listeners.remove(l)
        }
    }
}
