# Load Testing

Built-in performance and load testing capabilities.

## Basic Load Test

```kotlin
loadTest {
    scenario("User registration")
    
    // Load configuration
    concurrentUsers = 50
    duration = 2.minutes
    rampUpTime = 30.seconds
    
    // Test logic
    step("Register user") {
        post("/users") {
            body = User(
                name = faker.name(),
                email = faker.email()
            )
        }.expect {
            status(201)
        }
    }
    
    // Assertions
    assertions {
        responseTime.average lessThan 200.milliseconds
        responseTime.p95 lessThan 500.milliseconds
        responseTime.p99 lessThan 1.seconds
        errorRate lessThan 1.percent
        throughput greaterThan 100.requestsPerSecond
    }
}
```

## Load Patterns

### Constant Load
```kotlin
loadTest {
    concurrentUsers = 100
    duration = 5.minutes
    // Maintains 100 concurrent users for 5 minutes
}
```

### Ramp Up
```kotlin
loadTest {
    startUsers = 10
    endUsers = 100
    rampUpTime = 2.minutes
    duration = 5.minutes
    // Gradually increases from 10 to 100 users over 2 minutes
}
```

### Spike Test
```kotlin
loadTest {
    concurrentUsers = 200
    duration = 30.seconds
    // Sudden spike of 200 users
}
```

### Step Load
```kotlin
loadTest {
    steps = listOf(
        LoadStep(users = 25, duration = 1.minute),
        LoadStep(users = 50, duration = 1.minute),
        LoadStep(users = 100, duration = 1.minute)
    )
}
```

## Metrics

```kotlin
loadTest {
    // ... test logic
    
    onComplete { results ->
        println("Total Requests: ${results.totalRequests}")
        println("Successful: ${results.successfulRequests}")
        println("Failed: ${results.failedRequests}")
        println("Avg Response Time: ${results.averageResponseTime}ms")
        println("Throughput: ${results.requestsPerSecond} req/s")
        
        // Export metrics
        results.exportToCsv("load-test-results.csv")
        results.exportToJson("load-test-results.json")
    }
}
```

## Distributed Load Testing

```kotlin
distributedLoadTest {
    nodes = listOf(
        LoadNode("http://node1:8080"),
        LoadNode("http://node2:8080"),
        LoadNode("http://node3:8080")
    )
    
    totalUsers = 1000  // Distributed across nodes
    duration = 10.minutes
    
    scenario("Distributed test") {
        // Test logic
    }
}
```

## Advanced Configuration

```kotlin
loadTest {
    config {
        // Think time between requests
        thinkTime = 1.second
        
        // Timeout
        requestTimeout = 30.seconds
        
        // Connection pooling
        maxConnections = 200
        
        // Keep-alive
        keepAlive = true
        
        // Follow redirects
        followRedirects = true
    }
}
```
