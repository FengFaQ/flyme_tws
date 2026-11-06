package github.forest.tool.tws.io

import io.github.libxposed.api.XposedModule

/**
 * Reads the module's remote preferences (LSPosed framework-backed
 * SharedPreferences, see getRemotePreferences()). These are populated by the
 * module's own launcher UI via io.github.libxposed:service, so this is a REAL
 * runtime switch, not a compile-time constant.
 *
 * Default is TRUE: a freshly enabled module must actually attach its hooks.
 */
object ModuleSettings {
    const val PREFS_GROUP = "frost_tool_prefs"
    const val KEY_SPOOF_ENABLED = "tws_device_spoof_enabled"

    fun isSpoofEnabled(module: XposedModule): Boolean =
        try {
            module.getRemotePreferences(PREFS_GROUP)
                .getBoolean(KEY_SPOOF_ENABLED, true)
        } catch (t: Throwable) {
            true
        }
}
