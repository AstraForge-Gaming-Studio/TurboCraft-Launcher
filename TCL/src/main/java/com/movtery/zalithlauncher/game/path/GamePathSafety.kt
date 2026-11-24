/*
 * TurboCraft Launcher
 * Copyright (C) 2025-2026 TurboCraft Launcher contributors
 * Licensed under the GNU General Public License, version 3 or later.
 */
package com.movtery.zalithlauncher.game.path

import java.io.File
import java.io.IOException

/** Block the primary shared-storage root and its ancestors, including path aliases. */
internal fun isStorageRootOrAncestor(path: String, storageRoot: File): Boolean {
    if (path.isBlank()) return true
    return try {
        val target = File(path).canonicalFile
        var current: File? = storageRoot.canonicalFile
        while (current != null) {
            if (current == target) return true
            current = current.parentFile
        }
        false
    } catch (_: IOException) {
        true
    } catch (_: SecurityException) {
        true
    }
}
