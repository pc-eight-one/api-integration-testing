# Lifecycle & Hooks

Control test execution with lifecycle hooks at different levels.

## Suite Level Hooks

```kotlin
apiTestSuite("API Tests") {
    beforeSuite {
        println("Suite starting")
        // Setup database, start services, etc.
    }
    
    afterSuite {
        println("Suite completed")
        // Cleanup, generate reports, etc.
    }
    
    // Scenarios...
}
```

## Scenario Level Hooks

```kotlin
scenario("User tests") {
    beforeScenario {
        // Create test data
        context["testUser"] = createTestUser()
    }
    
    afterScenario {
        // Cleanup
        deleteTestUser(context["testUser"])
    }
    
    // Steps...
}
```

## Step Level Hooks

```kotlin
beforeEachStep {
    println("Executing step: ${it.name}")
}

afterEachStep {
    println("Step completed: ${it.name}")
}
```

## Execution Order

```
beforeSuite
  ↓
  beforeScenario
    ↓
    beforeEachStep
      ↓
      step executes
      ↓
    afterEachStep
    ↓
  afterScenario
  ↓
afterSuite
```

## Error Handling

```kotlin
afterScenario {
    if (scenarioFailed) {
        captureScreenshot()
        saveDebugInfo()
    }
}
```

## Resource Cleanup

```kotlin
afterSuite {
    // Cleanup all resources created during tests
    context.getCreatedResources().forEach { it.delete() }
}
```
