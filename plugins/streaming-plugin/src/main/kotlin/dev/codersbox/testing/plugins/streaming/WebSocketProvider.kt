package dev.codersbox.testing.plugins.streaming

import com.fasterxml.jackson.databind.ObjectMapper
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentLinkedQueue

class WebSocketProvider : StreamingProvider {
    private val objectMapper = ObjectMapper()

    override fun execute(request: Request): Response {
        val config = request.config as? WebSocketConfig
            ?: throw IllegalArgumentException("Request config must be WebSocketConfig")

        return runBlocking {
            try {
                executeWebSocket(config, request)
            } catch (e: Exception) {
                Response(
                    statusCode = 500,
                    body = e.message ?: "WebSocket connection failed",
                    headers = emptyMap(),
                    error = e
                )
            }
        }
    }

    private suspend fun executeWebSocket(config: WebSocketConfig, request: Request): Response {
        val messages = ConcurrentLinkedQueue<String>()
        val startTime = System.currentTimeMillis()

        val client = HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = config.pingInterval
                maxFrameSize = config.maxFrameSize
            }
        }

        return try {
            withTimeout(config.timeout) {
                client.webSocket(
                    urlString = request.url,
                    request = {
                        config.headers.forEach { (key, value) ->
                            headers.append(key, value)
                        }
                    }
                ) {
                    // Send messages if configured
                    config.messagesToSend.forEach { message ->
                        send(Frame.Text(message))
                    }

                    // Receive messages
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                messages.add(text)
                                
                                // Call message handler if provided
                                config.onMessage?.invoke(text)
                                
                                // Break if we've received expected number of messages
                                if (config.expectedMessages > 0 && messages.size >= config.expectedMessages) {
                                    break
                                }
                            }
                            is Frame.Binary -> {
                                val bytes = frame.readBytes()
                                messages.add("BINARY[${bytes.size} bytes]")
                            }
                            is Frame.Close -> {
                                break
                            }
                            else -> {}
                        }
                    }
                }
            }

            val responseTime = System.currentTimeMillis() - startTime

            Response(
                statusCode = 200,
                body = objectMapper.writeValueAsString(mapOf(
                    "messageCount" to messages.size,
                    "messages" to messages.toList()
                )),
                headers = mapOf(
                    "X-WebSocket-Messages" to messages.size.toString(),
                    "X-Connection-Time" to responseTime.toString()
                ),
                responseTime = responseTime
            )
        } finally {
            client.close()
        }
    }

    override fun supports(protocol: String): Boolean {
        return protocol.uppercase() in listOf("WEBSOCKET", "WS", "WSS")
    }
}

data class WebSocketConfig(
    val messagesToSend: List<String> = emptyList(),
    val expectedMessages: Int = 0,
    val timeout: Long = 30000L,
    val pingInterval: Long = 20000L,
    val maxFrameSize: Long = Long.MAX_VALUE,
    val headers: Map<String, String> = emptyMap(),
    val onMessage: ((String) -> Unit)? = null
)
