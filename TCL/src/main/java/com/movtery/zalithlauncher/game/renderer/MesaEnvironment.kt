/*
 * TurboCraft Launcher
 * Copyright (C) 2025-2026 TurboCraft Launcher contributors
 * Licensed under the GNU General Public License, version 3 or later.
 */
package com.movtery.zalithlauncher.game.renderer

/** Configure Mesa only for Mesa renderers; never force Zink onto other backends. */
internal fun applyMesaEnvironment(
    env: MutableMap<String, String>,
    renderer: RendererInterface,
    rendererLibrary: String?,
    cacheDirectory: String
) {
    if (renderer.mesaBased) {
        env["LIB_MESA_NAME"] = rendererLibrary ?: "null"
        env["MESA_GLSL_CACHE_DIR"] = cacheDirectory
    }
    if (renderer.needsZinkDriverOverride) {
        env["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
        env["MESA_GL_VERSION_OVERRIDE"] = "4.6"
        env["MESA_GLSL_VERSION_OVERRIDE"] = "460"
        env["force_glsl_extensions_warn"] = "true"
        env["allow_higher_compat_version"] = "true"
        env["allow_glsl_extension_directive_midshader"] = "true"
    }
}
