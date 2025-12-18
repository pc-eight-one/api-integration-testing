# Plugin Overview

The framework uses a plugin architecture for extensibility.

## Plugin Types

### 1. Protocol Plugins
Handle different communication protocols:
- REST (HTTP/HTTPS)
- GraphQL
- gRPC
- WebSocket
- SOAP
- Kafka
- RabbitMQ

### 2. Format Plugins
Parse and validate different data formats:
- JSON (built-in)
- XML
- CSV
- Protocol Buffers
- Avro

### 3. Authentication Plugins
Implement auth strategies:
- Basic Auth
- Bearer Token
- OAuth 2.0
- API Key
- JWT
- Custom

## Using Plugins

Add plugin dependency:
```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-graphql</artifactId>
    <version>1.0.0</version>
</dependency>
```

Use in tests:
```kotlin
scenario("GraphQL test") {
    step("Query") {
        graphql {
            query = "{ users { id name } }"
        }.expect {
            data.users.notEmpty()
        }
    }
}
```

## Creating Custom Plugins

See [Custom Plugins Guide](/advanced/custom-plugins) for creating your own.
