# Messaging Plugin (Kafka/RabbitMQ)

The Messaging plugin provides comprehensive support for testing message queues including Kafka, RabbitMQ, and other messaging systems.

## Installation

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>plugin-messaging</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Apache Kafka

### Basic Producer

```kotlin
import dev.codersbox.eng.lib.testing.plugins.messaging.kafka.*

apiTestSuite("Kafka Testing") {
    kafka {
        bootstrapServers = "localhost:9092"
    }
    
    scenario("Produce message") {
        step("Send to topic") {
            kafkaProduce {
                topic = "user-events"
                key = "user123"
                value = """
                    {
                        "userId": "123",
                        "event": "user.created",
                        "timestamp": "${System.currentTimeMillis()}"
                    }
                """
            }.expect {
                success()
                partition isNotNull()
                offset greaterThan 0
            }
        }
    }
}
```

### Basic Consumer

```kotlin
scenario("Consume messages") {
    step("Read from topic") {
        kafkaConsume {
            topic = "user-events"
            groupId = "test-consumer-group"
            
            // Read for 10 seconds or until message received
            timeout = 10.seconds
            
            onMessage { record ->
                println("Received: key=${record.key()}, value=${record.value()}")
                
                val json = parseJson(record.value())
                assert(json["event"] == "user.created")
            }
        }.expect {
            messagesReceived greaterThan 0
        }
    }
}
```

### Producer-Consumer Pattern

```kotlin
scenario("End-to-end message flow") {
    val testId = UUID.randomUUID().toString()
    
    step("Start consumer") {
        kafkaConsumer("orders-topic", "test-group") {
            filter { record ->
                record.key() == testId
            }
            
            expectMessage(timeout = 30.seconds) { record ->
                val order = parseJson(record.value())
                order["orderId"] == testId
                order["status"] == "created"
            }
        }
    }
    
    step("Produce message") {
        kafkaProduce {
            topic = "orders-topic"
            key = testId
            value = """{"orderId": "$testId", "status": "created"}"""
        }
    }
}
```

### Headers

```kotlin
kafkaProduce {
    topic = "events"
    key = "event123"
    value = eventData
    
    headers {
        "event-type" to "user.created"
        "correlation-id" to UUID.randomUUID().toString()
        "timestamp" to System.currentTimeMillis().toString()
    }
}

// Consumer with header validation
kafkaConsume {
    topic = "events"
    
    onMessage { record ->
        val eventType = record.headers().lastHeader("event-type").value().decodeToString()
        assert(eventType == "user.created")
    }
}
```

### Partitions

```kotlin
scenario("Produce to specific partition") {
    step("Send to partition 0") {
        kafkaProduce {
            topic = "orders"
            partition = 0
            key = "order123"
            value = orderData
        }
    }
}
```

### Transactions

```kotlin
scenario("Transactional produce") {
    step("Send multiple messages atomically") {
        kafkaTransaction {
            transactionalId = "test-transaction"
            
            produce("topic1", "key1", "value1")
            produce("topic2", "key2", "value2")
            produce("topic3", "key3", "value3")
            
            commit()
        }.expect {
            success()
            messagesProduced isEqualTo 3
        }
    }
}
```

### Schema Registry (Avro)

```kotlin
scenario("Produce Avro message") {
    step("Send with schema") {
        kafkaProduce {
            topic = "users"
            key = "user123"
            
            avro {
                schemaRegistry = "http://localhost:8081"
                schema = UserAvroSchema
                value = User(id = "123", name = "John")
            }
        }
    }
}
```

## RabbitMQ

### Basic Producer

```kotlin
apiTestSuite("RabbitMQ Testing") {
    rabbitmq {
        host = "localhost"
        port = 5672
        username = "guest"
        password = "guest"
    }
    
    scenario("Publish message") {
        step("Send to exchange") {
            rabbitmqPublish {
                exchange = "user-exchange"
                routingKey = "user.created"
                message = """
                    {
                        "userId": "123",
                        "email": "user@example.com"
                    }
                """
            }.expect {
                success()
            }
        }
    }
}
```

### Basic Consumer

```kotlin
scenario("Consume from queue") {
    step("Read messages") {
        rabbitmqConsume {
            queue = "user-queue"
            autoAck = false
            
            onMessage { message ->
                println("Received: ${message.body}")
                
                val json = parseJson(message.body)
                assert(json["userId"] != null)
                
                // Manual acknowledge
                message.ack()
            }
            
            timeout = 10.seconds
        }
    }
}
```

### Exchange Types

#### Direct Exchange

```kotlin
rabbitmqPublish {
    exchange = "direct-exchange"
    exchangeType = ExchangeType.DIRECT
    routingKey = "error"
    message = errorMessage
}
```

#### Topic Exchange

```kotlin
rabbitmqPublish {
    exchange = "topic-exchange"
    exchangeType = ExchangeType.TOPIC
    routingKey = "logs.error.payment"
    message = logMessage
}

rabbitmqConsume {
    queue = "error-logs"
    binding {
        exchange = "topic-exchange"
        routingKey = "logs.error.*"
    }
}
```

