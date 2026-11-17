#!/usr/bin/env python3
"""Stage the exact Android native libraries and setup assets from a locally built Starling APK.

Use only an APK built from the pinned upstream checkout by build-engine.sh. This
is a build-time integration helper, not an APK installer or an updater.
"""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import tempfile
import zipfile

ABIS = ("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
LIBRARIES = ("libhorizon_android.so", "libstarling_java.so", "libstarling_javac.so", "libc++_shared.so")
ASSETS = ("prepare-kit.zip", "lowbudget.zip", "StarlingVisuals.zip", "diagnostic-fonts/Geist-Regular.ttf", "diagnostic-fonts/Geist-SemiBold.ttf")
REVISION = "5432370d573642041585c69b40fb045b27133488"


def equivalent_assets(first, second):
    """ZIP timestamps can differ across checkouts; compare their actual content."""
    if first.name.endswith(".zip"):
        with zipfile.ZipFile(first) as a, zipfile.ZipFile(second) as b:
            names = a.namelist()
            return (len(names) == len(set(names)) and len(b.namelist()) == len(set(b.namelist()))
                    and set(names) == set(b.namelist())
                    and all(a.read(name) == b.read(name) for name in names))
    return first.read_bytes() == second.read_bytes()


def stage(apk, abi, proof, destination):
    if abi not in ABIS:
        raise ValueError(f"Unsupported ABI: {abi}")
    info = (proof / "build-info.txt").read_text()
    if f"revision={REVISION}\n" not in info or f"abi={abi}\n" not in info:
        raise ValueError("Engine build identity does not match the requested ABI/revision")
    engine = (proof / "libhorizon_android.so").read_bytes()
    if not engine or len(engine) > 300_000_000:
        raise ValueError("Invalid engine build")
    entries = {f"lib/{abi}/{name}": Path("jniLibs") / abi / name for name in LIBRARIES}
    entries.update({f"assets/{name}": Path("assets") / name for name in ASSETS})
    # Never extract arbitrary paths or allow duplicate names / zip bombs.
    with zipfile.ZipFile(apk) as archive:
        for name in entries:
            matches = [item for item in archive.infolist() if item.filename == name]
            if len(matches) != 1 or matches[0].file_size <= 0 or matches[0].file_size > 300_000_000:
                raise ValueError(f"Missing, duplicated or oversized runtime file: {name}")
        if archive.read(f"lib/{abi}/libhorizon_android.so") != engine:
            raise ValueError("APK engine does not match the verified build")
        destination.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory(dir=destination) as temporary:
            temp = Path(temporary)
            hashes = {}
            for name, relative in entries.items():
                data = archive.read(name)
                target = temp / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(data)
                hashes[str(relative)] = hashlib.sha256(data).hexdigest()
            # Asset contents must agree across ABIs. A mismatch is a hard error.
            for relative in entries.values():
                if relative.parts[0] == "assets":
                    existing = destination / relative
                    if existing.exists() and not equivalent_assets(existing, temp / relative):
                        raise ValueError(f"Assets differ between ABI builds: {relative}")
            for relative in entries.values():
                target = destination / relative
                target.parent.mkdir(parents=True, exist_ok=True)
                if relative.parts[0] != "assets" or not target.exists():
                    shutil.copyfile(temp / relative, target)
                hashes[str(relative)] = hashlib.sha256(target.read_bytes()).hexdigest()
            (destination / f"runtime-{abi}.json").write_text(json.dumps({"revision": REVISION, "abi": abi, "sha256": hashes}, indent=2) + "\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("abi", choices=ABIS)
    parser.add_argument("proof", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    stage(args.apk, args.abi, args.proof, args.destination)


if __name__ == "__main__":
    main()
