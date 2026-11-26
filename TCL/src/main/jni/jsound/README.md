# Native Java Sound bridge

These sources are ported from ZalithLauncher2 commit
`b6310dd0284caecfeeed47e9213eecc4cb69284c`. The OpenJDK Java Sound
sources retain their original copyright and GPLv2 with Classpath Exception
headers. The accompanying `LICENSE` and `ASSEMBLY_EXCEPTION` are from the
bundled OpenJDK 17 java.desktop runtime. The OpenAL backend is distributed
under the launcher project license. MIDI and port mixer device backends are
stubbed as in upstream; sampled audio is handled via OpenAL.
