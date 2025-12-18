# Data-Driven Testing

Run the same test scenario with different data sets.

## Inline Data Tables

```kotlin
scenario("Login with multiple users").dataTable(
    "username" to listOf("admin", "user1", "guest"),
    "password" to listOf("admin123", "user123", "guest123"),
    "expectedRole" to listOf("ADMIN", "USER", "GUEST"),
    "shouldSucceed" to listOf(true, true, false)
) {
    step("Attempt login") {
        post("/auth/login") {
            body = LoginRequest(
                username = param("username"),
                password = param("password")
            )
        }.expect {
            if (param<Boolean>("shouldSucceed")) {
                status(200)
                jsonPath("$.role").equals(param<String>("expectedRole"))
            } else {
                status(401)
            }
        }
    }
}
```

## CSV Data Source

**test-data/users.csv**:
```csv
name,email,age,country
John Doe,john@example.com,30,USA
Jane Smith,jane@example.com,25,UK
Bob Johnson,bob@example.com,35,Canada
```

```kotlin
scenario("Create users from CSV").fromCsv("test-data/users.csv") {
    step("Create user") {
        post("/users") {
            body = User(
                name = param("name"),
                email = param("email"),
                age = param<Int>("age"),
                country = param("country")
            )
        }.expect {
            status(201)
            jsonPath("$.email").equals(param<String>("email"))
        }
    }
}
```

## JSON Data Source

**test-data/products.json**:
```json
{
  "products": [
    {"name": "Laptop", "price": 999.99, "category": "Electronics"},
    {"name": "Phone", "price": 699.99, "category": "Electronics"},
    {"name": "Desk", "price": 299.99, "category": "Furniture"}
  ]
}
```

```kotlin
scenario("Create products from JSON")
    .fromJson("test-data/products.json", "$.products") {
    
    step("Create product") {
        post("/products") {
            body = Product(
                name = param("name"),
                price = param<Double>("price"),
                category = param("category")
            )
        }.expect {
            status(201)
        }
    }
}
```

## YAML Data Source

**test-data/config.yaml**:
```yaml
environments:
  - name: dev
    url: https://dev-api.example.com
    timeout: 30
  - name: staging
    url: https://staging-api.example.com
    timeout: 60
```

```kotlin
scenario("Test all environments").fromYaml("test-data/config.yaml", "$.environments") {
    step("Health check") {
        baseUrl = param("url")
        timeout = param<Int>("timeout").seconds
        
        get("/health") {}.expect {
            status(200)
        }
    }
}
```

## Parameterized Assertions

```kotlin
scenario("Validate users").dataTable(
    "userId" to listOf(1, 2, 3),
    "expectedName" to listOf("John", "Jane", "Bob"),
    "expectedAge" to listOf(30, 25, 35)
) {
    step("Get and validate user") {
        get("/users/${param<Int>("userId")}") {}.expect {
            status(200)
            jsonPath("$.name").equals(param<String>("expectedName"))
            jsonPath("$.age").equals(param<Int>("expectedAge"))
        }
    }
}
```

## Dynamic Data Generation

```kotlin
scenario("Create users with generated data")
    .repeat(10) {  // Run 10 times with different data
    
    step("Create user with fake data") {
        post("/users") {
            body = User(
                name = faker.name(),
                email = faker.email(),
                phone = faker.phoneNumber()
            )
        }.expect {
            status(201)
        }
    }
}
```
