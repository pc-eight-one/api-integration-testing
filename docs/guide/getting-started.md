# Getting Started

This guide will help you set up and start using the API Integration Testing Framework in your project.

## Prerequisites

- **Java**: JDK 11 or higher
- **Build Tool**: Maven 3.6+
- **IDE**: IntelliJ IDEA (recommended for Kotlin support)

## Installation

### Maven Setup

```xml
<dependencies>
    <!-- Core Framework -->
    <dependency>
        <groupId>dev.codersbox.eng.lib</groupId>
        <artifactId>api-testing-core</artifactId>
        <version>1.0.0</version>
        <scope>test</scope>
    </dependency>
    
    <!-- REST Plugin -->
    <dependency>
        <groupId>dev.codersbox.eng.lib</groupId>
        <artifactId>api-testing-plugin-rest</artifactId>
        <version>1.0.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Your First Test

```kotlin
package com.yourcompany.api.tests

import dev.codersbox.testing.api.core.dsl.apiTestSuite
import org.junit.jupiter.api.Test

class UserApiTest {
    
    @Test
    fun `should create and retrieve user`() {
        apiTestSuite("User API Tests") {
            baseUrl = "https://jsonplaceholder.typicode.com"
            
            scenario("Create and verify user") {
                var userId: String? = null
                
                step("Create new user") {
                    post("/users") {
                        body = mapOf(
                            "name" to "John Doe",
                            "email" to "john@example.com"
                        )
                    }.expect {
                        status(201)
                        jsonPath("$.id").exists()
                    }.extract {
                        userId = jsonPath("$.id")
                    }
                }
                
                step("Get created user") {
                    get("/users/$userId") {
                    }.expect {
                        status(200)
                        jsonPath("$.name").equals("John Doe")
                    }
                }
            }
        }.execute()
    }
}
```

## Run the Test

```bash
mvn test
```

## Next Steps

1. **Learn Core Concepts**: [Architecture](/guide/architecture)
2. **Explore Features**: [Data-driven testing](/guide/data-driven-testing)
3. **Try Examples**: [REST examples](/examples/rest-examples)
