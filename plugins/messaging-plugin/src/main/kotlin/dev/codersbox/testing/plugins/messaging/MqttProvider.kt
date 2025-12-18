package dev.codersbox.testing.plugins.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MqttProvider : MessagingProvider {
    private val objectMapper = ObjectMapper()

    override fun execute(request: Request): Response {
        val config = request.config as? MqttConfig
            ?: throw IllegalArgumentException("Request config must be MqttConfig")

        return when (config.operation) {
            MqttOperation.PUBLISH -> publish(config, request)
            MqttOperation.SUBSCRIBE -> subscribe(config)
        }
    }

    private fun publish(config: MqttConfig, request: Request): Response {
        val startTime = System.currentTimeMillis()

        return try {
            val client = createClient(config)
            client.connect(createConnectOptions(config))

            val messageBody = when (request.body) {
                is String -> (request.body as String).toByteArray()
                else -> objectMapper.writeValueAsBytes(request.body)
            }

            val message = MqttMessage(messageBody).apply {
                qos = config.qos
                isRetained = config.retained
            }

            client.publish(config.topic, message)
            client.disconnect()
            client.close()

            val responseTime = System.currentTimeMillis() - startTime

            Response(
                statusCode = 200,
                body = objectMapper.writeValueAsString(mapOf(
                    "topic" to config.topic,
                    "messageSize" to messageBody.size,
                    "qos" to config.qos,
                    "retained" to config.retained
                )),
                headers = mapOf("X-MQTT-Topic" to config.topic),
                responseTime = responseTime
            )
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "MQTT publish failed",
                headers = emptyMap(),
                error = e
            )
        }
    }

    private fun subscribe(config: MqttConfig): Response {
        val startTime = System.currentTimeMillis()
        val messages = mutableListOf<Map<String, Any?>>()
        val latch = CountDownLatch(1)

        return try {
            val client = createClient(config)
            
            client.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    latch.countDown()
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    messages.add(mapOf(
                        "topic" to topic,
                        "payload" to String(message.payload),
                        "qos" to message.qos,
                        "retained" to message.isRetained,
                        "duplicate" to message.isDuplicate
                    ))
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            client.connect(createConnectOptions(config))
            client.subscribe(config.topic, config.qos)

            // Wait for messages
            latch.await(config.timeout, TimeUnit.MILLISECONDS)

            client.disconnect()
            client.close()

            val responseTime = System.currentTimeMillis() - startTime

            Response(
                statusCode = 200,
                body = objectMapper.writeValueAsString(mapOf(
                    "messageCount" to messages.size,
                    "messages" to messages
                )),
                headers = mapOf(
                    "X-MQTT-Topic" to config.topic,
                    "X-Message-Count" to messages.size.toString()
                ),
                responseTime = responseTime
            )
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "MQTT subscribe failed",
                headers = emptyMap(),
                error = e
            )
        }
    }

    private fun createClient(config: MqttConfig): MqttClient {
        return MqttClient(
            config.brokerUrl,
            config.clientId ?: MqttClient.generateClientId(),
            MemoryPersistence()
        )
    }

    private fun createConnectOptions(config: MqttConfig): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = config.cleanSession
            config.username?.let { userName = it }
            config.password?.let { password = it.toCharArray() }
            connectionTimeout = (config.timeout / 1000).toInt()
        }
    }

    override fun supports(protocol: String): Boolean = protocol.uppercase() == "MQTT"
}

data class MqttConfig(
    val brokerUrl: String,
    val topic: String,
    val operation: MqttOperation = MqttOperation.PUBLISH,
    val clientId: String? = null,
    val qos: Int = 0,
    val retained: Boolean = false,
    val cleanSession: Boolean = true,
    val username: String? = null,
    val password: String? = null,
    val timeout: Long = 5000L
)

enum class MqttOperation {
    PUBLISH, SUBSCRIBE
}
