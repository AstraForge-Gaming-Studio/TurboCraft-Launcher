#!/usr/bin/env python3
"""Fail a build if its APK is missing the verified Rust runtime or setup assets."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile
from stage_runtime import ABIS, ASSETS, LIBRARIES, REVISION


def verify(apk, runtime, abis):
    with zipfile.ZipFile(apk) as archive:
        for abi in abis:
            manifest = json.loads((runtime / f'runtime-{abi}.json').read_text())
            if manifest.get('revision') != REVISION or manifest.get('abi') != abi:
                raise ValueError(f'Bad provenance for {abi}')
            paths = [f'jniLibs/{abi}/{lib}' for lib in LIBRARIES] + [f'assets/{asset}' for asset in ASSETS]
            for path in paths:
                name = path.replace('jniLibs/', 'lib/', 1)
                if archive.namelist().count(name) != 1:
                    raise ValueError(f'Missing or duplicate APK entry: {name}')
                actual = hashlib.sha256(archive.read(name)).hexdigest()
                if actual != manifest['sha256'].get(path):
                    raise ValueError(f'APK runtime hash mismatch: {name}')
    print(f'Verified {apk}: {", ".join(abis)}')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('apk', type=Path)
    parser.add_argument('runtime', type=Path)
    parser.add_argument('abis', nargs='+', choices=ABIS)
    args = parser.parse_args()
    verify(args.apk, args.runtime, args.abis)


if __name__ == '__main__':
    main()
