package com.movtery.zalithlauncher.game.version.mod.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class ReplaceUpdatedModTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun `disabled mod remains disabled after update`() {
        val dir = temp.newFolder("mods")
        val old = File(dir, "old.jar.DISABLED").apply { writeText("old") }
        val download = temp.newFile("download.jar").apply { writeText("new") }
        val result = replaceUpdatedMod(old, download, dir, "new.jar")
        assertEquals("new.jar.disabled", result.name)
        assertEquals("new", result.readText())
        assertFalse(old.exists())
    }

    @Test fun `same filename is safely replaced`() {
        val dir = temp.newFolder("mods")
        val old = File(dir, "mod.jar").apply { writeText("old") }
        val download = temp.newFile("download.jar").apply { writeText("new") }
        assertEquals(old, replaceUpdatedMod(old, download, dir, "mod.jar"))
        assertEquals("new", old.readText())
    }

    @Test fun `missing download preserves old mod`() {
        val dir = temp.newFolder("mods")
        val old = File(dir, "old.jar").apply { writeText("old") }
        try {
            replaceUpdatedMod(old, File(dir, "missing.jar"), dir, "new.jar")
            throw AssertionError("Expected IOException")
        } catch (_: IOException) { }
        assertEquals("old", old.readText())
        assertFalse(File(dir, "new.jar").exists())
    }

    @Test fun `existing target is never overwritten`() {
        val dir = temp.newFolder("mods")
        val old = File(dir, "old.jar").apply { writeText("old") }
        val target = File(dir, "new.jar").apply { writeText("existing") }
        val download = temp.newFile("download.jar").apply { writeText("new") }
        try {
            replaceUpdatedMod(old, download, dir, "new.jar")
            throw AssertionError("Expected IOException")
        } catch (_: IOException) { }
        assertEquals("old", old.readText())
        assertEquals("existing", target.readText())
    }

    @Test fun `path traversal is rejected`() {
        val dir = temp.newFolder("mods")
        val old = File(dir, "old.jar").apply { writeText("old") }
        val download = temp.newFile("download.jar").apply { writeText("new") }
        try {
            replaceUpdatedMod(old, download, dir, "../new.jar")
            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) { }
        assertTrue(old.exists())
    }
}
