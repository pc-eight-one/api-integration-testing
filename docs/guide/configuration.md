# Configuration

The framework provides flexible configuration options for different environments and test scenarios.

## Environment Configuration

### Configuration Files

Create environment-specific configuration files:

**config/dev.properties**
```properties
api.base.url=http://localhost:8080
api.timeout=5000
api.auth.token=dev-token-123
database.url=jdbc:postgresql://localhost:5432/testdb
```

**config/staging.properties**
```properties
api.base.url=https://staging-api.example.com
api.timeout=10000
api.auth.token=${STAGING_API_TOKEN}
database.url=jdbc:postgresql://staging-db:5432/testdb
```

**config/prod.properties**
```properties
api.base.url=https://api.example.com
api.timeout=30000
api.auth.token=${PROD_API_TOKEN}
database.url=jdbc:postgresql://prod-db:5432/testdb
```

### Loading Configuration

```kotlin
import dev.codersbox.eng.lib.testing.config.TestConfiguration

// Load environment-specific config
val config = TestConfiguration.load("dev") // or "staging", "prod"

apiTestSuite("User API") {
    baseUrl = config.getString("api.base.url")
    timeout = config.getInt("api.timeout").milliseconds
    
    defaultHeaders {
        "Authorization" to "Bearer ${config.getString("api.auth.token")}"
    }
    
    scenario("Get user") {
        step("Fetch user data") {
            get("/users/123")
        }
    }
}
```

### Environment Variables

```kotlin
// Use environment variables directly
apiTestSuite("API Tests") {
    baseUrl = System.getenv("API_BASE_URL") ?: "http://localhost:8080"
    
    defaultHeaders {
        "Authorization" to "Bearer ${System.getenv("API_TOKEN")}"
    }
}
```

### System Properties

```kotlin
// Access via system properties
apiTestSuite("API Tests") {
    baseUrl = System.getProperty("api.base.url", "http://localhost:8080")
    timeout = System.getProperty("api.timeout", "5000").toInt().milliseconds
}
```

Run with system properties:
```bash
mvn test -Dapi.base.url=https://api.example.com -Dapi.timeout=10000
```

## Suite-Level Configuration

### Global Defaults

```kotlin
apiTestSuite("E-commerce API") {
    // Base URL for all requests
    baseUrl = "https://api.example.com"
    
    // Default timeout for all requests
    timeout = 10.seconds
    
    // Default headers for all requests
    defaultHeaders {
        "Content-Type" to "application/json"
        "Accept" to "application/json"
        "X-API-Version" to "v1"
        "User-Agent" to "API-Test-Framework/1.0"
    }
    
    // Default query parameters
    defaultQueryParams {
        "format" to "json"
        "lang" to "en"
    }
    
    // Authentication
    auth {
        bearer("your-api-token")
    }
    
    scenario("Get products") {
        step("Fetch products") {
            // Inherits all defaults
            get("/products")
        }
    }
}
```

### SSL/TLS Configuration

```kotlin
apiTestSuite("Secure API") {
    // Disable SSL verification (for testing only!)
    sslConfig {
        trustAllCertificates = true
        hostnameVerification = false
    }
    
    // Or use custom truststore
    sslConfig {
        trustStore = File("path/to/truststore.jks")
        trustStorePassword = "password"
    }
}
```

### Proxy Configuration

```kotlin
apiTestSuite("Proxied API") {
    proxy {
        host = "proxy.example.com"
        port = 8080
        username = "user"
        password = "pass"
    }
}
```

## Request-Level Configuration

Override suite defaults for specific requests:

```kotlin
scenario("Custom request config") {
    step("Long-running request") {
        get("/slow-endpoint") {
            timeout = 60.seconds // Override suite timeout
            
            headers {
                "X-Custom-Header" to "value"
            }
            
            queryParams {
                "verbose" to "true"
            }
        }
    }
    
    step("Binary download") {
        get("/files/large.zip") {
            followRedirects = true
            maxRedirects = 5
        }
    }
}
```

## Retry Configuration

