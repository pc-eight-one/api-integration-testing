# Load Testing Examples

Comprehensive examples for performance and load testing APIs.

## Basic Load Test

```kotlin
import dev.codersbox.eng.lib.testing.dsl.*
import kotlin.time.Duration.Companion.seconds

class BasicLoadTest : FreeSpec({
    "Basic load test - Homepage" {
        apiTestSuite("Homepage Load Test") {
            baseUrl = "https://api.example.com"
            
            scenario("Test homepage under load") {
                loadTest {
                    duration = 60.seconds
                    virtualUsers = 50
                    rampUp = 10.seconds
                    
                    step("GET homepage") {
                        get("/")
                    }.expect {
                        status(200)
                        responseTime(lessThan = 1.seconds)
                    }
                }
            }
        }
    }
})
```

## Ramp-Up Strategy

```kotlin
"Load test with gradual ramp-up" {
    apiTestSuite("Gradual Load Test") {
        baseUrl = "https://api.example.com"
        
        scenario("Progressive user load") {
            loadTest {
                stages {
                    stage(duration = 30.seconds, targetUsers = 10)
                    stage(duration = 30.seconds, targetUsers = 25)
                    stage(duration = 30.seconds, targetUsers = 50)
                    stage(duration = 30.seconds, targetUsers = 75)
                    stage(duration = 60.seconds, targetUsers = 100)
                    stage(duration = 30.seconds, targetUsers = 50)
                    stage(duration = 30.seconds, targetUsers = 10)
                }
                
                step("API calls") {
                    get("/api/products")
                }.expect {
                    status(200)
                    responseTime(lessThan = 2.seconds)
                }
            }
        }
    }
}
```

See [Load Testing Guide](../guides/load-testing.md) for more details.
