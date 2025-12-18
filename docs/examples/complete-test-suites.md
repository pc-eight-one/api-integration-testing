# Complete Test Suites

Real-world examples of complete test suites combining multiple features.

## E-Commerce API Test Suite

```kotlin
import dev.codersbox.eng.lib.testing.dsl.*
import io.kotest.core.spec.style.FreeSpec

class ECommerceTestSuite : FreeSpec({
    
    lateinit var authToken: String
    lateinit var userId: String
    lateinit var orderId: String
    
    "E-Commerce API Complete Test Suite" - {
        
        apiTestSuite("E-Commerce Platform") {
            baseUrl = System.getenv("API_BASE_URL") ?: "https://api.ecommerce.example.com"
            
            defaultHeaders {
                "Content-Type" to "application/json"
                "X-Client-Version" to "1.0.0"
            }
            
            // Lifecycle hooks
            beforeSuite {
                println("Starting E-Commerce Test Suite")
                clearTestData()
            }
            
            afterSuite {
                println("Cleaning up test data")
                cleanupTestUsers()
            }
            
            beforeScenario {
                println("Starting scenario: ${it.name}")
            }
            
            afterScenario {
                println("Completed scenario: ${it.name}")
            }
            
            // 1. Authentication Flow
            scenario("User Registration and Login") {
                val testEmail = "test-${System.currentTimeMillis()}@example.com"
                val testPassword = "SecurePass123!"
                
                step("Register new user") {
                    post("/api/auth/register") {
                        json {
                            "email" to testEmail
                            "password" to testPassword
                            "firstName" to faker.name().firstName()
                            "lastName" to faker.name().lastName()
                        }
                    }.expect {
                        status(201)
                        jsonPath("$.id").isNotEmpty()
                        jsonPath("$.email").equals(testEmail)
                        header("Location").matches("/api/users/.*")
                    }.extract {
                        userId = jsonPath("$.id")
                    }
                }
                
                step("Login with credentials") {
                    post("/api/auth/login") {
                        json {
                            "email" to testEmail
                            "password" to testPassword
                        }
                    }.expect {
                        status(200)
                        jsonPath("$.token").isNotEmpty()
                        jsonPath("$.expiresIn").isNumber()
                    }.extract {
                        authToken = jsonPath("$.token")
                    }
                }
                
                step("Verify token works") {
                    get("/api/auth/me") {
                        header("Authorization", "Bearer $authToken")
                    }.expect {
                        status(200)
                        jsonPath("$.id").equals(userId)
                        jsonPath("$.email").equals(testEmail)
                    }
                }
            }
            
            // 2. Product Catalog
            scenario("Browse and Search Products") {
                var productId = ""
                
                step("Get all products") {
                    get("/api/products") {
                        queryParam("page", "1")
                        queryParam("limit", "20")
                    }.expect {
                        status(200)
                        jsonPath("$.items").isArray()
                        jsonPath("$.items").arraySize(greaterThan = 0)
                        jsonPath("$.pagination.total").isNumber()
                    }.extract {
                        productId = jsonPath("$.items[0].id")
                    }
                }
                
                step("Get product details") {
                    get("/api/products/$productId")
                }.expect {
                    status(200)
                    jsonPath("$.id").equals(productId)
                    jsonPath("$.name").isNotEmpty()
                    jsonPath("$.price").isNumber()
                    jsonPath("$.stock").isNumber()
                }
                
                step("Search products") {
                    get("/api/products/search") {
                        queryParam("q", "laptop")
                        queryParam("category", "electronics")
                    }.expect {
                        status(200)
                        jsonPath("$.results").isArray()
                        jsonPath("$.results[*].name").allMatch { it.contains("laptop", ignoreCase = true) }
                    }
                }
            }
            
            // 3. Shopping Cart Flow
            scenario("Shopping Cart Operations") {
                authentication {
                    bearerToken(authToken)
                }
                
                var cartId = ""
                var productId = "prod-123"
                
                step("Create cart") {
                    post("/api/cart")
                }.expect {
                    status(201)
                    jsonPath("$.id").isNotEmpty()
                    jsonPath("$.items").isArray()
                    jsonPath("$.items").arraySize(0)
                }.extract {
                    cartId = jsonPath("$.id")
                }
                
                step("Add item to cart") {
                    post("/api/cart/$cartId/items") {
                        json {
                            "productId" to productId
                            "quantity" to 2
                        }
                    }.expect {
                        status(201)
                        jsonPath("$.items").arraySize(1)
                        jsonPath("$.items[0].productId").equals(productId)
                        jsonPath("$.items[0].quantity").equals(2)
                        jsonPath("$.total").isNumber()
                    }
                }
                
                step("Update cart item quantity") {
                    put("/api/cart/$cartId/items/$productId") {
                        json {
                            "quantity" to 5
                        }
                    }.expect {
                        status(200)
                        jsonPath("$.items[0].quantity").equals(5)
                    }
                }
                
                step("Remove item from cart") {
                    delete("/api/cart/$cartId/items/$productId")
                }.expect {
                    status(204)
                }
                
                step("Verify cart is empty") {
                    get("/api/cart/$cartId")
                }.expect {
                    status(200)
                    jsonPath("$.items").arraySize(0)
                }
            }
            
            // 4. Order Processing
            scenario("Complete Order Flow") {
                authentication {
                    bearerToken(authToken)
                }
                
                step("Create order") {
                    post("/api/orders") {
                        json {
                            "items" to listOf(
                                mapOf(
                                    "productId" to "prod-123",
                                    "quantity" to 1
                                )
                            )
                            "shippingAddress" to mapOf(
                                "street" to faker.address().streetAddress(),
                                "city" to faker.address().city(),
                                "zipCode" to faker.address().zipCode(),
                                "country" to "USA"
                            )
                            "paymentMethod" to "credit_card"
                        }
                    }.expect {
                        status(201)
                        jsonPath("$.id").isNotEmpty()
                        jsonPath("$.status").equals("pending")
                        jsonPath("$.total").isNumber()
                    }.extract {
                        orderId = jsonPath("$.id")
                    }
                }
                
                step("Get order details") {
                    get("/api/orders/$orderId")
                }.expect {
                    status(200)
                    jsonPath("$.id").equals(orderId)
                    jsonPath("$.items").isNotEmpty()
                }
                
                step("Get user orders") {
                    get("/api/users/$userId/orders")
                }.expect {
                    status(200)
                    jsonPath("$[*].id").contains(orderId)
                }
            }
            
            // 5. Data-Driven Test
            scenario("Product Price Validation").dataTable(
                "productId" to listOf("prod-1", "prod-2", "prod-3"),
                "expectedMinPrice" to listOf(10.0, 20.0, 30.0)
            ) {
                step("Validate product price") {
                    get("/api/products/${param<String>("productId")}")
                }.expect {
                    status(200)
                    jsonPath("$.price").isNumber()
                    jsonPath("$.price").isGreaterThan(param<Double>("expectedMinPrice"))
                }
            }
            
            // 6. Reusable Steps
            val loginStep = reusableStep("Login as admin") {
                post("/api/auth/login") {
                    json {
                        "email" to "admin@example.com"
                        "password" to System.getenv("ADMIN_PASSWORD")
                    }
                }.expect {
                    status(200)
                }.extract {
                    authToken = jsonPath("$.token")
                }
            }
            
            scenario("Admin Operations") {
                execute(loginStep)
                
                authentication {
                    bearerToken(authToken)
                }
                
                step("Create new product") {
                    post("/api/admin/products") {
                        json {
                            "name" to faker.commerce().productName()
                            "price" to faker.number().randomDouble(2, 10, 1000)
                            "stock" to faker.number().numberBetween(0, 100)
                            "category" to "electronics"
                        }
                    }.expect {
                        status(201)
                        jsonPath("$.id").isNotEmpty()
                    }
                }
            }
            
            // 7. Error Handling
            scenario("Handle Invalid Requests") {
                authentication {
                    bearerToken(authToken)
                }
                
                step("Invalid product ID") {
                    get("/api/products/invalid-id")
                }.expect {
                    status(404)
                    jsonPath("$.error").equals("Product not found")
                }
                
                step("Invalid request body") {
                    post("/api/products") {
                        json {
                            "name" to ""  // Invalid: empty name
                        }
                    }.expect {
                        status(400)
                        jsonPath("$.errors").isNotEmpty()
                    }
                }
                
                step("Unauthorized access") {
                    get("/api/admin/users") {
                        // No auth header
                    }.expect {
                        status(401)
                    }
                }
            }
            
            // 8. Performance Test
            scenario("API Performance Under Load") {
                loadTest {
                    duration = 60.seconds
                    virtualUsers = 50
                    rampUp = 10.seconds
                    
                    step("Load test products endpoint") {
                        get("/api/products")
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

## Microservices Integration Test Suite

```kotlin
class MicroservicesTestSuite : FreeSpec({
    
    "Microservices Integration Tests" - {
        
        val userServiceUrl = System.getenv("USER_SERVICE_URL")
        val orderServiceUrl = System.getenv("ORDER_SERVICE_URL")
        val inventoryServiceUrl = System.getenv("INVENTORY_SERVICE_URL")
        val paymentServiceUrl = System.getenv("PAYMENT_SERVICE_URL")
        
        var userId = ""
        var orderId = ""
        var paymentId = ""
        
        scenario("Cross-service user journey") {
            
            // Step 1: Create user in User Service
            step("Create user") {
                post("$userServiceUrl/api/users") {
                    json {
                        "email" to faker.internet().emailAddress()
                        "name" to faker.name().fullName()
                    }
                }.expect {
                    status(201)
                }.extract {
                    userId = jsonPath("$.id")
                }
            }
            
            // Step 2: Check inventory in Inventory Service
            var productId = ""
            step("Check product availability") {
                get("$inventoryServiceUrl/api/inventory/product-123")
            }.expect {
                status(200)
                jsonPath("$.available").isTrue()
                jsonPath("$.quantity").isGreaterThan(0)
            }.extract {
                productId = jsonPath("$.productId")
            }
            
            // Step 3: Create order in Order Service
            step("Create order") {
                post("$orderServiceUrl/api/orders") {
                    json {
                        "userId" to userId
                        "items" to listOf(
                            mapOf("productId" to productId, "quantity" to 1)
                        )
                    }
                }.expect {
                    status(201)
                }.extract {
                    orderId = jsonPath("$.id")
                }
            }
            
            // Step 4: Process payment in Payment Service
            step("Process payment") {
                post("$paymentServiceUrl/api/payments") {
                    json {
                        "orderId" to orderId
                        "amount" to 99.99
                        "method" to "credit_card"
                    }
                }.expect {
                    status(200)
                    jsonPath("$.status").equals("success")
                }.extract {
                    paymentId = jsonPath("$.id")
                }
            }
            
            // Step 5: Verify order status updated
            step("Verify order completed") {
                get("$orderServiceUrl/api/orders/$orderId")
            }.expect {
                status(200)
                jsonPath("$.status").equals("completed")
                jsonPath("$.paymentId").equals(paymentId)
            }
            
            // Step 6: Verify inventory updated
            step("Verify inventory decreased") {
                get("$inventoryServiceUrl/api/inventory/$productId")
            }.expect {
                status(200)
                jsonPath("$.quantity").isLessThan(original_quantity)
            }
        }
    }
})
```

## Next Steps

- [REST API Examples](rest-api-examples.md)
- [GraphQL Examples](graphql-examples.md)
- [Load Testing Examples](load-testing-examples.md)
- [Best Practices](../guides/best-practices.md)
