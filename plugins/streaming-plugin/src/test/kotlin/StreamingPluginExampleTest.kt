package dev.codersbox.testing.plugins.streaming

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StreamingPluginExampleTest : FunSpec({
    
    test("WebSocket Configuration") {
        val config = WebSocketConfig(
            messagesToSend = listOf(
                """{"type": "join", "user": "testUser"}""",
                """{"type": "message", "text": "Hello"}"""
            ),
            expectedMessages = 2,
            timeout = 10000L,
            headers = mapOf("Authorization" to "Bearer token123")
        )
        
        config.messagesToSend.size shouldBe 2
        config.expectedMessages shouldBe 2
    }
    
    test("SSE Configuration") {
        val config = SSEConfig(
            expectedEvents = 5,
            timeout = 30000L,
            headers = mapOf("Accept" to "text/event-stream")
        )
        
        config.expectedEvents shouldBe 5
        config.timeout shouldBe 30000L
    }
    
    test("Streaming Plugin Supports Protocols") {
        val plugin = StreamingPlugin()
        
        plugin.supports(
            dev.codersbox.testing.core.request.Request(
                method = "GET",
                url = "ws://localhost:8080/chat",
                protocol = "WEBSOCKET"
            )
        ) shouldBe true
        
        plugin.supports(
            dev.codersbox.testing.core.request.Request(
                method = "GET",
                url = "http://localhost:8080/events",
                protocol = "SSE"
            )
        ) shouldBe true
    }
    
    test("WebSocket Provider Supports WS Protocols") {
        val provider = WebSocketProvider()
        
        provider.supports("WEBSOCKET") shouldBe true
        provider.supports("WS") shouldBe true
        provider.supports("WSS") shouldBe true
        provider.supports("HTTP") shouldBe false
    }
    
    test("SSE Provider Supports SSE Protocol") {
        val provider = ServerSentEventsProvider()
        
        provider.supports("SSE") shouldBe true
        provider.supports("HTTP") shouldBe false
    }
})
