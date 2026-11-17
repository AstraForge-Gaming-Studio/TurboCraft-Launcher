# Experimental Rust edition integration

This is an optional integration of [Starling](https://github.com/starling-client/starling)
(commit `5432370d573642041585c69b40fb045b27133488`), an unofficial Horizon-based
native Rust client targeting Minecraft Java 26.3. It is not made by Mojang or
Microsoft and does not replace the existing Java launcher. Java mods and TCL
control layouts are not supported. The Rust client has its own sign-in and setup;
it requires the player's own Minecraft Java resources and does not bundle them.

**Status:** The ARM64 native engine and isolated bridge have built on CI.
The ARM64 integrated debug APK has compiled and its staged runtime has been
verified. First-run setup, authentication, rendering, input, restart and save
handling have not yet been verified on a device; other architectures need CI
and device verification. Do not represent this as tested gameplay.

## Experimental build

Use an unmodified Starling checkout at the pinned commit, and install its Android
build dependencies (including SDK build-tools 35.0.0, platform 36, NDK
28.2.13676358, Rust, cargo-ndk, Python and JDK 25). For each ABI required:

```sh
./rust-edition/build-engine.sh /path/to/starling arm64-v8a /tmp/starling-proof
python3 /path/to/starling/packaging/android/build_apk.py --abi arm64-v8a --profile quick --no-build
python3 rust-edition/stage_runtime.py /path/to/starling/target/android/starling-0.8.0-arm64-v8a.apk arm64-v8a /tmp/starling-proof rust-edition/runtime
```

The staging script checks the APK engine against the locally verified library,
requires the native Java launchers and all setup assets, and writes per-file
hashes. Stage all four ABIs for a universal APK, or the appropriate ABI for a
split. Then switch to TCL's build prerequisites (including JDK 21 and NDK
25.2.9519653 for the other TCL modules, plus NDK 28.2.13676358 for the
experimental app module) and build, for example:

```sh
bash ./gradlew :TCL:assembleDebug -Darch=arm64
```

Rust is enabled by default; Gradle rejects missing or altered staged files rather
than silently producing an APK without the edition. Set `-PrustEdition=false`
only for an explicitly Java-only development build. The shared build workflow
builds and stages the pinned runtime for all ABIs, packages it, and checks each
APK. A branch push must not publish a release. Gradle rejects missing or altered staged files. `rustRuntimeDir` may be set to
an alternate absolute directory using `-PrustRuntimeDir=/path/to/runtime`.
The GitHub `rust-engine-check.yml` workflow also builds and checks an experimental
ABI-specific debug APK without publishing a release. Packaging validation is not
a substitute for device testing. Runtime artifacts are ignored by Git; never
commit Minecraft resources, generated assets, or APKs.

The bridge runs the native game in its own process, uses a private Rust data
folder, and disables upstream APK updates and downloaded native mod builds.
Upstream MIT license and third-party notices are in `RustEditionBridge/` and
must accompany distributions. Do not install such a build over a production
installation without appropriate backups and further validation.
