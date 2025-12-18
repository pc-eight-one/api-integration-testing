# Installation

Detailed installation guide for different build tools and setups.

## Maven Installation

### Basic Setup

```xml
<dependencies>
    <dependency>
        <groupId>dev.codersbox.eng.lib</groupId>
        <artifactId>api-testing-core</artifactId>
        <version>1.0.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### With Plugins

```xml
<!-- REST Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-rest</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- GraphQL Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-graphql</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- gRPC Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-grpc</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- WebSocket Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-websocket</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Kafka Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-kafka</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- RabbitMQ Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-rabbitmq</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- SOAP Plugin -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-soap</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Format Plugins

```xml
<!-- XML -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-format-xml</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- CSV -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-format-csv</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Protocol Buffers -->
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-format-protobuf</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Gradle Installation

```kotlin
dependencies {
    testImplementation("dev.codersbox.eng.lib:api-testing-core:1.0.0")
    testImplementation("dev.codersbox.eng.lib:api-testing-plugin-rest:1.0.0")
    testImplementation("dev.codersbox.eng.lib:api-testing-plugin-graphql:1.0.0")
}
```

## Version Management

Use BOM (Bill of Materials) for consistent versions:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>dev.codersbox.eng.lib</groupId>
            <artifactId>api-testing-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then omit versions from dependencies:

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-core</artifactId>
    <scope>test</scope>
</dependency>
```

## Verification

Run this test to verify installation:

```kotlin
@Test
fun `verify framework installation`() {
    apiTestSuite("Installation Test") {
        baseUrl = "https://httpbin.org"
        scenario("Simple test") {
            step("GET request") {
                get("/get") {}.expect { status(200) }
            }
        }
    }.execute()
}
```
