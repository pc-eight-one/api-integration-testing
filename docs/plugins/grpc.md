# gRPC Plugin

The gRPC plugin enables testing of gRPC services with full support for unary, streaming, and bidirectional calls.

## Installation

Add the gRPC plugin dependency:

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>plugin-grpc</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Protocol Buffer Setup

Define your `.proto` files:

**user.proto**
```protobuf
syntax = "proto3";

package example;

service UserService {
  rpc GetUser (GetUserRequest) returns (User);
  rpc CreateUser (CreateUserRequest) returns (User);
  rpc ListUsers (ListUsersRequest) returns (stream User);
  rpc StreamUsers (stream CreateUserRequest) returns (stream User);
}

message GetUserRequest {
  string id = 1;
}

message CreateUserRequest {
  string name = 1;
  string email = 2;
}

message User {
  string id = 1;
  string name = 2;
  string email = 3;
}

message ListUsersRequest {
  int32 page_size = 1;
  string page_token = 2;
}
```

## Basic Usage

### Unary RPC

```kotlin
import dev.codersbox.eng.lib.testing.plugins.grpc.*

apiTestSuite("gRPC User Service") {
    grpcChannel {
        host = "localhost"
        port = 9090
        usePlaintext = true // For testing without TLS
    }
    
    scenario("Get user") {
        step("Fetch user by ID") {
            grpcUnary<GetUserRequest, User> {
                service = "example.UserService"
                method = "GetUser"
                request = GetUserRequest.newBuilder()
                    .setId("123")
                    .build()
            }.expect {
                status isSuccess()
                response {
                    it.name == "John Doe"
                    it.email == "john@example.com"
                }
            }
        }
    }
}
```

### Creating Resources

```kotlin
scenario("Create user") {
    step("Send create request") {
        grpcUnary<CreateUserRequest, User> {
            service = "example.UserService"
            method = "CreateUser"
            request = CreateUserRequest.newBuilder()
                .setName("Jane Doe")
                .setEmail("jane@example.com")
                .build()
        }.expect {
            status isSuccess()
            response {
                it.id.isNotEmpty()
                it.name == "Jane Doe"
            }
        }.extractTo { userId = it.response.id }
    }
}
```

## Streaming RPCs

### Server Streaming

```kotlin
scenario("List users with streaming") {
    step("Stream users") {
        val users = mutableListOf<User>()
        
        grpcServerStream<ListUsersRequest, User> {
            service = "example.UserService"
            method = "ListUsers"
            request = ListUsersRequest.newBuilder()
                .setPageSize(10)
                .build()
            
            onMessage { user ->
                users.add(user)
                println("Received user: ${user.name}")
            }
            
            onComplete {
                println("Stream completed. Total users: ${users.size}")
            }
        }.expect {
            status isSuccess()
            messagesReceived greaterThan 0
        }
        
        // Validate collected users
        assert(users.size >= 10)
        assert(users.all { it.email.contains("@") })
    }
}
```

### Client Streaming

```kotlin
scenario("Bulk create users") {
    step("Stream multiple create requests") {
        grpcClientStream<CreateUserRequest, User> {
            service = "example.UserService"
            method = "BulkCreateUsers"
            
            // Send multiple requests
            requests {
                repeat(5) { i ->
                    send(CreateUserRequest.newBuilder()
                        .setName("User $i")
                        .setEmail("user$i@example.com")
                        .build())
                }
            }
        }.expect {
            status isSuccess()
            response {
                it.id.isNotEmpty()
            }
        }
    }
}
```

### Bidirectional Streaming

```kotlin
scenario("Real-time user sync") {
    step("Bidirectional stream") {
        val receivedUsers = mutableListOf<User>()
        
        grpcBidiStream<CreateUserRequest, User> {
            service = "example.UserService"
            method = "StreamUsers"
            
            // Send requests
            requests {
                repeat(3) { i ->
                    send(CreateUserRequest.newBuilder()
                        .setName("User $i")
                        .setEmail("user$i@example.com")
                        .build())
                    delay(100) // Simulate timing
                }
            }
            
            // Handle responses
            onMessage { user ->
                receivedUsers.add(user)
                println("Created: ${user.name}")
            }
        }.expect {
            status isSuccess()
            messagesReceived isEqualTo 3
        }
    }
}
```

## Metadata (Headers)

### Send Metadata

```kotlin
scenario("Send request with metadata") {
    step("Call with headers") {
        grpcUnary<GetUserRequest, User> {
            service = "example.UserService"
            method = "GetUser"
            request = GetUserRequest.newBuilder().setId("123").build()
            
            metadata {
                "authorization" to "Bearer token123"
                "x-request-id" to UUID.randomUUID().toString()
                "x-client-version" to "1.0.0"
            }
        }
    }
}
```

