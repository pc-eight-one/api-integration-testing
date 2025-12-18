# What is API Integration Testing Framework?

The **API Integration Testing Framework** is a powerful, extensible Kotlin-based testing solution designed for modern API testing scenarios. It provides an intuitive DSL (Domain-Specific Language) that makes writing integration tests, load tests, and end-to-end API tests straightforward and maintainable.

## Problem Statement

Modern applications rely on multiple APIs and services:
- REST APIs
- GraphQL endpoints
- gRPC services
- WebSocket connections
- Message queues (Kafka, RabbitMQ)
- SOAP services

Testing these diverse protocols traditionally requires:
- Multiple testing tools and frameworks
- Duplicated test infrastructure
- Inconsistent test patterns
- Complex setup and configuration

## Our Solution

This framework provides a unified testing interface with a single, consistent DSL to test any protocol.

## Key Features

### Expressive DSL
```kotlin
apiTestSuite("E-Commerce API") {
    baseUrl = "https://api.shop.com"
    
    scenario("Complete purchase flow") {
        step("Add item to cart") {
            post("/cart/items") {
                body = CartItem(productId = 123, quantity = 2)
            }.expect {
                status(201)
                jsonPath("$.total").greaterThan(0)
            }
        }
    }
}
```

### Multi-Protocol Support
- REST, GraphQL, gRPC, WebSocket, SOAP, Kafka, RabbitMQ

### Plugin Architecture
Extend with custom protocols, formats, auth strategies, and validators

### Enterprise Features
- Data-driven testing
- Load testing
- Lifecycle hooks
- Resource tracking
- Smart assertions
- Rich context sharing

## Next Steps

- [Getting Started Guide](/guide/getting-started)
- [Quick Start Tutorial](/guide/quick-start)
- [Installation Instructions](/guide/installation)
