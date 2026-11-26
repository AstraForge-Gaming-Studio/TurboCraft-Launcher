#!/usr/bin/env bash
# Compile the real Java Sound backend on Linux with a fake OpenAL device.
set -euo pipefail
: "${JAVA_HOME:?JAVA_HOME must point to a JDK with JNI headers}"
root=$(cd "$(dirname "$0")/.." && pwd)
src="$root/TCL/src/main/jni/jsound"
tests="$src/tests"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
flags=(-std=c11 -D_GNU_SOURCE -DX_PLATFORM=X_LINUX -D_LITTLE_ENDIAN -DUSE_DAUDIO=TRUE -DUSE_PORTS=FALSE -DUSE_PLATFORM_MIDI_OUT=FALSE -DUSE_PLATFORM_MIDI_IN=FALSE -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/linux" -I"$tests" -I"$src")
gcc -shared -fPIC -std=c11 "$tests/fake_openal.c" -o "$tmp/libopenal.so"
gcc -shared -fPIC "${flags[@]}" \
  "$src"/{Utilities,Platform,DirectAudioDevice,DirectAudioDeviceProvider,PortMixer,PortMixerProvider,MidiOutDevice,MidiOutDeviceProvider,MidiInDevice,MidiInDeviceProvider,PlatformMidi,jsound_openal,jdk8_compat}.c \
  "$tests/android_log.c" -ldl -lpthread -o "$tmp/libjsound.so"
gcc "${flags[@]}" "$tests/jsound_test.c" -L"$tmp" -ljsound -lopenal -lpthread -Wl,-rpath,"$tmp" -o "$tmp/test"
LD_LIBRARY_PATH="$tmp${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" timeout 10 "$tmp/test"
