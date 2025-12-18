# DSL Reference

Complete Kotlin DSL syntax reference.

## Test Suite Structure

```kotlin
apiTestSuite("name") {
    baseUrl = "https://api.example.com"
    
    scenario("scenario name") {
        step("step name") {
            get("/path").expect {
                status(200)
            }
        }
    }
}
```

## HTTP Methods

- `get(path)`
- `post(path)`
- `put(path)`
- `patch(path)`
- `delete(path)`

## Request Building

```kotlin
post("/api/users") {
    header("Content-Type", "application/json")
    json {
        "name" to "John"
        "email" to "john@example.com"
    }
}
```

## Response Validation

```kotlin
response.expect {
    status(200)
    jsonPath("$.id").isNotEmpty()
    jsonPath("$.name").equals("John")
    header("Content-Type", "application/json")
    responseTime(lessThan = 1.seconds)
}
```

## Complete Examples

See [Examples](../examples/rest-api-examples.md) for comprehensive usage.

## Next Steps

- [Core API](core-api.md)
- [Examples](../examples/rest-api-examples.md)
- [Quick Start](../getting-started/quick-start.md)
