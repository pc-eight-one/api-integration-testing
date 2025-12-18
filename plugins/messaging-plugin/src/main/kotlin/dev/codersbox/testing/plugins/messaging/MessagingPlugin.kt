package dev.codersbox.testing.plugins.messaging

import dev.codersbox.testing.core.plugin.ProtocolPlugin
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response

class MessagingPlugin : ProtocolPlugin {
    override val name: String = "messaging"
    override val supportedProtocols: List<String> = listOf("KAFKA", "RABBITMQ", "MQTT", "AMQP")

    private val providers = mutableMapOf<String, MessagingProvider>()

    init {
        registerProvider("KAFKA", KafkaProvider())
        registerProvider("RABBITMQ", RabbitMQProvider())
        registerProvider("MQTT", MqttProvider())
        registerProvider("AMQP", RabbitMQProvider()) // AMQP uses RabbitMQ
    }

    fun registerProvider(protocol: String, provider: MessagingProvider) {
        providers[protocol.uppercase()] = provider
    }

    override fun execute(request: Request): Response {
        val protocol = request.protocol.uppercase()
        val provider = providers[protocol]
            ?: throw IllegalArgumentException("Unsupported messaging protocol: $protocol")

        return provider.execute(request)
    }

    override fun supports(request: Request): Boolean {
        return request.protocol.uppercase() in supportedProtocols
    }
}

interface MessagingProvider {
    fun execute(request: Request): Response
    fun supports(protocol: String): Boolean
}
