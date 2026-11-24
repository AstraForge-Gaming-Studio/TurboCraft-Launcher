package com.movtery.zalithlauncher.game.path

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GamePathSafetyTest {
    @get:Rule val temp = TemporaryFolder()

    @Test fun `storage root and ancestors cannot be used`() {
        val root = temp.newFolder("storage")
        assertTrue(isStorageRootOrAncestor(root.path, root))
        assertTrue(isStorageRootOrAncestor(root.parent, root))
        assertTrue(isStorageRootOrAncestor(File(root, "child/..").path, root))
        assertTrue(isStorageRootOrAncestor("", root))
    }

    @Test fun `subdirectories and unrelated paths remain selectable`() {
        val root = temp.newFolder("storage")
        assertFalse(isStorageRootOrAncestor(File(root, "games").path, root))
        assertFalse(isStorageRootOrAncestor(File(root.parentFile, "other").path, root))
    }
}
