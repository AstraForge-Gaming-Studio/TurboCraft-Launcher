plugins {
    alias(libs.plugins.android.library)
}

// Isolated, compile-only Android bridge. Not yet a dependency of the launcher.
android {
    namespace = "io.github.juanluci.starling"
    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
