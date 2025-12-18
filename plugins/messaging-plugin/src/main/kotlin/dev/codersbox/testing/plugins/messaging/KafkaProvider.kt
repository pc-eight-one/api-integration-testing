package dev.codersbox.testing.plugins.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.util.*

class KafkaProvider : MessagingProvider {
    private val objectMapper = ObjectMapper()

    override fun execute(request: Request): Response {
        val config = request.config as? KafkaConfig
            ?: throw IllegalArgumentException("Request config must be KafkaConfig")

        return when (config.operation) {
            KafkaOperation.PRODUCE -> produce(config, request)
            KafkaOperation.CONSUME -> consume(config)
        }
    }

    private fun produce(config: KafkaConfig, request: Request): Response {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            putAll(config.additionalProperties)
        }

        val startTime = System.currentTimeMillis()
        
        return try {
            KafkaProducer<String, String>(props).use { producer ->
                val record = ProducerRecord(
                    config.topic,
                    config.partition,
                    config.key,
                    request.body as? String ?: objectMapper.writeValueAsString(request.body)
                )

                config.headers.forEach { (key, value) ->
                    record.headers().add(key, value.toByteArray())
                }

                val metadata = producer.send(record).get()
                val responseTime = System.currentTimeMillis() - startTime

                Response(
                    statusCode = 200,
                    body = objectMapper.writeValueAsString(mapOf(
                        "topic" to metadata.topic(),
                        "partition" to metadata.partition(),
                        "offset" to metadata.offset(),
                        "timestamp" to metadata.timestamp()
                    )),
                    headers = mapOf("X-Kafka-Topic" to config.topic),
                    responseTime = responseTime
                )
            }
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "Kafka produce failed",
                headers = emptyMap(),
                error = e
            )
        }
    }

    private fun consume(config: KafkaConfig): Response {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, config.consumerGroup ?: "test-consumer-${UUID.randomUUID()}")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset ?: "earliest")
            putAll(config.additionalProperties)
        }

        val startTime = System.currentTimeMillis()
        val messages = mutableListOf<Map<String, Any?>>()

        return try {
            runBlocking {
                withTimeout(config.timeout) {
                    KafkaConsumer<String, String>(props).use { consumer ->
                        consumer.subscribe(listOf(config.topic))

                        val records = consumer.poll(Duration.ofMillis(config.timeout))
                        records.forEach { record ->
                            messages.add(mapOf(
                                "key" to record.key(),
                                "value" to record.value(),
                                "partition" to record.partition(),
                                "offset" to record.offset(),
                                "timestamp" to record.timestamp(),
                                "headers" to record.headers().associate { 
                                    it.key() to String(it.value()) 
                                }
                            ))
                        }
                    }
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
                    "X-Kafka-Topic" to config.topic,
                    "X-Message-Count" to messages.size.toString()
                ),
                responseTime = responseTime
            )
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "Kafka consume failed",
                headers = emptyMap(),
                error = e
            )
        }
    }

    override fun supports(protocol: String): Boolean = protocol.uppercase() == "KAFKA"
}

data class KafkaConfig(
    val bootstrapServers: String,
    val topic: String,
    val operation: KafkaOperation = KafkaOperation.PRODUCE,
    val key: String? = null,
    val partition: Int? = null,
    val headers: Map<String, String> = emptyMap(),
    val consumerGroup: String? = null,
    val autoOffsetReset: String? = "earliest",
    val timeout: Long = 5000L,
    val additionalProperties: Map<String, Any> = emptyMap()
)

enum class KafkaOperation {
    PRODUCE, CONSUME
}
