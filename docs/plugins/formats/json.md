# JSON Format Plugin

The JSON format plugin provides comprehensive support for JSON serialization and deserialization using Jackson.

## Overview

- **Plugin Name**: `json`
- **Content Type**: `application/json`
- **Implementation**: Uses Jackson for JSON processing
- **Default**: Yes (used when no format is specified)

## Features

- ✅ Automatic serialization of Kotlin data classes
- ✅ Support for complex nested structures
- ✅ Custom serialization/deserialization
- ✅ JsonPath support for validation
- ✅ Pretty printing for debugging
- ✅ Null handling and optional fields

## Basic Usage

### Sending JSON Requests

```kotlin
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int? = null
)

scenario("Create user with JSON") {
    step("Send JSON request") {
        post("/api/users") {
            contentType = "application/json"
            body = CreateUserRequest(
                name = "John Doe",
                email = "john@example.com",
                age = 30
            )
        }.expect {
            status(201)
        }
    }
}
```

### Using Raw JSON Strings

```kotlin
scenario("Send raw JSON") {
    step("Post raw JSON string") {
        post("/api/users") {
            contentType = "application/json"
            body = """
                {
                    "name": "John Doe",
                    "email": "john@example.com",
                    "age": 30
                }
            """.trimIndent()
        }
    }
}
```

### Using Maps and Lists

```kotlin
scenario("Send JSON from collections") {
    step("Post map as JSON") {
        post("/api/users") {
            contentType = "application/json"
            body = mapOf(
                "name" to "John Doe",
                "email" to "john@example.com",
                "tags" to listOf("developer", "kotlin")
            )
        }
    }
}
```

## Response Validation

### JsonPath Expressions

```kotlin
scenario("Validate JSON response") {
    step("Check user data") {
        get("/api/users/1").expect {
            status(200)
            jsonPath("$.name") shouldBe "John Doe"
            jsonPath("$.email") shouldContain "@example.com"
            jsonPath("$.age") shouldBeGreaterThan 18
            jsonPath("$.tags") shouldHaveSize 2
            jsonPath("$.tags[0]") shouldBe "developer"
        }
    }
}
```

### Type-Safe Deserialization

```kotlin
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int?
)

scenario("Type-safe response") {
    step("Parse to data class") {
        get("/api/users/1").expect {
            status(200)
            body<User> { user ->
                user.id shouldBe 1
                user.name shouldBe "John Doe"
                user.email shouldContain "@example.com"
                user.age shouldBeGreaterThan 18
            }
        }
    }
}
```

### Array Validation

```kotlin
scenario("Validate JSON array") {
    step("Check user list") {
        get("/api/users").expect {
            status(200)
            jsonPath("$") shouldBeArray()
            jsonPath("$.length()") shouldBeGreaterThan 0
            jsonPath("$[*].name") shouldContain "John Doe"
            
            bodyList<User> { users ->
                users shouldNotBeEmpty()
                users.first().name shouldBe "John Doe"
            }
        }
    }
}
```

## Advanced Features

### Custom Jackson Configuration

```kotlin
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

apiTestSuite("Custom JSON config") {
    beforeSuite {
        // Configure Jackson ObjectMapper
        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
            enable(SerializationFeature.INDENT_OUTPUT)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
        
        context["objectMapper"] = objectMapper
    }
}
```

### Date/Time Handling

```kotlin
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class Event(
    val name: String,
    val timestamp: LocalDateTime,
    val scheduledAt: ZonedDateTime
)

scenario("Handle date/time in JSON") {
    step("Create event") {
        post("/api/events") {
            body = Event(
                name = "Deployment",
                timestamp = LocalDateTime.now(),
                scheduledAt = ZonedDateTime.now().plusDays(1)
            )
        }.expect {
            status(201)
            jsonPath("$.timestamp") shouldMatch "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"
        }
    }
}
```

### Nested Objects

```kotlin
data class Address(
    val street: String,
    val city: String,
    val country: String
)

data class UserWithAddress(
    val name: String,
    val email: String,
    val address: Address
)

scenario("Nested JSON objects") {
    step("Create user with address") {
        post("/api/users") {
            body = UserWithAddress(
                name = "John Doe",
                email = "john@example.com",
                address = Address(
                    street = "123 Main St",
                    city = "New York",
                    country = "USA"
                )
            )
        }.expect {
            status(201)
            jsonPath("$.address.city") shouldBe "New York"
            jsonPath("$.address.country") shouldBe "USA"
        }
    }
}
```

### Partial Updates (PATCH)

```kotlin
scenario("Partial JSON update") {
    step("Update only email") {
        patch("/api/users/1") {
            body = mapOf("email" to "newemail@example.com")
        }.expect {
            status(200)
            jsonPath("$.email") shouldBe "newemail@example.com"
        }
    }
}
```

## JsonPath Syntax

Common JsonPath expressions:

| Expression | Description | Example |
|------------|-------------|---------|
| `$` | Root element | `$.name` |
| `$.field` | Direct child | `$.user.name` |
| `$[0]` | Array element | `$[0].name` |
| `$[*]` | All array elements | `$[*].name` |
| `$..field` | Recursive descent | `$..email` |
| `$[?(@.age > 18)]` | Filter | `$[?(@.status == 'active')]` |

## Testing Tips

### Pretty Print for Debugging

```kotlin
scenario("Debug JSON") {
    step("View formatted response") {
        get("/api/users/1").expect {
            status(200)
            printBody() // Pretty prints JSON
        }
    }
}
```

### Schema Validation

```kotlin
scenario("Validate JSON schema") {
    step("Check response structure") {
        get("/api/users/1").expect {
            status(200)
            hasJsonPath("$.id")
            hasJsonPath("$.name")
            hasJsonPath("$.email")
            hasJsonPath("$.createdAt")
        }
    }
}
```

## Configuration

### Default Configuration

```properties
# application.properties
json.prettyPrint=false
json.includeNulls=false
json.dateFormat=yyyy-MM-dd'T'HH:mm:ss.SSSZ
```

### Programmatic Configuration

```kotlin
apiTestSuite("JSON Config") {
    config {
        json {
            prettyPrint = true
            includeNulls = false
            dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        }
    }
}
```

## See Also

- [XML Format Plugin](xml.md)
- [CSV Format Plugin](csv.md)
- [Custom Format Plugins](../../advanced/custom-plugins.md)
- [Assertions & Validation](../../guide/assertions.md)
