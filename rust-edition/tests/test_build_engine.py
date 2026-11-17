"""Contract tests for build-engine.sh, using fake tools (no Android compilation)."""
import os
import pathlib
import subprocess
import tempfile
import unittest

SCRIPT = pathlib.Path(__file__).resolve().parents[1] / "build-engine.sh"
REVISION = "5432370d573642041585c69b40fb045b27133488"


class BuildEngineContractTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = pathlib.Path(self.temp.name)
        self.source = self.root / "starling"
        self.output = self.root / "output"
        self.tools = self.root / "bin"
        self.ndk = self.root / "ndk"
        self.tools.mkdir()
        (self.source / "packaging/android").mkdir(parents=True)
        (self.source / "LICENSE").write_text("MIT test license")
        (self.source / "THIRD_PARTY_NOTICES.md").write_text("Test notices")
        (self.source / "packaging/android/build_apk.py").write_text(
            "from pathlib import Path\n"
            "p = Path(__file__).resolve().parents[2] / 'target/android/libhorizon_android.so'\n"
            "p.parent.mkdir(parents=True, exist_ok=True)\n"
            "p.write_bytes(b'test-engine')\n"
        )
        git = self.tools / "git"
        git.write_text("#!/bin/sh\ncase \"$*\" in *rev-parse*) echo \"${TEST_REVISION}\";; *status*) printf '%s' \"${TEST_DIRTY:-}\";; esac\n")
        git.chmod(0o755)
        nm = self.ndk / "toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-nm"
        nm.parent.mkdir(parents=True)
        (self.ndk / "source.properties").write_text("Pkg.Revision = 28.2.13676358\n")
        nm.write_text("#!/bin/sh\nprintf '%s\\n' \"${TEST_SYMBOL:-000 T starling_on_create}\"\n")
        nm.chmod(0o755)
        self.env = dict(os.environ, PATH=f"{self.tools}:{os.environ['PATH']}",
                        TEST_REVISION=REVISION, ANDROID_HOME=str(self.root / "sdk"),
                        ANDROID_NDK_HOME=str(self.ndk))

    def tearDown(self):
        self.temp.cleanup()

    def run_build(self, abi="arm64-v8a"):
        return subprocess.run([str(SCRIPT), str(self.source), abi, str(self.output)],
                              env=self.env, text=True, capture_output=True)

    def test_verified_output_includes_provenance_and_notices(self):
        result = self.run_build()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual((self.output / "libhorizon_android.so").read_bytes(), b"test-engine")
        self.assertIn(REVISION, (self.output / "build-info.txt").read_text())
        self.assertIn("libhorizon_android.so", (self.output / "SHA256SUMS.txt").read_text())
        self.assertTrue((self.output / "STARLING-LICENSE.txt").exists())
        self.assertTrue((self.output / "STARLING-THIRD-PARTY-NOTICES.md").exists())

    def test_bad_revision_dirty_source_and_unsupported_abi_are_rejected(self):
        self.env["TEST_REVISION"] = "wrong"
        self.assertNotEqual(self.run_build().returncode, 0)
        self.env["TEST_REVISION"] = REVISION
        self.env["TEST_DIRTY"] = " M something"
        self.assertNotEqual(self.run_build().returncode, 0)
        self.env["TEST_DIRTY"] = ""
        self.assertNotEqual(self.run_build("not-an-abi").returncode, 0)

    def test_missing_entry_point_is_rejected(self):
        self.env["TEST_SYMBOL"] = "000 T unrelated_symbol"
        result = self.run_build()
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse((self.output / "libhorizon_android.so").exists())


if __name__ == "__main__":
    unittest.main()
