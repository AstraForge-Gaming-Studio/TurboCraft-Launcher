package com.movtery.zalithlauncher.game.multirt

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class NativeSoundInstallerTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun `modern runtimes get native sound and existing library is updated`() {
        val runtime = folder.newFolder("runtime")
        val lib = File(runtime, "lib").apply { mkdirs() }
        val target = File(lib, "libjsound.so").apply { writeText("old") }
        val source = folder.newFile("source.so").apply { writeText("new-native-bridge") }
        installNativeSound(runtime, "aarch64", false, source)
        assertArrayEquals(source.readBytes(), target.readBytes())
        target.setLastModified(123456789000L)
        val time = target.lastModified()
        installNativeSound(runtime, "aarch64", false, source)
        assertEquals(time, target.lastModified())
        assertTrue(lib.listFiles()!!.none { it.name.endsWith(".tmp") })
    }

    @Test fun `java eight uses architecture directory and jdk layout`() {
        val runtime = folder.newFolder("jdk")
        val lib = File(runtime, "jre/lib/amd64").apply { mkdirs() }
        val source = folder.newFile("source.so").apply { writeText("bridge") }
        installNativeSound(runtime, "amd64", true, source)
        assertEquals("bridge", File(lib, "libjsound.so").readText())
    }

    @Test fun `runtime with architecture subdirectory uses that directory`() {
        val runtime = folder.newFolder("jre8")
        val lib = File(runtime, "lib/aarch64").apply { mkdirs() }
        val source = folder.newFile("source.so").apply { writeText("bridge") }
        installNativeSound(runtime, "aarch64", false, source)
        assertEquals("bridge", File(lib, "libjsound.so").readText())
    }

    @Test fun `missing source never deletes existing sound library`() {
        val runtime = folder.newFolder("runtime")
        val lib = File(runtime, "lib").apply { mkdirs() }
        val target = File(lib, "libjsound.so").apply { writeText("original") }
        try {
            installNativeSound(runtime, "aarch64", false, File(folder.root, "missing.so"))
            throw AssertionError("Missing source must fail")
        } catch (_: IOException) {
            assertEquals("original", target.readText())
        }
    }

    @Test fun `architecture cannot escape runtime library directory`() {
        val runtime = folder.newFolder("runtime")
        File(runtime, "lib").mkdirs()
        val source = folder.newFile("source.so").apply { writeText("bridge") }
        for (arch in listOf("..", "../outside", "/outside")) {
            try {
                installNativeSound(runtime, arch, false, source)
                throw AssertionError("Unsafe architecture accepted: $arch")
            } catch (_: IOException) { }
        }
    }
}
