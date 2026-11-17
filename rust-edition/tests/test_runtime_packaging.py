"""Synthetic runtime packaging checks; not a substitute for Android device tests."""
import hashlib
import json
from pathlib import Path
import sys
import tempfile
import unittest
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import merge_runtime
import stage_runtime
import verify_apk


class RuntimePackagingTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        self.inputs = self.root / 'inputs'
        for abi in ('arm64-v8a', 'x86'):
            root = self.inputs / f'rust-runtime-{abi}'
            hashes = {}
            for path in ([f'jniLibs/{abi}/{lib}' for lib in stage_runtime.LIBRARIES]
                         + [f'assets/{name}' for name in stage_runtime.ASSETS]):
                file = root / path
                file.parent.mkdir(parents=True, exist_ok=True)
                if path.endswith('.zip'):
                    with zipfile.ZipFile(file, 'w') as archive:
                        archive.writestr('data', b'asset')
                else:
                    file.write_bytes(b'asset' if path.startswith('assets/') else abi.encode())
                hashes[path] = hashlib.sha256(file.read_bytes()).hexdigest()
            (root / f'runtime-{abi}.json').write_text(json.dumps({
                'revision': stage_runtime.REVISION, 'abi': abi, 'sha256': hashes}))
        self.out = self.root / 'runtime'

    def tearDown(self):
        self.tmp.cleanup()

    def test_merge_and_apk_verification(self):
        abis = ('arm64-v8a', 'x86')
        merge_runtime.merge(self.inputs, self.out, abis)
        apk = self.root / 'app.apk'
        with zipfile.ZipFile(apk, 'w') as archive:
            for file in self.out.rglob('*'):
                if file.is_file() and not file.name.startswith('runtime-'):
                    name = file.relative_to(self.out).as_posix().replace('jniLibs/', 'lib/', 1)
                    archive.write(file, name)
        verify_apk.verify(apk, self.out, abis)

    def test_merge_rejects_tampered_library(self):
        path = self.inputs / 'rust-runtime-x86/jniLibs/x86/libhorizon_android.so'
        path.write_bytes(b'tampered')
        with self.assertRaisesRegex(ValueError, 'Invalid runtime file'):
            merge_runtime.merge(self.inputs, self.out, ('x86',))

    def test_verify_rejects_missing_entry(self):
        merge_runtime.merge(self.inputs, self.out, ('arm64-v8a',))
        apk = self.root / 'empty.apk'
        with zipfile.ZipFile(apk, 'w'):
            pass
        with self.assertRaisesRegex(ValueError, 'Missing or duplicate'):
            verify_apk.verify(apk, self.out, ('arm64-v8a',))

    def test_merge_rejects_different_assets(self):
        path = self.inputs / 'rust-runtime-x86/assets/lowbudget.zip'
        with zipfile.ZipFile(path, 'w') as z:
            z.writestr('data', b'different')
        manifest_path = path.parents[1] / 'runtime-x86.json'
        manifest = json.loads(manifest_path.read_text())
        manifest['sha256']['assets/lowbudget.zip'] = hashlib.sha256(path.read_bytes()).hexdigest()
        manifest_path.write_text(json.dumps(manifest))
        with self.assertRaisesRegex(ValueError, 'Assets differ'):
            merge_runtime.merge(self.inputs, self.out, ('arm64-v8a', 'x86'))


if __name__ == '__main__':
    unittest.main()
