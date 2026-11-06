// Compile-only stubs for the LSPosed / libxposed framework API (provided at
// runtime by the host, never packaged into the APK). Keeping them in a separate
// :xposed-api JVM module keeps the app's runtime dependency graph clean.
//
// No explicit JVM toolchain is declared: the CI runner supplies JDK 17 via
// setup-java, and Kotlin's default bytecode target is readable by AGP.
plugins {
    id("org.jetbrains.kotlin.jvm")
}
