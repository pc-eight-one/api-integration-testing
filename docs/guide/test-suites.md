# Test Suites & Scenarios

## Test Suite

A test suite is the top-level container for your API tests:

```kotlin
apiTestSuite("User Management API") {
    baseUrl = "https://api.example.com"
    defaultHeaders = mapOf("Accept" to "application/json")
    timeout = 30.seconds
    
    // Scenarios go here
}
```

### Configuration

- `baseUrl`: Base URL for all requests
- `defaultHeaders`: Headers applied to all requests
- `timeout`: Request timeout
- `retry`: Retry configuration

## Scenarios

Scenarios group related test steps:

```kotlin
scenario("Complete user registration flow") {
    // Steps go here
}
```

### Scenario Features

- **Isolation**: Each scenario has its own context
- **Dependencies**: Can chain scenarios
- **Data**: Can be data-driven
- **Hooks**: Before/after scenario hooks

## Steps

Individual test operations:

```kotlin
step("Create user") {
    post("/users") {
        body = userData
    }.expect {
        status(201)
    }
}
```

## Example

```kotlin
apiTestSuite("E-Commerce API") {
    baseUrl = "https://shop-api.com"
    
    beforeSuite {
        println("Starting test suite")
    }
    
    scenario("Product purchase flow") {
        var productId: String? = null
        var orderId: String? = null
        
        step("Browse products") {
            get("/products") {
                queryParam("category", "electronics")
            }.expect {
                status(200)
                jsonPath("$.items").arrayNotEmpty()
            }.extract {
                productId = jsonPath("$.items[0].id")
            }
        }
        
        step("Add to cart") {
            post("/cart/items") {
                body = mapOf("productId" to productId, "quantity" to 1)
            }.expect {
                status(201)
            }
        }
        
        step("Checkout") {
            post("/orders") {
                body = mapOf("cartId" to context["cartId"])
            }.expect {
                status(200)
                jsonPath("$.orderId").exists()
            }.extract {
                orderId = jsonPath("$.orderId")
            }
        }
    }
    
    afterSuite {
        println("Test suite completed")
    }
}
```
