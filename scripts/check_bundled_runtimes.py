#!/usr/bin/env python3
"""Check that bundled Java 17/21 archives form complete, matching ABI sets."""
from pathlib import Path
import re
import tarfile

ROOT = Path(__file__).resolve().parent.parent / "TCL/src/main/assets/runtimes"
ARCHES = {
    "arm": ("arm", 1, 40),
    "arm64": ("aarch64", 2, 183),
    "x86": ("x86", 1, 3),
    "x86_64": ("x86_64", 2, 62),
}


def member(tar, name):
    return next((entry for entry in tar if entry.name.removeprefix("./") == name), None)


for major in (17, 21):
    directory = ROOT / f"jre-{major}"
    version = (directory / "version").read_text().strip()
    if not version.startswith(f"{major}."):
        raise ValueError(f"Unexpected JRE {major} version: {version}")
    with tarfile.open(directory / "universal.tar.xz") as tar:
        if member(tar, "lib/modules") is None:
            raise ValueError(f"JRE {major} universal archive lacks lib/modules")
    for arch, (expected_arch, elf_class, machine) in ARCHES.items():
        with tarfile.open(directory / f"bin-{arch}.tar.xz") as tar:
            entries = {entry.name.removeprefix("./"): entry for entry in tar}
            release = tar.extractfile(entries["release"]).read().decode("utf-8")
            def property_value(key):
                match = re.search(rf'^{key}="([^"]+)"$', release, re.MULTILINE)
                if match is None:
                    raise ValueError(f"JRE {major} {arch} missing {key}")
                return match.group(1)
            if property_value("JAVA_VERSION") != version or property_value("OS_ARCH") != expected_arch:
                raise ValueError(f"JRE {major} {arch} release metadata mismatch")
            header = tar.extractfile(entries["lib/server/libjvm.so"]).read(20)
            if (header[:4] != b"\x7fELF" or header[4] != elf_class or header[5] != 1
                    or int.from_bytes(header[18:20], "little") != machine):
                raise ValueError(f"JRE {major} {arch} libjvm ELF architecture mismatch")
        print(f"JRE {major}: {version}, {arch} verified")
