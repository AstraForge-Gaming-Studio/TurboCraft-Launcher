package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.renderer.renderers.KopperZinkRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.VirGLRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MesaEnvironmentTest {
    private fun renderer(mesa: Boolean, zink: Boolean) = object : RendererInterface {
        override val mesaBased = mesa
        override val needsZinkDriverOverride = zink
        override fun getRendererId() = "test"
        override fun getUniqueIdentifier() = "test"
        override fun getRendererName() = "test"
        override fun getRendererEnv() = lazy { emptyMap<String, String>() }
        override fun getDlopenLibrary() = lazy { emptyList<String>() }
        override fun getRendererLibrary() = "libtest.so"
    }

    @Test fun `Mesa renderer gets library and cache without forced zink`() {
        val env = mutableMapOf<String, String>()
        applyMesaEnvironment(env, VirGLRenderer, "libOSMesa.so", "/cache")
        assertEquals("libOSMesa.so", env["LIB_MESA_NAME"])
        assertEquals("/cache", env["MESA_GLSL_CACHE_DIR"])
        assertFalse(env.containsKey("MESA_LOADER_DRIVER_OVERRIDE"))
        assertFalse(env.containsKey("MESA_GL_VERSION_OVERRIDE"))
    }

    @Test fun `Kopper zink gets its required overrides`() {
        val env = mutableMapOf<String, String>()
        applyMesaEnvironment(env, KopperZinkRenderer, "libglxshim.so", "/cache")
        assertEquals("zink", env["MESA_LOADER_DRIVER_OVERRIDE"])
        assertEquals("4.6", env["MESA_GL_VERSION_OVERRIDE"])
        assertEquals("libglxshim.so", env["LIB_MESA_NAME"])
    }

    @Test fun `non Mesa renderer is left untouched`() {
        val env = mutableMapOf("CUSTOM" to "value")
        applyMesaEnvironment(env, renderer(mesa = false, zink = false), "libtest.so", "/cache")
        assertEquals(mapOf("CUSTOM" to "value"), env)
    }
}
