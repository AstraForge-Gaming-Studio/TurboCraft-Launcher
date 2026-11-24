package com.movtery.zalithlauncher.setting

import com.movtery.zalithlauncher.setting.unit.AbstractSettingUnit
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingReadFallbackTest {
    private class FakeSetting : AbstractSettingUnit<String>("test", "default") {
        var failure: Exception? = null
        var stored = "saved"
        var writes = 0
        override fun readValue(): String {
            failure?.let { throw it }
            return stored
        }
        override fun saveValue(v: String): String { writes++; stored = v; return v }
    }

    @Test fun `failed read returns default without writing storage`() {
        val setting = FakeSetting()
        setting.failure = IllegalArgumentException("corrupt")
        assertEquals("default", setting.getValue())
        assertEquals("default", setting.state)
        assertEquals("saved", setting.stored)
        assertEquals(0, setting.writes)
        setting.failure = null
        assertEquals("saved", setting.getValue())
    }

    @Test fun `cancellation is not swallowed`() {
        val setting = FakeSetting()
        val failure = CancellationException("cancelled")
        setting.failure = failure
        try {
            setting.getValue()
            throw AssertionError("Cancellation must propagate")
        } catch (error: CancellationException) {
            assertEquals(failure, error)
        }
    }

    @Test fun `nullable default survives a failed read`() {
        val setting = object : AbstractSettingUnit<String?>("nullable", null) {
            override fun readValue(): String? = throw IllegalStateException("bad value")
            override fun saveValue(v: String?): String? = v
        }
        assertNull(setting.getValue())
    }
}
