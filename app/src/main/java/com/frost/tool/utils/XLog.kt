@file:Suppress("UNUSED_PARAMETER")

package com.frost.tool.utils

import android.util.Log
import io.github.libxposed.api.XposedModule

/** Logging helper used by the TWS hooks. */
object XLog {
    fun e(module: XposedModule?, tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun e(module: XposedModule?, tag: String, msg: String, tr: Throwable) {
        Log.e(tag, msg, tr)
    }
}