Configure retry behavior for flaky endpoints:

```kotlin
apiTestSuite("Flaky API") {
    // Global retry policy
    retryPolicy {
        maxAttempts = 3
        delay = 1.seconds
        backoffMultiplier = 2.0
        retryOn = setOf(500, 502, 503, 504)
    }
    
    scenario("Retry on failure") {
        step("Call flaky endpoint") {
            get("/sometimes-fails") {
                // Request-specific retry
                retry {
                    maxAttempts = 5
                    delay = 500.milliseconds
                }
            }
        }
    }
}
```

## Load Testing Configuration

```kotlin
loadTestSuite("Performance Tests") {
    baseUrl = "https://api.example.com"
    
    // Load test specific config
    loadConfig {
        virtualUsers = 100
        duration = 5.minutes
        rampUpTime = 30.seconds
        
        thinkTime {
            min = 1.seconds
            max = 5.seconds
        }
    }
    
    scenario("User registration load") {
        step("Register user") {
            post("/users") {
                body = fakeUser()
            }
        }
    }
}
```

## Logging Configuration

Configure logging levels and output:

```kotlin
apiTestSuite("API Tests") {
    logging {
        level = LogLevel.DEBUG
        logRequests = true
        logResponses = true
        logHeaders = true
        logBody = true
        
        // Mask sensitive data
        maskFields = setOf("password", "token", "apiKey")
    }
}
```

## Configuration Profiles

Create reusable configuration profiles:

```kotlin
object ConfigProfiles {
    val development = TestConfig(
        baseUrl = "http://localhost:8080",
        timeout = 5.seconds,
        retryEnabled = false
    )
    
    val staging = TestConfig(
        baseUrl = "https://staging-api.example.com",
        timeout = 10.seconds,
        retryEnabled = true,
        retryAttempts = 3
    )
    
    val production = TestConfig(
        baseUrl = "https://api.example.com",
        timeout = 30.seconds,
        retryEnabled = true,
        retryAttempts = 5
    )
}

// Usage
val profile = ConfigProfiles.staging

apiTestSuite("API Tests") {
    baseUrl = profile.baseUrl
    timeout = profile.timeout
    
    if (profile.retryEnabled) {
        retryPolicy {
            maxAttempts = profile.retryAttempts
        }
    }
}
```

## Dynamic Configuration

Load configuration dynamically at runtime:

```kotlin
class DynamicConfig {
    private val env = System.getenv("TEST_ENV") ?: "dev"
    private val props = Properties().apply {
        load(FileInputStream("config/$env.properties"))
    }
    
    fun getBaseUrl() = props.getProperty("api.base.url")
    fun getTimeout() = props.getProperty("api.timeout").toLong()
    fun getToken() = System.getenv("API_TOKEN") 
        ?: props.getProperty("api.auth.token")
}

val config = DynamicConfig()

apiTestSuite("Dynamic Config Tests") {
    baseUrl = config.getBaseUrl()
    timeout = config.getTimeout().milliseconds
    auth { bearer(config.getToken()) }
}
```

## Configuration Validation

Validate configuration before running tests:

```kotlin
fun validateConfig(config: TestConfig) {
    require(config.baseUrl.isNotEmpty()) { "Base URL must be configured" }
    require(config.timeout > 0.seconds) { "Timeout must be positive" }
    require(config.authToken.isNotEmpty()) { "Auth token is required" }
}

val config = TestConfiguration.load("staging")
validateConfig(config)
```

## Best Practices

1. **Use Environment Variables**: For sensitive data (tokens, passwords)
2. **Profile Per Environment**: Maintain separate configs for dev/staging/prod
3. **Sensible Defaults**: Provide defaults for optional configurations
4. **Validate Early**: Check configuration before running tests
5. **Document Settings**: Comment complex configuration options
6. **Version Control**: Commit config templates, not secrets

## Next Steps

- [Authentication](./authentication.md)
- [Lifecycle Hooks](./lifecycle-hooks.md)
- [Load Testing](./load-testing.md)
