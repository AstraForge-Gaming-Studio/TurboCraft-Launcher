package com.movtery.zalithlauncher.ui.screens.content

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionExportFilterTest {
    @Test fun `only root version jar and json are excluded`() {
        assertTrue(isExcludedVersionFile("my-version.jar", 1, "my-version"))
        assertTrue(isExcludedVersionFile("my-version.json", 1, "my-version"))
        assertFalse(isExcludedVersionFile("my-version.jar.disabled", 1, "my-version"))
        assertFalse(isExcludedVersionFile("my-version.json.backup", 1, "my-version"))
        assertFalse(isExcludedVersionFile("my-version.jar", 2, "my-version"))
        assertFalse(isExcludedVersionFile("my-version.json", 3, "my-version"))
    }
}
