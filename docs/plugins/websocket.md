# WebSocket Plugin

The WebSocket plugin provides comprehensive support for testing WebSocket APIs with real-time messaging capabilities.

## Installation

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>plugin-websocket</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Basic Usage

### Connect and Send

```kotlin
import dev.codersbox.eng.lib.testing.plugins.websocket.*

apiTestSuite("WebSocket Chat API") {
    scenario("Connect and send message") {
        step("Establish connection") {
            websocket("ws://localhost:8080/chat") {
                onConnect {
                    println("Connected to WebSocket")
                }
                
                send("""{"type": "join", "room": "general"}""")
                
                expectMessage { message ->
                    message.contains("Welcome")
                }
            }
        }
    }
}
```

### Receive Messages

```kotlin
scenario("Receive real-time updates") {
    step("Listen for messages") {
        websocket("ws://localhost:8080/updates") {
            val messages = mutableListOf<String>()
            
            onMessage { message ->
                println("Received: $message")
                messages.add(message)
            }
            
            // Keep connection open for 10 seconds
            duration = 10.seconds
            
            onClose {
                assert(messages.size > 0)
            }
        }
    }
}
```

## Advanced Features

### Binary Messages

```kotlin
scenario("Send binary data") {
    step("Send binary frame") {
        websocket("ws://localhost:8080/binary") {
            sendBinary(byteArrayOf(0x01, 0x02, 0x03))
            
            expectBinaryMessage { bytes ->
                bytes.size > 0
            }
        }
    }
}
```

### Message Filtering

```kotlin
scenario("Filter specific messages") {
    step("Listen for events") {
        websocket("ws://localhost:8080/events") {
            onMessage { message ->
                val json = parseJson(message)
                if (json["type"] == "user_joined") {
                    println("User joined: ${json["username"]}")
                }
            }
            
            // Only assert on specific message type
            expectMessage(filter = { it.contains("\"type\":\"user_joined\"") }) {
                it.contains("username")
            }
        }
    }
}
```

### Ping/Pong

```kotlin
scenario("Heartbeat with ping/pong") {
    step("Send periodic pings") {
        websocket("ws://localhost:8080/ws") {
            // Send ping every 5 seconds
            pingInterval = 5.seconds
            
            onPong {
                println("Pong received")
            }
            
            duration = 30.seconds
        }
    }
}
```

## Authentication

### Token-Based Auth

```kotlin
scenario("Connect with auth token") {
    step("Authenticated connection") {
        websocket("ws://localhost:8080/ws") {
            headers {
                "Authorization" to "Bearer $token"
            }
            
            onConnect {
                send("""{"action": "subscribe", "channel": "private"}""")
            }
        }
    }
}
```

### Query Parameters

```kotlin
websocket("ws://localhost:8080/ws?token=$token&user=$userId") {
    onConnect {
        println("Authenticated via query params")
    }
}
```

## Request/Response Pattern

```kotlin
scenario("Request-response over WebSocket") {
    step("Send request and wait for response") {
        websocket("ws://localhost:8080/rpc") {
            val requestId = UUID.randomUUID().toString()
            
            send("""
                {
                    "id": "$requestId",
                    "method": "getUser",
                    "params": {"userId": "123"}
                }
            """)
            
            expectMessage(timeout = 5.seconds) { message ->
                val json = parseJson(message)
                json["id"] == requestId && json["result"] != null
            }
        }
    }
}
```

## Pub/Sub Testing

```kotlin
scenario("Test publish/subscribe") {
    val subscriber = step("Subscribe to topic") {
        websocket("ws://localhost:8080/pubsub") {
            val receivedMessages = mutableListOf<String>()
            
            // Subscribe
            send("""{"action": "subscribe", "topic": "notifications"}""")
            
            onMessage { message ->
                if (message.contains("notification")) {
                    receivedMessages.add(message)
                }
            }
            
            duration = 20.seconds
        }
    }
    
    step("Publish message") {
        delay(2.seconds) // Wait for subscription
        
        post("/api/publish") {
            body = """{"topic": "notifications", "message": "Test"}"""
        }
    }
    
    // Verify subscriber received it
    delay(1.second)
}
```

## Error Handling

### Connection Failures

```kotlin
scenario("Handle connection errors") {
    step("Connect to invalid endpoint") {
        try {
            websocket("ws://invalid-host:9999/ws") {
                onConnect {
                    fail("Should not connect")
                }
            }
        } catch (e: WebSocketException) {
            println("Expected error: ${e.message}")
        }
    }
}
```

### Close Codes

```kotlin
scenario("Validate close codes") {
    step("Normal closure") {
        websocket("ws://localhost:8080/ws") {
            send("""{"action": "disconnect"}""")
            
            onClose { code, reason ->
                assert(code == 1000) // Normal closure
                assert(reason == "Goodbye")
            }
        }
    }
}
```

## Load Testing

```kotlin
loadTestSuite("WebSocket Performance") {
    loadConfig {
        virtualUsers = 100
        duration = 5.minutes
    }
    
    scenario("Concurrent connections") {
        step("Connect and exchange messages") {
            websocket("ws://localhost:8080/ws") {
                onConnect {
                    // Send message every second
                    repeat(60) {
                        send("""{"type": "ping"}""")
                        delay(1.second)
                    }
                }
                
                onMessage { message ->
                    // Track response time
                }
                
                duration = 60.seconds
            }.expect {
                messagesReceived greaterThan 50
                avgLatency lessThan 100.milliseconds
            }
        }
    }
}
```

## Reconnection

```kotlin
scenario("Auto-reconnect on disconnect") {
    step("Test reconnection") {
        websocket("ws://localhost:8080/ws") {
            reconnect {
                enabled = true
                maxAttempts = 3
                delay = 2.seconds
            }
            
            onReconnect { attempt ->
                println("Reconnection attempt: $attempt")
            }
            
            onConnect {
                send("""{"action": "subscribe"}""")
            }
        }
    }
}
```

## Best Practices

1. **Set Timeouts**: Always configure duration or message timeouts
2. **Clean Closure**: Close connections properly with correct codes
3. **Handle Reconnects**: Implement retry logic for production tests
4. **Message Ordering**: Don't assume message order in concurrent scenarios
5. **Resource Cleanup**: Ensure connections are closed after tests
6. **Binary vs Text**: Use appropriate frame type for your data

## Next Steps

- [gRPC Plugin](./grpc.md)
- [Messaging Plugin](./messaging.md)
- [Load Testing](../guide/load-testing.md)
