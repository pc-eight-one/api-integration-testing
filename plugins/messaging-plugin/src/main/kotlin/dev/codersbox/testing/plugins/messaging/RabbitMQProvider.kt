package dev.codersbox.testing.plugins.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.GetResponse
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response
import java.util.concurrent.TimeUnit

class RabbitMQProvider : MessagingProvider {
    private val objectMapper = ObjectMapper()

    override fun execute(request: Request): Response {
        val config = request.config as? RabbitMQConfig
            ?: throw IllegalArgumentException("Request config must be RabbitMQConfig")

        return when (config.operation) {
            RabbitMQOperation.PUBLISH -> publish(config, request)
            RabbitMQOperation.CONSUME -> consume(config)
        }
    }

    private fun publish(config: RabbitMQConfig, request: Request): Response {
        val startTime = System.currentTimeMillis()

        return try {
            createConnection(config).use { connection ->
                connection.createChannel().use { channel ->
                    declareExchangeAndQueue(channel, config)

                    val messageBody = when (request.body) {
                        is String -> (request.body as String).toByteArray()
                        else -> objectMapper.writeValueAsBytes(request.body)
                    }

                    val properties = com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .contentType(config.contentType)
                        .headers(config.headers)
                        .build()

                    channel.basicPublish(
                        config.exchange ?: "",
                        config.routingKey ?: config.queue,
                        properties,
                        messageBody
                    )

                    val responseTime = System.currentTimeMillis() - startTime

                    Response(
                        statusCode = 200,
                        body = objectMapper.writeValueAsString(mapOf(
                            "queue" to config.queue,
                            "exchange" to config.exchange,
                            "routingKey" to config.routingKey,
                            "messageSize" to messageBody.size
                        )),
                        headers = mapOf("X-RabbitMQ-Queue" to config.queue),
                        responseTime = responseTime
                    )
                }
            }
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "RabbitMQ publish failed",
                headers = emptyMap(),
                error = e
            )
        }
    }

    private fun consume(config: RabbitMQConfig): Response {
        val startTime = System.currentTimeMillis()
        val messages = mutableListOf<Map<String, Any?>>()

        return try {
            createConnection(config).use { connection ->
                connection.createChannel().use { channel ->
                    declareExchangeAndQueue(channel, config)

                    var response: GetResponse?
                    val endTime = System.currentTimeMillis() + config.timeout

                    while (System.currentTimeMillis() < endTime) {
                        response = channel.basicGet(config.queue, config.autoAck)
                        if (response != null) {
                            messages.add(mapOf(
                                "body" to String(response.body),
                                "deliveryTag" to response.envelope.deliveryTag,
                                "exchange" to response.envelope.exchange,
                                "routingKey" to response.envelope.routingKey,
                                "headers" to response.props.headers,
                                "contentType" to response.props.contentType
                            ))

                            if (!config.autoAck) {
                                channel.basicAck(response.envelope.deliveryTag, false)
                            }
                        } else {
                            Thread.sleep(100)
                        }
                    }

                    val responseTime = System.currentTimeMillis() - startTime

                    Response(
                        statusCode = 200,
                        body = objectMapper.writeValueAsString(mapOf(
                            "messageCount" to messages.size,
                            "messages" to messages
                        )),
                        headers = mapOf(
                            "X-RabbitMQ-Queue" to config.queue,
                            "X-Message-Count" to messages.size.toString()
                        ),
                        responseTime = responseTime
                    )
                }
            }
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "RabbitMQ consume failed",
                headers = emptyMap(),
                error = e
            )
        }
    }

    private fun createConnection(config: RabbitMQConfig): Connection {
        val factory = ConnectionFactory().apply {
            host = config.host
            port = config.port
            username = config.username
            password = config.password
            config.virtualHost?.let { virtualHost = it }
        }
        return factory.newConnection()
    }

    private fun declareExchangeAndQueue(channel: Channel, config: RabbitMQConfig) {
        if (config.declareQueue) {
            channel.queueDeclare(config.queue, config.durable, false, false, null)
        }
        
        config.exchange?.let { exchange ->
            if (config.declareExchange) {
                channel.exchangeDeclare(exchange, config.exchangeType, config.durable)
            }
            config.routingKey?.let { routingKey ->
                channel.queueBind(config.queue, exchange, routingKey)
            }
        }
    }

    override fun supports(protocol: String): Boolean {
        return protocol.uppercase() in listOf("RABBITMQ", "AMQP")
    }
}

data class RabbitMQConfig(
    val host: String = "localhost",
    val port: Int = 5672,
    val username: String = "guest",
    val password: String = "guest",
    val virtualHost: String? = null,
    val queue: String,
    val exchange: String? = null,
    val routingKey: String? = null,
    val exchangeType: String = "direct",
    val operation: RabbitMQOperation = RabbitMQOperation.PUBLISH,
    val durable: Boolean = false,
    val autoAck: Boolean = true,
    val declareQueue: Boolean = true,
    val declareExchange: Boolean = true,
    val contentType: String = "application/json",
    val headers: Map<String, Any> = emptyMap(),
    val timeout: Long = 5000L
)

enum class RabbitMQOperation {
    PUBLISH, CONSUME
}
