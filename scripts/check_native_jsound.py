#!/usr/bin/env python3
"""Verify every ABI in an APK ships the matching native Java Sound bridge."""
import io
import subprocess
import sys
import tempfile
from pathlib import Path
from zipfile import ZipFile

APK, variant = Path(sys.argv[1]), sys.argv[2]
abis = {
    "arm": {"armeabi-v7a": (1, 40)},
    "arm64": {"arm64-v8a": (2, 183)},
    "x86": {"x86": (1, 3)},
    "x86_64": {"x86_64": (2, 62)},
}
expected = {abi: details for group in abis.values() for abi, details in group.items()} if variant == "all" else abis[variant]
required = (
    "Java_com_sun_media_sound_DirectAudioDeviceProvider_nGetNumDevices",
    "Java_com_sun_media_sound_Platform_nIsSigned8",
    "Java_com_sun_media_sound_Platform_nGetExtraLibraries",
    "Java_com_sun_media_sound_Platform_nGetLibraryForFeature",
)

with ZipFile(APK) as apk:
    for abi, (elf_class, machine) in expected.items():
        data = apk.read(f"lib/{abi}/libjsound.so")
        if data[:4] != b"\x7fELF" or data[4] != elf_class or data[5] != 1 or int.from_bytes(data[18:20], "little") != machine:
            raise ValueError(f"{abi}: wrong libjsound ELF architecture")
        if f"lib/{abi}/libopenal.so" not in apk.namelist():
            raise ValueError(f"{abi}: libopenal.so is missing")
        with tempfile.NamedTemporaryFile() as lib:
            lib.write(data)
            lib.flush()
            symbols = subprocess.check_output(["readelf", "--dyn-syms", "-W", lib.name], text=True)
        for symbol in required:
            if not any(line.split()[-1:] == [symbol] and " UND " not in line for line in symbols.splitlines()):
                raise ValueError(f"{abi}: native Java Sound symbol {symbol} is missing")
        print(f"{abi}: native Java Sound, JDK 8 compatibility and OpenAL verified")
    for version in ("3.3.3", "3.4.1"):
        path = f"assets/app_runtime/lwjgl/{version}/lwjgl-{version}-merged-modules.jar"
        with ZipFile(io.BytesIO(apk.read(path))) as jar:
            if any(name.startswith("org/lwjgl/openal/jsound/") for name in jar.namelist()):
                raise ValueError(f"LWJGL {version}: old Java JSound provider is still bundled")
            if "META-INF/services/javax.sound.sampled.spi.MixerProvider" in jar.namelist():
                raise ValueError(f"LWJGL {version}: old Java JSound service still registered")
        print(f"LWJGL {version}: no competing Java Sound provider")
