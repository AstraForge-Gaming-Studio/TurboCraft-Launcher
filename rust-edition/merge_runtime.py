#!/usr/bin/env python3
"""Combine per-architecture CI outputs, checking identity and shared asset content."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
from stage_runtime import ABIS, ASSETS, LIBRARIES, REVISION, equivalent_assets


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def merge(inputs, destination, abis):
    if not abis or len(set(abis)) != len(abis) or any(abi not in ABIS for abi in abis):
        raise ValueError('Invalid ABI selection')
    destination.mkdir(parents=True, exist_ok=True)
    for abi in abis:
        root = inputs / f'rust-runtime-{abi}'
        manifest = json.loads((root / f'runtime-{abi}.json').read_text())
        if manifest.get('revision') != REVISION or manifest.get('abi') != abi:
            raise ValueError(f'Invalid provenance for {abi}')
        paths = [f'jniLibs/{abi}/{lib}' for lib in LIBRARIES] + [f'assets/{asset}' for asset in ASSETS]
        hashes = manifest.get('sha256', {})
        if set(hashes) != set(paths):
            raise ValueError(f'Invalid runtime manifest for {abi}')
        for path in paths:
            source = root / path
            if not source.is_file() or digest(source) != hashes[path]:
                raise ValueError(f'Invalid runtime file: {path}')
            target = destination / path
            if path.startswith('assets/') and target.exists():
                if not equivalent_assets(target, source):
                    raise ValueError(f'Assets differ between ABI builds: {path}')
            else:
                target.parent.mkdir(parents=True, exist_ok=True)
                shutil.copyfile(source, target)
            hashes[path] = digest(target)
        (destination / f'runtime-{abi}.json').write_text(json.dumps(manifest, indent=2) + '\n')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('inputs', type=Path)
    parser.add_argument('destination', type=Path)
    parser.add_argument('abis', nargs='+', choices=ABIS)
    args = parser.parse_args()
    merge(args.inputs, args.destination, args.abis)


if __name__ == '__main__':
    main()
