"""Runtime staging tests use synthetic files; they do not test Android gameplay."""
import importlib.util
from pathlib import Path
import tempfile
import unittest
import zipfile

spec = importlib.util.spec_from_file_location("stage_runtime", Path(__file__).resolve().parents[1] / "stage_runtime.py")
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class StageRuntimeTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        self.proof = self.root / "proof"
        self.proof.mkdir()
        (self.proof / "build-info.txt").write_text(f"revision={module.REVISION}\nabi=arm64-v8a\n")
        (self.proof / "libhorizon_android.so").write_bytes(b"engine")
        self.apk = self.root / "test.apk"
        with zipfile.ZipFile(self.apk, "w") as z:
            for lib in module.LIBRARIES:
                z.writestr(f"lib/arm64-v8a/{lib}", b"engine" if lib == "libhorizon_android.so" else b"test")
            for asset in module.ASSETS:
                z.writestr(f"assets/{asset}", b"asset")
            z.writestr("assets/../../escape", b"ignored")
        self.output = self.root / "out"

    def tearDown(self):
        self.temp.cleanup()

    def test_stages_only_required_files(self):
        module.stage(self.apk, "arm64-v8a", self.proof, self.output)
        self.assertEqual((self.output / "jniLibs/arm64-v8a/libhorizon_android.so").read_bytes(), b"engine")
        self.assertTrue((self.output / "assets/prepare-kit.zip").exists())
        self.assertTrue((self.output / "runtime-arm64-v8a.json").exists())
        self.assertFalse((self.root / "escape").exists())

    def test_rejects_wrong_engine(self):
        (self.proof / "libhorizon_android.so").write_bytes(b"not-the-apk-engine")
        with self.assertRaisesRegex(ValueError, "does not match"):
            module.stage(self.apk, "arm64-v8a", self.proof, self.output)
        self.assertFalse((self.output / "jniLibs").exists())

    def test_rejects_wrong_abi(self):
        with self.assertRaisesRegex(ValueError, "identity"):
            module.stage(self.apk, "x86", self.proof, self.output)

    def test_rejects_ambiguous_duplicate_entry(self):
        with zipfile.ZipFile(self.apk, "a") as z:
            z.writestr("assets/prepare-kit.zip", b"duplicate")
        with self.assertRaisesRegex(ValueError, "duplicated"):
            module.stage(self.apk, "arm64-v8a", self.proof, self.output)


if __name__ == "__main__":
    unittest.main()
