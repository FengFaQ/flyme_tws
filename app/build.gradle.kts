plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "github.forest.tool.tws.io"
    compileSdk = 34

    defaultConfig {
        applicationId = "github.forest.tool.tws.io"
        // APK Signature Scheme v3 is natively applicable on Android 9 (API 28)+.
        // Keeping minSdk at 28 guarantees apksigner emits a real v3 block.
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Signing is done by the CI workflow with apksigner --v3-signing-enabled.
    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    // Keep META-INF/xposed/* module metadata merged into the APK.
    packaging {
        resources {
            merges += "META-INF/xposed/*"
        }
    }
}

dependencies {
    // ---- REAL LSPosed modern Xposed API (compileOnly; supplied by the framework
    // at runtime, NEVER bundled into the APK). Extracted from the OFFICIAL
    // published artifacts io.github.libxposed:api:102.0.0 / annotation:1.0.0.
    // The AARs declare minCompileSdk=37 (a metadata restriction), but their
    // classes only reference Android types present since API 28, so the plain
    // classes.jar is safe to compile against with compileSdk 34.
    compileOnly(files("libs/libxposed-api-102.0.0.jar"))
    compileOnly(files("libs/libxposed-annotation-1.0.0.jar"))

    // ---- REAL libxposed service: lets the launcher UI write the module's
    // remote preferences (the enable switch). Bundled, per official example.
    implementation(files("libs/libxposed-service-102.0.0.jar"))
    implementation(files("libs/libxposed-interface-102.0.0.jar"))

    // Annotations referenced by the service jar (compile-time only, CLASS retention).
    compileOnly("androidx.annotation:annotation:1.7.1")
}
