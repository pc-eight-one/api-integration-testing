# Assertions & Validation

The framework provides a rich set of assertion capabilities to validate API responses comprehensively.

## Basic Assertions

### Status Code Validation

```kotlin
apiTestSuite("User API") {
    scenario("Create user returns 201") {
        step("Create user") {
            post("/users") {
                body = """{"name": "John", "email": "john@example.com"}"""
            }.expect {
                status(201)
                // or use predicates
                status { it in 200..299 }
            }
        }
    }
}
```

### Response Body Validation

```kotlin
step("Validate user data") {
    get("/users/123").expect {
        // JSON path assertions
        jsonPath("$.id") isEqualTo "123"
        jsonPath("$.name") isEqualTo "John Doe"
        jsonPath("$.email") matches ".*@example\\.com"
        
        // Nested paths
        jsonPath("$.address.city") isEqualTo "New York"
        
        // Array operations
        jsonPath("$.tags") hasSize 3
        jsonPath("$.tags[0]") isEqualTo "developer"
    }
}
```

## Advanced Assertions

### Custom Matchers

```kotlin
// Email validation
jsonPath("$.email") isValidEmail()

// URL validation
jsonPath("$.website") isValidUrl()

// Date/Time validation
jsonPath("$.createdAt") isValidIsoDateTime()
jsonPath("$.createdAt") isAfter "2024-01-01T00:00:00Z"

// Numeric comparisons
jsonPath("$.age") isGreaterThan 18
jsonPath("$.price") isBetween 10.0..100.0
```

### Collection Assertions

```kotlin
step("Validate user list") {
    get("/users").expect {
        // Array size
        jsonPath("$.users") hasSize 10
        jsonPath("$.users").arraySize greaterThan 5
        
        // Contains checks
        jsonPath("$.users[*].name") contains "John"
        jsonPath("$.users[*].email") containsAll listOf("john@test.com", "jane@test.com")
        
        // All items match
        jsonPath("$.users[*].age") allMatch { it as Int > 18 }
        
        // Any item matches
        jsonPath("$.users[*].role") anyMatch { it == "admin" }
    }
}
```

### Header Assertions

```kotlin
step("Validate headers") {
    get("/api/resource").expect {
        headers {
            "Content-Type" isEqualTo "application/json"
            "X-RateLimit-Remaining" { it.toInt() > 0 }
            "Cache-Control" contains "no-cache"
            "ETag" isNotEmpty()
        }
    }
}
```

### Response Time Assertions

```kotlin
step("Check performance") {
    get("/api/fast-endpoint").expect {
        responseTime lessThan 500.milliseconds
        responseTime inRange 100.milliseconds..1.seconds
    }
}
```

## Schema Validation

### JSON Schema Validation

```kotlin
step("Validate against schema") {
    get("/users/123").expect {
        matchesJsonSchema("""
            {
              "type": "object",
              "required": ["id", "name", "email"],
              "properties": {
                "id": { "type": "string" },
                "name": { "type": "string" },
                "email": { "type": "string", "format": "email" },
                "age": { "type": "integer", "minimum": 0 }
              }
            }
        """)
    }
}

// Or from file
get("/users/123").expect {
    matchesJsonSchema(File("schemas/user-schema.json"))
}
```

### Contract Testing

```kotlin
step("Validate OpenAPI contract") {
    post("/users") {
        body = newUser
    }.expect {
        matchesContract("openapi.yaml", "/users", "post")
    }
}
```

## Best Practices

1. **Be Specific**: Use precise assertions rather than broad checks
2. **Test Edge Cases**: Include boundary values and invalid inputs
3. **Use Schema Validation**: For complex responses, schema validation is more maintainable
4. **Meaningful Messages**: Provide clear error messages for custom assertions
5. **Avoid Over-Assertion**: Don't validate every field if not necessary

## Next Steps

- [Test Data Management](./test-data.md)
- [Lifecycle Hooks](./lifecycle-hooks.md)
- [Data-Driven Testing](./data-driven-testing.md)
