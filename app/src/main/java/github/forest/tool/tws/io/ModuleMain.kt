package github.forest.tool.tws.io

import android.util.Log
import github.forest.tool.tws.io.hook.TWS.BluetoothHook
import github.forest.tool.tws.io.hook.TWS.EarbudHook
import github.forest.tool.tws.io.hook.TWS.MeizuTWS
import github.forest.tool.tws.io.hook.TWS.MeizuiotTWS
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * Real libxposed (LSPosed modern Xposed API) module entry.
 *
 * Declared in `META-INF/xposed/java_init.list`; the framework instantiates this
 * class once per module generation in every process listed in
 * `META-INF/xposed/scope.list`, attaches the framework interface, then delivers
 * lifecycle callbacks. All hook registration happens here via the REAL API.
 */
class ModuleMain : XposedModule() {

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "flyme_tws loaded in ${param.processName}; " +
                "framework=$frameworkName api=$apiVersion")
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (!ModuleSettings.isSpoofEnabled(this)) {
            log(Log.INFO, TAG, "disabled for ${param.packageName}")
            return
        }
        try {
            when (param.packageName) {
                "com.android.bluetooth" -> BluetoothHook.hook(this, param)
                "com.meizu.earphone" -> MeizuTWS.hook(this, param)
                "com.meizu.meizuiot" -> MeizuiotTWS.hook(this, param)
                "com.android.systemui" -> EarbudHook.hook(this, param)
                else -> Unit
            }
            log(Log.INFO, TAG, "hooks installed for ${param.packageName}")
        } catch (t: Throwable) {
            log(Log.ERROR, TAG, "failed to install hooks for ${param.packageName}", t)
        }
    }

    private companion object {
        const val TAG = "MzfpHook"
    }
}
