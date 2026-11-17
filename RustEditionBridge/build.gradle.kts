plugins {
    alias(libs.plugins.android.library)
}

// Linked into TCL only for explicitly enabled experimental Rust builds.
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

val runtimePath = providers.gradleProperty("rustRuntimeDir")
    .orElse(rootProject.layout.projectDirectory.dir("rust-edition/runtime").asFile.absolutePath)
androidComponents {
    onVariants { variant ->
        variant.sources.jniLibs?.addStaticSourceDirectory(file("${runtimePath.get()}/jniLibs").absolutePath)
        variant.sources.assets?.addStaticSourceDirectory(file("${runtimePath.get()}/assets").absolutePath)
    }
}
