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
    val runtimePath = providers.gradleProperty("rustRuntimeDir")
        .orElse(rootProject.layout.projectDirectory.dir("rust-edition/runtime").asFile.absolutePath)
    sourceSets.getByName("main") {
        jniLibs.srcDir(file("${runtimePath.get()}/jniLibs"))
        assets.srcDir(file("${runtimePath.get()}/assets"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
