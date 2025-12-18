# API Integration Testing Framework

[![Maven Central](https://img.shields.io/maven-central/v/dev.codersbox.eng.lib/api-integration-testing-framework.svg)](https://search.maven.org/artifact/dev.codersbox.eng.lib/api-integration-testing-framework)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)

A powerful, extensible, and developer-friendly Kotlin DSL framework for API integration testing, load testing, and test automation. Built on top of Ktor and Kotest, this framework provides an intuitive way to write readable and maintainable API tests.

## 🚀 Features

### Core Features
- **Intuitive Kotlin DSL** - Write expressive and readable API tests
- **Multi-Protocol Support** - REST, GraphQL, gRPC, WebSocket, SOAP, Messaging (Kafka/RabbitMQ)
- **Multiple Content Formats** - JSON, XML, CSV, Protocol Buffers, YAML
- **Flexible Authentication** - Basic Auth, Bearer Token, OAuth 2.0, API Key, Custom Auth
- **Advanced Validation** - JSONPath, XPath, Regex, Custom matchers
- **Data-Driven Testing** - Support for CSV, JSON, YAML data sources
- **Load Testing** - Built-in performance testing capabilities
- **Smart Test Data** - Auto-generation using Kotlin Faker
- **Hooks & Lifecycle** - Before/After hooks at suite, scenario, and step levels
- **Resource Tracking** - Automatic cleanup of test data

### Developer Tools
- **IntelliJ IDEA Plugin** - Run scenarios, view reports, auto-completion, live templates
- **CLI Tool** - Run tests, generate reports, scaffold projects, auto-generate tests from OpenAPI
- **Reusable Steps** - Define and reuse common test steps
- **Scenario Chaining** - Chain scenarios with dependencies
- **Environment Management** - Multi-environment configuration support
- **Rich Reporting** - HTML, JSON, Allure, JUnit reports

### Extensibility
- **Plugin Architecture** - Protocol plugins, format plugins, auth plugins
- **Custom Validators** - Create domain-specific validators
- **Custom Matchers** - Build custom assertion matchers
- **Custom Plugins** - Extend the framework with your own plugins

## 📦 Project Structure

```
api-integration-testing/
├── core/                    # Core framework with DSL and engine
├── plugins/                 # Protocol and format plugins
│   ├── rest-plugin/        # REST API support
│   ├── graphql-plugin/     # GraphQL support
│   ├── grpc-plugin/        # gRPC support
│   ├── websocket-plugin/   # WebSocket support
│   ├── soap-plugin/        # SOAP support
│   ├── messaging-plugin/   # Kafka/RabbitMQ support
│   └── formats/            # Format plugins (JSON, XML, CSV, Protobuf)
├── cli/                    # Command-line tool
├── intellij-plugin/        # IntelliJ IDEA plugin
├── demo-project/           # Demonstration examples
├── examples/               # Additional examples
└── documentation/          # VitePress documentation
```

## 🏁 Quick Start

### Maven Dependency

**This is not yet available**
Add to your `pom.xml`:

```xml
<dependencies>
    <!-- Core Framework -->
    <dependency>
        <groupId>dev.codersbox.eng.lib</groupId>
        <artifactId>core</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    
    <!-- Protocol Plugins (choose what you need) -->
    <dependency>
        <groupId>dev.codersbox.eng.lib</groupId>
        <artifactId>rest-plugin</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>dev.codersbox.eng.lib</groupId>
        <artifactId>graphql-plugin</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Basic Example

```kotlin
import dev.codersbox.eng.lib.api.testing.dsl.*
import io.kotest.core.spec.style.FunSpec

class UserApiTest : FunSpec({
    test("User CRUD Operations") {
        apiTestSuite("User API Tests") {
            baseUrl = "https://api.example.com"
            
            scenario("Create and verify user") {
                var userId: String = ""
                
                step("Create new user") {
                    post("/users") {
                        headers {
                            "Content-Type" to "application/json"
                        }
                        body = """
                            {
                                "name": "John Doe",
                                "email": "john@example.com"
                            }
                        """
                    }.expect {
                        status(201)
                        jsonPath("$.name") equalTo "John Doe"
                        jsonPath("$.email") equalTo "john@example.com"
                    }.extract {
                        userId = jsonPath("$.id")
                    }
                }
                
                step("Get user by ID") {
                    get("/users/$userId") {
                        headers {
                            "Accept" to "application/json"
                        }
                    }.expect {
                        status(200)
                        jsonPath("$.id") equalTo userId
                        jsonPath("$.name") equalTo "John Doe"
                    }
                }
            }
        }
    }
})
```

## 📖 Documentation

Comprehensive documentation is available at [http://localhost:5173](http://localhost:5173) when running locally.

### Documentation Sections

- **Getting Started** - Installation, quick start, basic concepts
- **User Guide** - DSL reference, scenarios, assertions, validation
- **Protocol Plugins** - REST, GraphQL, gRPC, WebSocket, SOAP, Messaging
- **Format Plugins** - JSON, XML, CSV, Protocol Buffers
- **Authentication** - All supported auth mechanisms
- **Advanced Topics** - Custom plugins, validators, CI/CD integration
- **CLI Tool** - Command reference and usage
- **IntelliJ Plugin** - IDE integration guide
- **Examples** - Real-world test examples
- **API Reference** - Complete API documentation

### Running Documentation Locally

```bash
cd documentation
npm install
npm run dev
```

Visit `http://localhost:5173` in your browser.

## 🛠️ CLI Tool

The framework includes a powerful CLI tool for running tests, generating reports, and scaffolding projects.

### Installation

```bash
# Build the CLI
cd cli
mvn clean package

# Add to PATH or create alias
alias api-test='java -jar /path/to/cli-1.0.0-SNAPSHOT.jar'
```

### Usage Examples

```bash
# Run all tests
api-test run

# Run specific scenarios
api-test run --scenario "User Login"

# Generate tests from OpenAPI spec
api-test generate --openapi openapi.yaml --output src/test/kotlin

# Run load tests
api-test load-test --config load-test-config.yaml

# Generate HTML report
api-test report --format html --output reports/
```

## 💡 IntelliJ IDEA Plugin

Install the IntelliJ IDEA plugin for enhanced development experience:

### Features
- Run individual scenarios with gutter icons
- View test results in tool window
- Code completion for DSL
- Live templates for common patterns
- Quick actions and refactoring
- Environment configuration UI

### Installation

1. Open IntelliJ IDEA
2. Go to **Settings** → **Plugins**
3. Search for "API Integration Testing"
4. Install and restart

Or build from source:

```bash
cd intellij-plugin
./gradlew buildPlugin
# Install from disk: build/distributions/intellij-plugin-1.0.0-SNAPSHOT.zip
```

## 🧪 Load Testing

Built-in load testing capabilities:

```kotlin
loadTest {
    name = "User API Load Test"
    virtualUsers = 100
    duration = 60.seconds
    rampUpTime = 10.seconds
    
    scenario {
        weight = 1.0
        
        step("Get Users") {
            get("/users")
            expect { status(200) }
        }
    }
}
```

## 🔌 Creating Custom Plugins

### Protocol Plugin Example

```kotlin
class MyProtocolPlugin : ProtocolPlugin {
    override val name = "my-protocol"
    
    override fun createClient(config: ProtocolConfig): ProtocolClient {
        return MyProtocolClient(config)
    }
    
    override fun createDSL(): ProtocolDSL {
        return MyProtocolDSL()
    }
}
```

Register in `META-INF/services/dev.codersbox.eng.lib.api.testing.plugin.ProtocolPlugin`

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup

```bash
# Clone the repository
git clone https://github.com/codersbox/api-integration-testing.git
cd api-integration-testing

# Build the project
mvn clean install

# Run tests
mvn test

# Build documentation
cd documentation
npm install && npm run build
```

## 📋 Requirements

- JDK 17 or higher
- Maven 3.8+
- Kotlin 1.9.22+

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

Built with:
- [Kotlin](https://kotlinlang.org/) - Programming language
- [Ktor](https://ktor.io/) - HTTP client
- [Kotest](https://kotest.io/) - Testing framework
- [Jackson](https://github.com/FasterXML/jackson) - JSON/XML/CSV processing
- [Kotlin Faker](https://github.com/serpro69/kotlin-faker) - Test data generation
- [Clikt](https://ajalt.github.io/clikt/) - CLI framework

## 📞 Support

- **Documentation**: [http://localhost:5173](http://localhost:5173)
- **Issues**: [GitHub Issues](https://github.com/codersbox/api-integration-testing/issues)
- **Discussions**: [GitHub Discussions](https://github.com/codersbox/api-integration-testing/discussions)

## 🗺️ Roadmap

- [ ] Contract testing support (OpenAPI/Swagger validation)
- [ ] Mock server integration (WireMock)
- [ ] Parallel execution support
- [ ] Database validation helpers
- [ ] Cloud provider integrations (AWS, Azure, GCP)
- [ ] CI/CD templates (GitHub Actions, GitLab CI, Jenkins)
- [ ] Performance optimization
- [ ] More protocol plugins (MQTT, AMQP, etc.)

---

**Made with ❤️ at CodersBox**
