/*
 * TurboCraft Launcher
 * Copyright (C) 2025-2026 TurboCraft Launcher contributors
 * Licensed under the GNU General Public License, version 3 or later.
 */
package com.movtery.zalithlauncher.game.version.mod.update

import com.movtery.zalithlauncher.game.version.mod.DISABLED_MOD_SUFFIX
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Preserve the disabled state and stage the download before replacing the old mod. */
internal fun replaceUpdatedMod(oldFile: File, download: File, modsDir: File, fileName: String): File {
    require(fileName.isNotBlank() && fileName != "." && fileName != ".." && File(fileName).name == fileName) {
        "Invalid mod filename"
    }
    val targetName = if (oldFile.isDisabled()) "$fileName$DISABLED_MOD_SUFFIX" else fileName
    val target = File(modsDir, targetName)
    val sameFile = oldFile.canonicalFile == target.canonicalFile
    if (!oldFile.isFile || !download.isFile || !modsDir.isDirectory) {
        throw IOException("Mod update files or directory are missing")
    }
    if (!sameFile && target.exists()) throw IOException("Mod update target already exists: $target")

    val staged = Files.createTempFile(modsDir.toPath(), ".tcl-mod-update-", ".tmp")
    try {
        Files.copy(download.toPath(), staged, StandardCopyOption.REPLACE_EXISTING)
        try {
            Files.move(staged, target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(staged, target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        if (!sameFile && !oldFile.delete()) {
            // Keep the old mod if its removal failed; avoid leaving both copies enabled.
            target.delete()
            throw IOException("Failed to remove old mod: $oldFile")
        }
        return target
    } finally {
        Files.deleteIfExists(staged)
    }
}
