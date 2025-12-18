package dev.codersbox.testing.plugins.messaging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MessagingPluginExampleTest : FunSpec({
    
    test("Kafka Provider Configuration") {
        val config = KafkaConfig(
            bootstrapServers = "localhost:9092",
            topic = "test-topic",
            operation = KafkaOperation.PRODUCE,
            key = "test-key",
            headers = mapOf("correlation-id" to "12345")
        )
        
        config.topic shouldBe "test-topic"
        config.bootstrapServers shouldBe "localhost:9092"
    }
    
    test("RabbitMQ Provider Configuration") {
        val config = RabbitMQConfig(
            host = "localhost",
            port = 5672,
            queue = "test-queue",
            exchange = "test-exchange",
            routingKey = "test.routing.key",
            operation = RabbitMQOperation.PUBLISH
        )
        
        config.queue shouldBe "test-queue"
        config.exchange shouldBe "test-exchange"
    }
    
    test("MQTT Provider Configuration") {
        val config = MqttConfig(
            brokerUrl = "tcp://localhost:1883",
            topic = "test/topic",
            operation = MqttOperation.PUBLISH,
            qos = 1,
            retained = false
        )
        
        config.topic shouldBe "test/topic"
        config.qos shouldBe 1
    }
    
    test("Messaging Plugin Supports Protocols") {
        val plugin = MessagingPlugin()
        
        plugin.supports(
            dev.codersbox.testing.core.request.Request(
                method = "POST",
                url = "kafka://localhost:9092",
                protocol = "KAFKA"
            )
        ) shouldBe true
        
        plugin.supports(
            dev.codersbox.testing.core.request.Request(
                method = "POST",
                url = "amqp://localhost:5672",
                protocol = "RABBITMQ"
            )
        ) shouldBe true
    }
})
