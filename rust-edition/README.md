# Rust edition integration: engine build prototype

TurboCraft currently launches Minecraft's Java client through `VMActivity` and
`GameLauncher`. Horizon/Starling is an unofficial native Rust client, not a
Minecraft edition or a Java mod. This directory starts a separate integration:
it builds the upstream Android engine at a pinned commit and verifies its
NativeActivity entry point. It does **not** add a playable edition yet.

Upstream: <https://github.com/starling-client/starling>, commit
`5432370d573642041585c69b40fb045b27133488`. The upstream project is MIT
licensed; its license and third-party notices must accompany any distribution.
Do not commit upstream APKs, game files, generated assets or build artifacts.

## Build check

The `Rust edition engine build check` GitHub Actions workflow builds ARM64
when the Rust integration changes on an Arena branch. It can also be manually
dispatched for a selected ABI after the workflow exists on the default branch.
The resulting library, build identity, SHA-256 and upstream notices are kept as
a short-lived CI artifact. It is not part of TCL's release pipeline.

For a local check, obtain an unmodified checkout of the pinned commit and the
upstream Android build dependencies, including Android SDK, NDK 28.2.13676358,
Rust, cargo-ndk, Python and JDK. Then run:

```sh
./rust-edition/build-engine.sh /path/to/starling arm64-v8a /path/to/output
```

## What remains before users can play

- Integrate the upstream Android NativeActivity / JNI bridge and its lifecycle
  in a separate game process, without routing it through the JVM launcher.
- Build and package the engine for each supported ABI, plus all upstream
  preparation assets, native Java launchers, required Android code and notices;
  the isolated library from this check is **not sufficient** for gameplay.
- Implement verified acquisition and preparation of game resources from the
  user's own Java Edition files / official sources, with progress and errors.
  Do not redistribute Minecraft assets.
- Add an explicit experimental edition selection and keep Rust game files,
  account handling, worlds and settings separate until compatibility is
  validated. Do not assume Java mods or TCL control layouts work with Rust.
- Test installation, first-run preparation, input, authentication, rendering,
  process exit and save safety on physical devices and supported ABIs.

The engine targets Minecraft Java 26.3, not arbitrary Java version/mod-loader
combinations. It is not affiliated with Mojang or Microsoft; compatibility and
performance must not be claimed without device testing.