#### Fanout Exchange

```kotlin
rabbitmqPublish {
    exchange = "notifications"
    exchangeType = ExchangeType.FANOUT
    message = notification
}
```

### Message Properties

```kotlin
rabbitmqPublish {
    exchange = "tasks"
    routingKey = "task.process"
    message = taskData
    
    properties {
        contentType = "application/json"
        priority = 5
        expiration = "60000" // milliseconds
        messageId = UUID.randomUUID().toString()
        correlationId = requestId
        replyTo = "response-queue"
        headers = mapOf(
            "x-retry-count" to 0,
            "x-created-by" to "test-suite"
        )
    }
}
```

### Dead Letter Queue

```kotlin
scenario("Handle failed messages") {
    step("Setup DLQ") {
        rabbitmqQueue {
            name = "main-queue"
            arguments = mapOf(
                "x-dead-letter-exchange" to "dlx-exchange",
                "x-dead-letter-routing-key" to "dlq"
            )
        }
    }
    
    step("Consume with rejection") {
        rabbitmqConsume {
            queue = "main-queue"
            
            onMessage { message ->
                // Simulate processing failure
                message.nack(requeue = false) // Send to DLQ
            }
        }
    }
    
    step("Verify DLQ") {
        rabbitmqConsume {
            queue = "dead-letter-queue"
            
            expectMessage { message ->
                message.body.isNotEmpty()
            }
        }
    }
}
```

### RPC Pattern

```kotlin
scenario("Request-Reply pattern") {
    val correlationId = UUID.randomUUID().toString()
    
    step("Send request") {
        rabbitmqPublish {
            exchange = ""
            routingKey = "rpc-queue"
            message = """{"operation": "calculate", "value": 42}"""
            
            properties {
                replyTo = "response-queue"
                correlationId = correlationId
            }
        }
    }
    
    step("Wait for response") {
        rabbitmqConsume {
            queue = "response-queue"
            
            expectMessage(timeout = 5.seconds) { message ->
                message.properties.correlationId == correlationId
                parseJson(message.body)["result"] != null
            }
        }
    }
}
```

## AWS SQS/SNS

### SQS Producer

```kotlin
apiTestSuite("AWS SQS") {
    sqs {
        region = "us-east-1"
        accessKey = System.getenv("AWS_ACCESS_KEY")
        secretKey = System.getenv("AWS_SECRET_KEY")
    }
    
    scenario("Send to SQS") {
        step("Produce message") {
            sqsSend {
                queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789/test-queue"
                messageBody = """{"event": "user.created"}"""
                
                attributes {
                    "EventType" to "UserCreated"
                }
            }
        }
    }
}
```

### SQS Consumer

```kotlin
scenario("Consume from SQS") {
    step("Poll messages") {
        sqsReceive {
            queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789/test-queue"
            maxMessages = 10
            waitTimeSeconds = 20 // Long polling
            
            onMessage { message ->
                println(message.body())
                
                // Delete after processing
                message.delete()
            }
        }
    }
}
```

### SNS Publish

```kotlin
scenario("Publish to SNS") {
    step("Send notification") {
        snsPublish {
            topicArn = "arn:aws:sns:us-east-1:123456789:test-topic"
            message = """{"notification": "System update"}"""
            subject = "System Alert"
        }
    }
}
```

## Load Testing

### Kafka Load Test

```kotlin
loadTestSuite("Kafka Performance") {
    kafka {
        bootstrapServers = "localhost:9092"
    }
    
    loadConfig {
        virtualUsers = 100
        duration = 5.minutes
    }
    
    scenario("High-throughput produce") {
        step("Produce messages") {
            kafkaProduce {
                topic = "load-test"
                key = UUID.randomUUID().toString()
                value = fakeOrderData()
            }.expect {
                success()
                latency lessThan 50.milliseconds
            }
        }
    }
}
```

### RabbitMQ Load Test

```kotlin
loadTestSuite("RabbitMQ Performance") {
    rabbitmq {
        host = "localhost"
    }
    
    loadConfig {
        virtualUsers = 50
        duration = 2.minutes
    }
    
    scenario("Message throughput") {
        step("Publish load") {
            rabbitmqPublish {
                exchange = "load-test"
                routingKey = "test"
                message = fakeMessage()
            }
        }
    }
}
```

## Best Practices

1. **Consumer Groups**: Use unique group IDs for each test
2. **Cleanup**: Delete test topics/queues after tests
3. **Idempotency**: Design tests to be idempotent
4. **Timeouts**: Set appropriate timeouts for message consumption
5. **Correlation IDs**: Use correlation IDs to track messages
6. **Isolation**: Isolate test data with unique keys/topics
7. **Error Handling**: Test both success and failure scenarios

## Next Steps

- [WebSocket Plugin](./websocket.md)
- [gRPC Plugin](./grpc.md)
- [Load Testing](../guide/load-testing.md)
