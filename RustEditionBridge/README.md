# Rust edition Android bridge (experimental)

This isolated Android library contains the Java/JNI bridge from
[Starling](https://github.com/starling-client/starling), commit
`5432370d573642041585c69b40fb045b27133488`, and its required Java resources.
The original source is kept unchanged, including its package names because the
native library looks up JNI symbols by those names. Its MIT license and
third-party notices are in this directory.

This is **not connected to the TurboCraft app** and its manifest deliberately
registers no activities, services or providers. It cannot launch the game yet.
In particular, do not add it to an APK until the engine, prepared assets,
separate-process lifecycle, permissions, account handling, providers, native
updates and game-data isolation have been reviewed and tested together. The
upstream updater and native-mod functionality must not silently replace the
TurboCraft app or run code without the user's informed choice.

Compile the isolated bridge with `./gradlew :RustEditionBridge:assembleDebug`.
The separate `rust-engine-check.yml` workflow verifies it and builds the pinned
native engine independently; no Minecraft game files are shipped.