### Receive Metadata

```kotlin
scenario("Validate response metadata") {
    step("Check response headers") {
        grpcUnary<GetUserRequest, User> {
            service = "example.UserService"
            method = "GetUser"
            request = GetUserRequest.newBuilder().setId("123").build()
        }.expect {
            metadata {
                "content-type" contains "application/grpc"
                "x-rate-limit-remaining" { it.toInt() > 0 }
            }
        }
    }
}
```

## Error Handling

### Status Codes

```kotlin
scenario("Handle gRPC errors") {
    step("Request non-existent user") {
        grpcUnary<GetUserRequest, User> {
            service = "example.UserService"
            method = "GetUser"
            request = GetUserRequest.newBuilder().setId("invalid").build()
        }.expect {
            status isEqualTo Status.Code.NOT_FOUND
            statusDescription contains "User not found"
        }
    }
}
```

### Common Status Codes

```kotlin
// Success
status isEqualTo Status.Code.OK

// Client errors
status isEqualTo Status.Code.INVALID_ARGUMENT
status isEqualTo Status.Code.NOT_FOUND
status isEqualTo Status.Code.ALREADY_EXISTS
status isEqualTo Status.Code.PERMISSION_DENIED
status isEqualTo Status.Code.UNAUTHENTICATED

// Server errors
status isEqualTo Status.Code.INTERNAL
status isEqualTo Status.Code.UNAVAILABLE
status isEqualTo Status.Code.DEADLINE_EXCEEDED
```

## Authentication

### TLS/SSL

```kotlin
apiTestSuite("Secure gRPC") {
    grpcChannel {
        host = "api.example.com"
        port = 443
        useTls = true
        
        // Custom certificate
        tlsConfig {
            trustCertificates = File("ca-cert.pem")
        }
    }
}
```

### Token Authentication

```kotlin
apiTestSuite("Authenticated gRPC") {
    grpcChannel {
        host = "localhost"
        port = 9090
        
        // Add auth interceptor
        interceptor {
            CallCredentials.from { executor, applier ->
                executor.execute {
                    val metadata = Metadata()
                    metadata.put(
                        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER),
                        "Bearer your-token"
                    )
                    applier.apply(metadata)
                }
            }
        }
    }
}
```

### Mutual TLS

```kotlin
grpcChannel {
    host = "api.example.com"
    port = 443
    useTls = true
    
    tlsConfig {
        trustCertificates = File("ca-cert.pem")
        clientCertificates = File("client-cert.pem")
        clientPrivateKey = File("client-key.pem")
    }
}
```

## Deadlines & Timeouts

```kotlin
scenario("Handle timeouts") {
    step("Request with deadline") {
        grpcUnary<GetUserRequest, User> {
            service = "example.UserService"
            method = "GetUser"
            request = GetUserRequest.newBuilder().setId("123").build()
            
            // Set deadline
            deadline = 5.seconds
        }.expect {
            responseTime lessThan 5.seconds
        }
    }
}
```

## Load Testing

```kotlin
loadTestSuite("gRPC Performance") {
    grpcChannel {
        host = "localhost"
        port = 9090
    }
    
    loadConfig {
        virtualUsers = 100
        duration = 2.minutes
    }
    
    scenario("Load test user queries") {
        step("Concurrent requests") {
            grpcUnary<GetUserRequest, User> {
                service = "example.UserService"
                method = "GetUser"
                request = GetUserRequest.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .build()
            }.expect {
                responseTime lessThan 100.milliseconds
            }
        }
    }
}
```

## Interceptors

### Logging Interceptor

```kotlin
grpcChannel {
    interceptor {
        object : ClientInterceptor {
            override fun <ReqT, RespT> interceptCall(
                method: MethodDescriptor<ReqT, RespT>,
                callOptions: CallOptions,
                next: Channel
            ): ClientCall<ReqT, RespT> {
                println("Calling: ${method.fullMethodName}")
                return next.newCall(method, callOptions)
            }
        }
    }
}
```

### Retry Interceptor

```kotlin
grpcChannel {
    interceptor {
        RetryInterceptor(
            maxAttempts = 3,
            backoff = 100.milliseconds
        )
    }
}
```

## Best Practices

1. **Use Deadlines**: Always set reasonable deadlines for RPCs
2. **Handle Streams Properly**: Clean up resources in streaming calls
3. **Secure by Default**: Use TLS in production
4. **Structured Logging**: Log request IDs and trace information
5. **Test Error Cases**: Validate all gRPC status codes
6. **Connection Pooling**: Reuse channels when possible

## Next Steps

- [REST Plugin](./rest.md)
- [WebSocket Plugin](./websocket.md)
- [Load Testing](../guide/load-testing.md)
