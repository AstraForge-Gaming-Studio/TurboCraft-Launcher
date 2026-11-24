package com.movtery.zalithlauncher.game.version.multiplayer

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatusResponseFaviconTest {
    private fun status(favicon: String): StatusResponse = StatusResponse.parse(
        """{"players":{"max":20,"online":3},"version":{"name":"Test","protocol":1},"favicon":$favicon}"""
    )

    @Test fun `invalid favicon does not discard server status`() {
        val response = status("\"not-a-png-data-url\"")
        assertNull(response.favicon)
        assertEquals(3, response.players.online)
        assertEquals("Test", response.version.name)
    }

    @Test fun `favicon with line breaks is decoded`() {
        val response = status("\"data:image/png;base64,AQID\\nBA==\"")
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), response.favicon!!.icon)
    }

    @Test fun `missing favicon remains absent`() {
        val response = StatusResponse.parse("""{"players":{"max":1,"online":0},"version":{"name":"Test","protocol":1}}""")
        assertNull(response.favicon)
    }
}
