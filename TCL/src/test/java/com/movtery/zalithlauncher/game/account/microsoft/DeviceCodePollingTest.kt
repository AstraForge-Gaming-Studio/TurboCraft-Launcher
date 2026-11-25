package com.movtery.zalithlauncher.game.account.microsoft

import com.movtery.zalithlauncher.game.account.microsoft.models.DeviceCodeResponse
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceCodePollingTest {
    private val server = MockWebServer().also { it.start() }
    private val code = DeviceCodeResponse("USER", "DEVICE", "https://example.org", 20, 0)

    @After fun close() = server.close()

    private fun response(status: Int, json: String) = MockResponse.Builder()
        .code(status).addHeader("Content-Type", "application/json").body(json).build()

    @Test fun `pending and slow down are followed until a token arrives`() = runBlocking {
        server.enqueue(response(400, """{"error":"authorization_pending"}"""))
        server.enqueue(response(400, """{"error":"slow_down"}"""))
        server.enqueue(response(200, """{"access_token":"test-access","refresh_token":"test-refresh","expires_in":3600}"""))
        val token = getTokenResponse(code, Dispatchers.IO, server.url("/token").toString()) { false }
        assertEquals("test-access", token.accessToken)
        assertEquals("test-refresh", token.refreshToken)
        assertEquals(3, server.requestCount)
        val request = server.takeRequest()
        val body = requireNotNull(request.body).utf8()
        assertTrue(body.contains("client_id=00000000402b5328"))
        assertTrue(body.contains("device_code=DEVICE"))
    }

    @Test fun `server errors retry without requiring a json body`() = runBlocking {
        server.enqueue(response(503, "temporarily unavailable"))
        server.enqueue(response(200, """{"access_token":"ok","expires_in":60}"""))
        val token = getTokenResponse(code, Dispatchers.IO, server.url("/token").toString()) { false }
        assertEquals("ok", token.accessToken)
        assertEquals(2, server.requestCount)
    }

    @Test fun `terminal client errors are not retried`() = runBlocking {
        server.enqueue(response(400, """{"error":"authorization_declined"}"""))
        try {
            getTokenResponse(code, Dispatchers.IO, server.url("/token").toString()) { false }
            throw AssertionError("Expected terminal client error")
        } catch (_: ClientRequestException) {
            assertEquals(1, server.requestCount)
        }
    }

    @Test fun `cancellation callback stops polling`() = runBlocking {
        server.enqueue(response(400, """{"error":"authorization_pending"}"""))
        server.enqueue(response(400, """{"error":"authorization_pending"}"""))
        try {
            getTokenResponse(code, Dispatchers.IO, server.url("/token").toString()) { true }
            throw AssertionError("Expected cancellation")
        } catch (_: CancellationException) {
            assertEquals(2, server.requestCount)
        }
    }
}
