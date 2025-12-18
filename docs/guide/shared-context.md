# Shared Context

The framework provides a shared context for passing data between steps and scenarios.

## Basic Usage

```kotlin
scenario("User flow") {
    step("Create user") {
        post("/users") { body = user }.expect {
            status(201)
        }.extract {
            context["userId"] = jsonPath("$.id")
            context["userName"] = jsonPath("$.name")
        }
    }
    
    step("Get user") {
        get("/users/${context["userId"]}") {}.expect {
            jsonPath("$.name").equals(context["userName"])
        }
    }
}
```

## Context Methods

- `context[key] = value` - Store value
- `context.get<T>(key)` - Retrieve typed value
- `context.getString(key)` - Get as string
- `context.getInt(key)` - Get as int
- `context.has(key)` - Check existence
- `context.remove(key)` - Remove value
- `context.clear()` - Clear all

## Scope

- **Step scope**: Local variables
- **Scenario scope**: Shared across steps in a scenario
- **Suite scope**: Shared across all scenarios

## Advanced Usage

```kotlin
scenario("Advanced context") {
    context.store("config", Config(timeout = 30))
    
    step("Use config") {
        val config = context.get<Config>("config")
        // Use config
    }
}
```
