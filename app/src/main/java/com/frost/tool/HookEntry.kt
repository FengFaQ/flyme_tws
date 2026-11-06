package com.frost.tool

import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import com.frost.tool.hook.TWS.BluetoothHook
import com.frost.tool.hook.TWS.EarbudHook
import com.frost.tool.hook.TWS.MeizuTWS
import com.frost.tool.hook.TWS.MeizuiotTWS

/**
 * Logical entry point: dispatches each target process to its hook set.
 *
 * The concrete wiring (which class the host framework instantiates, and how a
 * `PackageReadyParam` is delivered) is host-specific — modern libxposed uses a
 * module service declared via manifest metadata, classic Xposed uses
 * `assets/xposed_init`. This object only centralises dispatch; every hook class
 * above is compiled into the APK regardless of being referenced here.
 */
object HookEntry {

    fun onPackageLoaded(module: XposedModule, param: PackageReadyParam) {
        val marker = param.classLoader.toString()
        when {
            "bluetooth" in marker -> BluetoothHook.hook(module, param)
            "meizu.earphone" in marker -> MeizuTWS.hook(module, param)
            "meizuiot" in marker -> MeizuiotTWS.hook(module, param)
            "systemui" in marker -> EarbudHook.hook(module, param)
        }
    }
}
