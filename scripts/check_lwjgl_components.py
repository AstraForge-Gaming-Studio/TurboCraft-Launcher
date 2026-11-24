#!/usr/bin/env python3
"""Check the committed LWJGL components before any build can overwrite them."""
from pathlib import Path
from zipfile import ZipFile

root = Path(__file__).resolve().parent.parent / "TCL/src/main/assets/app_runtime/lwjgl"
for version in ("3.3.3", "3.4.1"):
    directory = root / version
    jar = directory / f"lwjgl-{version}-merged-modules.jar"
    with ZipFile(jar) as archive:
        if archive.testzip() is not None:
            raise ValueError(f"Corrupt LWJGL {version} jar")
        glfw = archive.read("org/lwjgl/glfw/GLFW.class")
        for symbol in (b"gwResetEventWait", b"gwStopEventWait", b"gwWaitEvents", b"gwPostEmptyEvent"):
            if symbol not in glfw:
                raise ValueError(f"LWJGL {version} missing GLFW event wait symbol {symbol!r}")
        archive.getinfo("org/lwjgl/glfw/GLFW$EventWaitError.class")
        archive.getinfo("org/lwjgl/openal/jsound/JSoundProvider.class")
        provider = archive.read("META-INF/services/javax.sound.sampled.spi.MixerProvider")
        if b"org.lwjgl.openal.jsound.JSoundProvider" not in provider:
            raise ValueError(f"LWJGL {version} JSound service registration missing")
    if not (directory / "version").read_text().strip():
        raise ValueError(f"LWJGL {version} component version is empty")
    print(f"LWJGL {version}: packaged GLFW and JSound verified")
