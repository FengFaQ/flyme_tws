package io.github.libxposed.api

/* 
 * Compile-only counterpart of the framework's module interface. Only the
 * [PackageReadyParam] used by the hooks is modelled here.
 */
class XposedModuleInterface {

    interface PackageReadyParam {
        val classLoader: ClassLoader
    }
}
