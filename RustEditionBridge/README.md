# Rust edition Android bridge (experimental)

This module contains Java/JNI bridge code based on
[Starling](https://github.com/starling-client/starling), commit
`5432370d573642041585c69b40fb045b27133488`. Names are retained for native JNI
lookup. Local integration changes isolate data and processes, disable upstream
APK updating and downloaded native mod builds, and register internal Android
components. The MIT license and third-party notices are included here.

The launcher links this module only when `-PrustEdition=true` is explicitly
provided, and the required per-ABI native libraries and setup assets have been
staged. See `rust-edition/README.md` for build instructions and limitations.
An isolated bridge compile alone does not validate gameplay or setup.
