/*
 * TurboCraft Launcher
 * Copyright (C) 2025-2026 TurboCraft Launcher contributors
 * Licensed under the GNU General Public License, version 3 or later.
 */
package com.movtery.zalithlauncher.game.multirt

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** Install the matching APK's native Java Sound bridge before starting a JVM. */
internal fun installNativeSound(runtimeHome: File, arch: String?, isJdk8: Boolean, source: File) {
    if (!source.isFile) throw IOException("Native Java Sound library is missing: ${source.absolutePath}")
    if (arch != null && !arch.matches(Regex("[A-Za-z0-9][A-Za-z0-9_+.-]*"))) {
        throw IOException("Invalid Java runtime architecture")
    }
    val libDir = File(if (isJdk8) File(runtimeHome, "jre") else runtimeHome, "lib")
    val archDir = arch?.let { File(libDir, it) }
    val targetDir = if (archDir?.isDirectory == true) archDir else libDir
    if (!targetDir.isDirectory || !targetDir.canonicalFile.toPath().startsWith(runtimeHome.canonicalFile.toPath())) {
        throw IOException("Java runtime library directory is missing or outside the runtime: $targetDir")
    }
    val target = File(targetDir, "libjsound.so")
    if (target.isFile && FileUtils.contentEquals(source, target)) return

    val temporary = File.createTempFile("libjsound-", ".tmp", targetDir)
    try {
        source.copyTo(temporary, overwrite = true)
        try {
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        temporary.delete()
    }
}
