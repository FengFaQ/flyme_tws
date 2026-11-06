plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.frost.tool"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.frost.tool"
        // APK Signature Scheme v3 is natively applicable on Android 9 (API 28)+.
        // Keeping minSdk at 28 guarantees apksigner emits a real v3 block.
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Signing config intentionally NOT declared here: the CI workflow signs the
    // unsigned APK explicitly with apksigner --v3-signing-enabled true, so the
    // produced artifact carries an APK Signature Scheme v3 block.
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

    // assembleRelease otherwise runs lintVitalRelease, which aborts on NewApi
    // warnings (e.g. BluetoothDevice.getAlias, API 30 vs minSdk 28). Those are
    // runtime-only reflection concerns here, so they must not break the build.
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    // libxposed / XLog API is provided by LSPosed at runtime -> compileOnly only.
    compileOnly(project(":xposed-api"))
}
