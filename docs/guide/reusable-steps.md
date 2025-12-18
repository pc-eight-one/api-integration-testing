# Reusable Steps & Step Libraries

Create reusable step definitions for common operations.

## Basic Reusable Steps

```kotlin
object UserSteps {
    fun StepContext.loginAsAdmin() {
        post("/auth/login") {
            body = LoginRequest("admin", "password")
        }.expect {
            status(200)
        }.extract {
            context["adminToken"] = jsonPath("$.token")
        }
    }
    
    fun StepContext.createUser(name: String, email: String): String {
        var userId: String? = null
        post("/users") {
            body = User(name, email)
        }.expect {
            status(201)
        }.extract {
            userId = jsonPath("$.id")
        }
        return userId!!
    }
}
```

## Using Reusable Steps

```kotlin
import UserSteps.*

scenario("User management") {
    step("Login") {
        loginAsAdmin()
    }
    
    step("Create user") {
        val userId = createUser("John", "john@example.com")
        context["userId"] = userId
    }
}
```

## Step Libraries

Organize steps into libraries:

```kotlin
class AuthenticationSteps(val context: TestContext) {
    fun loginAs(username: String, password: String) {
        // Implementation
    }
    
    fun logout() {
        // Implementation
    }
    
    fun refreshToken() {
        // Implementation
    }
}

class ProductSteps(val context: TestContext) {
    fun createProduct(product: Product): String {
        // Implementation
    }
    
    fun updateProduct(id: String, updates: Map<String, Any>) {
        // Implementation
    }
}
```

## Composite Steps

Combine multiple steps:

```kotlin
fun StepContext.completeCheckoutFlow(cartId: String): String {
    step("Apply coupon") {
        post("/cart/$cartId/coupon") {
            body = mapOf("code" to "SAVE10")
        }
    }
    
    step("Select shipping") {
        post("/cart/$cartId/shipping") {
            body = mapOf("method" to "standard")
        }
    }
    
    step("Complete payment") {
        post("/orders") {
            body = mapOf("cartId" to cartId)
        }.extract {
            return jsonPath("$.orderId")
        }
    }
}
```

## Best Practices

1. **Single Responsibility**: Each step should do one thing
2. **Descriptive Names**: Clear, action-oriented names
3. **Return Values**: Return IDs or important data
4. **Error Handling**: Handle errors gracefully
5. **Documentation**: Document parameters and return values
