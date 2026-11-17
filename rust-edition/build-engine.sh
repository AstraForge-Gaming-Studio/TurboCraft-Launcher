#!/usr/bin/env bash
# Build the pinned Starling/Horizon Android engine as an integration artifact.
# This does not build a playable TCL APK or bundle Minecraft resources.
set -euo pipefail

readonly revision="5432370d573642041585c69b40fb045b27133488"
if [[ $# != 3 ]]; then
  echo "Usage: $0 <starling-source-directory> <abi> <output-directory>" >&2
  exit 2
fi
source_dir=$(cd "$1" && pwd)
abi=$2
mkdir -p "$3"
out_dir=$(cd "$3" && pwd)
case "$abi" in
  arm64-v8a|armeabi-v7a|x86|x86_64) ;;
  *) echo "Unsupported ABI: $abi" >&2; exit 2 ;;
esac
actual_revision=$(git -C "$source_dir" rev-parse HEAD)
if [[ "$actual_revision" != "$revision" ]]; then
  echo "Starling revision mismatch: expected $revision, got $actual_revision" >&2
  exit 1
fi
if [[ -n $(git -C "$source_dir" status --porcelain --untracked-files=normal) ]]; then
  echo "Starling checkout has changes; refusing an unverified source build" >&2
  exit 1
fi
: "${ANDROID_HOME:?ANDROID_HOME must point to the Android SDK}"
: "${ANDROID_NDK_HOME:?ANDROID_NDK_HOME must point to the Android NDK}"
[[ -f "$source_dir/packaging/android/build_apk.py" ]] || { echo "Missing upstream build script" >&2; exit 1; }
[[ -f "$ANDROID_NDK_HOME/source.properties" ]] && grep -Eq '^Pkg.Revision[[:space:]]*=[[:space:]]*28\.2\.13676358[[:space:]]*$' "$ANDROID_NDK_HOME/source.properties" || {
  echo "Android NDK 28.2.13676358 is required" >&2; exit 1;
}
# Never leave a successful-looking artifact from an earlier run after a failure.
rm -f -- "$out_dir/libhorizon_android.so" "$out_dir/exported-symbols.txt" \
  "$out_dir/SHA256SUMS.txt" "$out_dir/build-info.txt" \
  "$out_dir/STARLING-LICENSE.txt" "$out_dir/STARLING-THIRD-PARTY-NOTICES.md"

# The upstream build script uses the pinned NDK and Cargo's Android target.
# --library-only intentionally excludes the setup assets, activity and APK.
python3 "$source_dir/packaging/android/build_apk.py" --abi "$abi" --profile quick --library-only
library="$source_dir/target/android/libhorizon_android.so"
[[ -s "$library" ]] || { echo "Engine library was not produced" >&2; exit 1; }
nm="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-nm"
[[ -x "$nm" ]] || { echo "Android NDK llvm-nm not found" >&2; exit 1; }
"$nm" -D --defined-only "$library" > "$out_dir/exported-symbols.txt"
grep -Eq '[[:space:]]starling_on_create$' "$out_dir/exported-symbols.txt" || {
  echo "NativeActivity entry point missing from library" >&2; exit 1;
}
cp "$library" "$out_dir/libhorizon_android.so"
cp "$source_dir/LICENSE" "$out_dir/STARLING-LICENSE.txt"
cp "$source_dir/THIRD_PARTY_NOTICES.md" "$out_dir/STARLING-THIRD-PARTY-NOTICES.md"
(
  cd "$out_dir"
  sha256sum libhorizon_android.so > SHA256SUMS.txt
  printf 'repository=https://github.com/starling-client/starling\nrevision=%s\nabi=%s\n' "$revision" "$abi" > build-info.txt
)
echo "Built and checked $out_dir/libhorizon_android.so"
