package github.forest.tool.tws.io.utils

import android.util.Log
import io.github.libxposed.api.XposedModule

/**
 * Logging helper for the TWS hooks. Uses the REAL libxposed framework logger
 * (module.log), which LSPosed forwards to the module log; falls back to
 * android.util.Log if the framework logger is unavailable.
 */
object XLog {
    fun e(module: XposedModule?, tag: String, msg: String) {
        try {
            if (module != null) module.log(Log.ERROR, tag, msg) else Log.e(tag, msg)
        } catch (t: Throwable) {
            Log.e(tag, msg)
        }
    }

    fun e(module: XposedModule?, tag: String, msg: String, tr: Throwable) {
        try {
            if (module != null) module.log(Log.ERROR, tag, msg, tr) else Log.e(tag, msg, tr)
        } catch (t: Throwable) {
            Log.e(tag, msg, tr)
        }
    }
}
