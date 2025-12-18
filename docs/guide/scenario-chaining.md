# Scenario Chaining

Chain scenarios to create complex test flows where later scenarios depend on earlier ones.

## Basic Chaining

```kotlin
apiTestSuite("E-Commerce Flow") {
    scenario("Setup: Create test user") {
        step("Register user") {
            post("/users") {
                body = User(name = "Test User", email = "test@example.com")
            }.extract {
                context["userId"] = jsonPath("$.id")
            }
        }
    }
    
    scenario("Main: User shopping flow") {
        dependsOn("Setup: Create test user")
        
        step("Browse products") {
            get("/products") {}.expect { status(200) }
        }
        
        step("Add to cart") {
            post("/cart") {
                body = mapOf("userId" to context["userId"], "productId" to 123)
            }.expect { status(201) }
        }
    }
    
    scenario("Cleanup: Remove test user") {
        dependsOn("Main: User shopping flow")
        runsAfter = true
        
        step("Delete user") {
            delete("/users/${context["userId"]}") {}.expect { status(204) }
        }
    }
}
```

## Dependency Types

### Sequential Dependencies
```kotlin
scenario("Step 2") {
    dependsOn("Step 1")  // Waits for Step 1 to complete
}
```

### Multiple Dependencies
```kotlin
scenario("Final") {
    dependsOn("Setup 1", "Setup 2", "Setup 3")
}
```

### Conditional Execution
```kotlin
scenario("Cleanup") {
    runsIf { context.has("resourceCreated") }
}
```

## Shared Setup Scenarios

```kotlin
apiTestSuite("API Tests") {
    scenario("Setup") {
        isSetup = true  // Runs first
        step("Initialize") {
            // Setup code
        }
    }
    
    scenario("Test 1") { /* uses setup */ }
    scenario("Test 2") { /* uses setup */ }
    
    scenario("Teardown") {
        isTeardown = true  // Runs last
        step("Cleanup") {
            // Cleanup code
        }
    }
}
```
