package dev.codersbox.testing.plugins.streaming

import dev.codersbox.testing.core.plugin.ProtocolPlugin
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response

class StreamingPlugin : ProtocolPlugin {
    override val name: String = "streaming"
    override val supportedProtocols: List<String> = listOf("WEBSOCKET", "WS", "WSS", "SSE")

    private val providers = mutableMapOf<String, StreamingProvider>()

    init {
        registerProvider("WEBSOCKET", WebSocketProvider())
        registerProvider("WS", WebSocketProvider())
        registerProvider("WSS", WebSocketProvider())
        registerProvider("SSE", ServerSentEventsProvider())
    }

    fun registerProvider(protocol: String, provider: StreamingProvider) {
        providers[protocol.uppercase()] = provider
    }

    override fun execute(request: Request): Response {
        val protocol = request.protocol.uppercase()
        val provider = providers[protocol]
            ?: throw IllegalArgumentException("Unsupported streaming protocol: $protocol")

        return provider.execute(request)
    }

    override fun supports(request: Request): Boolean {
        return request.protocol.uppercase() in supportedProtocols
    }
}

interface StreamingProvider {
    fun execute(request: Request): Response
    fun supports(protocol: String): Boolean
}
