@file:Suppress("UNUSED_PARAMETER", "UNCHECKED_CAST")

package io.github.libxposed.api

import java.lang.reflect.Method

/**
 * Minimal compile-only representation of the LSPosed / libxposed framework API.
 *
 * The real implementation is supplied by the Xposed host at runtime; these
 * declarations exist only so the module sources type-check during an APK build.
 * They MUST stay `compileOnly` — bundling them would shadow the framework.
 */
open class XposedModule {

    /** Begin hooking a resolved method. */
    fun hook(method: Method): HookBuilder = HookBuilder()

    /** Read the module's remote (cross-process) preferences. */
    fun getRemotePreferences(name: String): RemotePreferences = RemotePreferences()
}

/*  Fluent builder returned by [XposedModule.hook]. */
class HookBuilder {
    fun intercept(callback: (HookChain) -> Any?) {
        // no-op at compile time; registered by the framework at runtime
    }
}

/**
 * Invocation context passed to an intercept lambda. Mirrors the classic
 * "method hook param" shape: receiver, arguments and a proceed() call.
 */
class HookChain(
    val thisObject: Any?,
    val args: Array<Any?>,
) {
    // Array<*> (not Array<Any?>) because Kotlin arrays are invariant and callers
    // pass Array<Any> from MutableList<Any>.toTypedArray().
    fun proceed(newArgs: Array<*> = emptyArray<Any?>()): Any? = null
}

/** Tiny subset of SharedPreferences used by the module. */
class RemotePreferences {
    fun getBoolean(key: String, defValue: Boolean): Boolean = defValue
}
