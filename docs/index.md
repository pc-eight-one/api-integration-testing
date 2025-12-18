---
layout: home

hero:
  name: API Integration Testing Framework
  text: Powerful Kotlin DSL for Modern API Testing
  tagline: A comprehensive, plugin-based framework for REST, GraphQL, gRPC, WebSocket, SOAP, and more
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: View on GitHub
      link: https://github.com/codersbox/api-integration-testing

features:
  - icon: 🚀
    title: Intuitive Kotlin DSL
    details: Write expressive, readable tests with a fluent DSL that makes API testing natural and enjoyable.
  
  - icon: 🔌
    title: Plugin Architecture
    details: Extensible plugin system supporting REST, GraphQL, gRPC, WebSocket, SOAP, Kafka, RabbitMQ, and more.
  
  - icon: 📊
    title: Data-Driven Testing
    details: Support for parameterized tests with CSV, JSON, YAML data sources and inline data tables.
  
  - icon: 🎯
    title: Smart Validation
    details: Powerful assertion engine with JSON Path, XPath, custom matchers, and schema validation.
  
  - icon: ⚡
    title: Load Testing
    details: Built-in performance testing with configurable concurrency, ramp-up, and detailed metrics.
  
  - icon: 🔐
    title: Authentication Support
    details: Multiple auth strategies including OAuth 2.0, JWT, Basic Auth, API Keys, and custom handlers.
  
  - icon: 🧪
    title: Test Data Management
    details: Faker integration, resource tracking, automatic cleanup, and smart test data builders.
  
  - icon: 🔄
    title: Reusable Components
    details: Shared steps, scenario chaining, lifecycle hooks, and extensible step definitions.
  
  - icon: 📝
    title: Rich Reporting
    details: Detailed test reports with request/response logging, timing metrics, and CI/CD integration.
---

## Quick Example

```kotlin
apiTestSuite("User Management API") {
    baseUrl = "https://api.example.com"
    
    scenario("Create and verify user") {
        step("Create new user") {
            post("/users") {
                body = User(
                    name = faker.name(),
                    email = faker.email()
                )
            }.expect {
                status(201)
                jsonPath("$.id").exists()
                jsonPath("$.email").isValidEmail()
            }.extract {
                context["userId"] = jsonPath("$.id")
            }
        }
        
        step("Verify user exists") {
            get("/users/${context["userId"]}") {}.expect {
                status(200)
                jsonPath("$.name").notEmpty()
            }
        }
    }
}
```

## Why This Framework?

### 🎨 Beautiful DSL
Write tests that read like documentation. The Kotlin DSL is designed for clarity and expressiveness.

### 🔧 Highly Extensible
Plugin architecture allows you to extend the framework with custom protocols, formats, validators, and more.

### 📦 Production Ready
Battle-tested features including lifecycle hooks, resource cleanup, retry logic, and comprehensive error handling.

### 🚄 Performance Focused
Built-in load testing capabilities with configurable concurrency, detailed metrics, and performance assertions.

### 🤝 Team Friendly
Reusable components, shared context, and clear patterns make it easy for teams to collaborate.

## Supported Protocols

- **REST** - Full HTTP/HTTPS support with all methods
- **GraphQL** - Queries, mutations, subscriptions
- **gRPC** - Unary, streaming, bidirectional
- **WebSocket** - Real-time bidirectional communication
- **SOAP** - Legacy SOAP 1.1/1.2 support
- **Messaging** - Kafka, RabbitMQ, and more

## Supported Formats

- **JSON** - Native support with JsonPath
- **XML** - Full XML support with XPath
- **CSV** - CSV parsing and validation
- **Protocol Buffers** - Binary format support
- **Custom Formats** - Extensible format system

## Get Started

```bash
# Add to your pom.xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-core</artifactId>
    <version>1.0.0</version>
</dependency>

# Add plugins as needed
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-rest</artifactId>
    <version>1.0.0</version>
</dependency>
```

[Get Started →](/guide/getting-started)
