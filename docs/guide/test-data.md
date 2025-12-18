# Test Data Management

Effective test data management is crucial for reliable and maintainable API tests. The framework provides comprehensive tools for generating, managing, and cleaning up test data.

## Faker Integration

Generate realistic test data automatically using the integrated Faker library.

### Basic Usage

```kotlin
import dev.codersbox.eng.lib.testing.data.TestDataGenerator

apiTestSuite("User API") {
    scenario("Create user with fake data") {
        step("Create user") {
            val userData = TestDataGenerator.fakeUser()
            
            post("/users") {
                body = userData
            }.expect {
                status(201)
                jsonPath("$.name") isEqualTo userData.name
                jsonPath("$.email") isEqualTo userData.email
            }
        }
    }
}
```

### Available Fake Data

```kotlin
// User data
val user = TestDataGenerator.fakeUser()
val email = TestDataGenerator.fakeEmail()
val name = TestDataGenerator.fakeName()
val phone = TestDataGenerator.fakePhone()

// Address data
val address = TestDataGenerator.fakeAddress()
val city = TestDataGenerator.fakeCity()
val country = TestDataGenerator.fakeCountry()

// Business data
val company = TestDataGenerator.fakeCompany()
val jobTitle = TestDataGenerator.fakeJobTitle()

// Internet data
val url = TestDataGenerator.fakeUrl()
val username = TestDataGenerator.fakeUsername()
val ipAddress = TestDataGenerator.fakeIpAddress()

// Financial data
val creditCard = TestDataGenerator.fakeCreditCard()
val iban = TestDataGenerator.fakeIban()

// Misc
val uuid = TestDataGenerator.fakeUuid()
val lorem = TestDataGenerator.fakeLorem(words = 10)
val date = TestDataGenerator.fakePastDate()
```

## Test Data Builders

Create complex test data with builder pattern.

### Basic Builder

```kotlin
data class User(
    val name: String,
    val email: String,
    val age: Int,
    val roles: List<String>
)

fun userBuilder() = User(
    name = TestDataGenerator.fakeName(),
    email = TestDataGenerator.fakeEmail(),
    age = (18..65).random(),
    roles = listOf("user")
)

// Usage
scenario("Create admin user") {
    step("Create user with admin role") {
        val user = userBuilder().copy(roles = listOf("admin"))
        
        post("/users") {
            body = user
        }
    }
}
```

### Advanced Builder Pattern

```kotlin
class UserBuilder {
    private var name = TestDataGenerator.fakeName()
    private var email = TestDataGenerator.fakeEmail()
    private var age = 25
    private var roles = mutableListOf("user")
    
    fun withName(name: String) = apply { this.name = name }
    fun withEmail(email: String) = apply { this.email = email }
    fun withAge(age: Int) = apply { this.age = age }
    fun withRole(role: String) = apply { roles.add(role) }
    fun asAdmin() = apply { roles.add("admin") }
    
    fun build() = User(name, email, age, roles)
}

// Usage
val adminUser = UserBuilder()
    .withName("Admin User")
    .asAdmin()
    .build()

val youngUser = UserBuilder()
    .withAge(18)
    .build()
```

## Auto-Cleanup & Resource Tracking

Automatically clean up test data after scenarios.

### Track Resources for Cleanup

```kotlin
apiTestSuite("Order API") {
    afterScenario {
        cleanupTrackedResources()
    }
    
    scenario("Create and cleanup order") {
        step("Create order") {
            post("/orders") {
                body = fakeOrder()
            }.expect {
                status(201)
            }.extractTo { orderId = it.jsonPath("$.id") }
             .trackForCleanup("orders", orderId)
        }
        
        step("Add items") {
            post("/orders/$orderId/items") {
                body = fakeOrderItem()
            }.trackForCleanup("order-items")
        }
        
        // After scenario, cleanup will automatically:
        // 1. DELETE /orders/{orderId}/items/{itemId}
        // 2. DELETE /orders/{orderId}
    }
}
```

### Custom Cleanup Logic

```kotlin
apiTestSuite("User API") {
    val createdUserIds = mutableListOf<String>()
    
    afterScenario {
        createdUserIds.forEach { userId ->
            delete("/users/$userId").execute()
        }
        createdUserIds.clear()
    }
    
    scenario("Bulk user creation") {
        repeat(5) { i ->
            step("Create user $i") {
                val response = post("/users") {
                    body = fakeUser()
                }.execute()
                
                val userId = response.jsonPath<String>("$.id")
                createdUserIds.add(userId)
            }
        }
    }
}
```

## Data-Driven Test Data

Load test data from external sources.

### From CSV

```kotlin
scenario("Login with multiple users").fromCsv("test-data/users.csv") {
    step("Login") {
        post("/login") {
            body = LoginRequest(
                username = param("username"),
                password = param("password")
            )
        }.expect {
            status(param<Int>("expectedStatus"))
        }
    }
}
```

CSV file format:
```csv
username,password,expectedStatus
admin,secret123,200
user1,pass456,200
invalid,wrong,401
```

### From JSON

```kotlin
scenario("Create products").fromJson("test-data/products.json") {
    step("Create product") {
        post("/products") {
            body = param<Map<String, Any>>("product")
        }.expect {
            status(201)
        }
    }
}
```

JSON file format:
```json
[
  {"product": {"name": "Laptop", "price": 999.99}},
  {"product": {"name": "Mouse", "price": 29.99}},
  {"product": {"name": "Keyboard", "price": 79.99}}
]
```

### From YAML

```kotlin
scenario("Configure services").fromYaml("test-data/configs.yaml") {
    step("Update config") {
        put("/services/${param<String>("service")}/config") {
            body = param<Map<String, Any>>("config")
        }
    }
}
```

## Test Data Templates

Define reusable data templates.

```kotlin
object TestDataTemplates {
    val validUser = mapOf(
        "name" to "John Doe",
        "email" to "john@example.com",
        "age" to 30
    )
    
    val invalidUser = mapOf(
        "name" to "",
        "email" to "invalid-email",
        "age" to -5
    )
    
    fun userWithRole(role: String) = validUser + ("role" to role)
}

// Usage
scenario("Create valid user") {
    step("Create user") {
        post("/users") {
            body = TestDataTemplates.validUser
        }.expect {
            status(201)
        }
    }
}

scenario("Reject invalid user") {
    step("Try to create invalid user") {
        post("/users") {
            body = TestDataTemplates.invalidUser
        }.expect {
            status(400)
        }
    }
}
```

## Database Seeding

Seed database with test data before tests.

```kotlin
apiTestSuite("E-commerce API") {
    beforeSuite {
        seedDatabase()
    }
    
    afterSuite {
        cleanDatabase()
    }
    
    scenario("Query products") {
        step("Get products") {
            get("/products").expect {
                jsonPath("$.items") hasMinSize 10
            }
        }
    }
}

fun seedDatabase() {
    // Use database client to insert test data
    repeat(10) {
        insertProduct(fakeProduct())
    }
}
```

## Best Practices

1. **Use Faker for Realistic Data**: Avoid hardcoded test data
2. **Implement Cleanup**: Always clean up created resources
3. **Isolate Tests**: Each test should create its own data
4. **Use Builders**: For complex objects, use builder pattern
5. **External Data for Large Sets**: Store large datasets in CSV/JSON
6. **Template Common Patterns**: Reuse data templates for consistency

## Next Steps

- [Data-Driven Testing](./data-driven-testing.md)
- [Lifecycle Hooks](./lifecycle-hooks.md)
- [Shared Context](./shared-context.md)
