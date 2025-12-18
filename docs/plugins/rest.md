# REST API Plugin

Comprehensive HTTP/HTTPS REST API testing support.

## Installation

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-rest</artifactId>
    <version>1.0.0</version>
</dependency>
```

## HTTP Methods

```kotlin
// GET
get("/users") {
    queryParam("page", 1)
    queryParam("limit", 10)
}.expect {
    status(200)
}

// POST
post("/users") {
    body = User(name = "John", email = "john@example.com")
    header("Content-Type", "application/json")
}.expect {
    status(201)
}

// PUT
put("/users/123") {
    body = userUpdate
}.expect {
    status(200)
}

// PATCH
patch("/users/123") {
    body = mapOf("name" to "Jane")
}.expect {
    status(200)
}

// DELETE
delete("/users/123") {}.expect {
    status(204)
}

// HEAD, OPTIONS
head("/users") {}.expect { status(200) }
options("/users") {}.expect { status(200) }
```

## Request Configuration

```kotlin
post("/users") {
    // Headers
    header("Authorization", "Bearer $token")
    headers(mapOf(
        "Accept" to "application/json",
        "X-Custom" to "value"
    ))
    
    // Query Parameters
    queryParam("verbose", true)
    queryParams(mapOf("sort" to "name", "order" to "asc"))
    
    // Path Parameters
    pathParam("id", userId)
    
    // Body
    body = userData
    jsonBody("""{"name": "John"}""")
    
    // Form Data
    formParam("username", "john")
    multipartFile("avatar", File("avatar.png"))
    
    // Timeout
    timeout(30.seconds)
    
    // Retry
    retry(maxAttempts = 3, delay = 1.second)
}
```

## Assertions

```kotlin
.expect {
    // Status
    status(200)
    statusIn(200..299)
    success()  // 2xx
    
    // Headers
    header("Content-Type").contains("application/json")
    header("X-Rate-Limit").exists()
    
    // Body - JSON
    jsonPath("$.id").exists()
    jsonPath("$.name").equals("John")
    jsonPath("$.age").greaterThan(18)
    jsonPath("$.email").matches(emailRegex)
    jsonPath("$.items").arraySize(5)
    jsonPath("$.active").isTrue()
    
    // Body - Text
    bodyContains("success")
    bodyMatches(regex)
    
    // Response Time
    responseTime.lessThan(500.milliseconds)
    
    // Schema Validation
    matchesJsonSchema(schema)
}
```

## Data Extraction

```kotlin
.extract {
    // Extract values
    val userId = jsonPath<String>("$.id")
    val userName = jsonPath<String>("$.name")
    
    // Store in context
    context["userId"] = jsonPath("$.id")
    context["token"] = header("Authorization")
    
    // Extract multiple
    context.putAll(mapOf(
        "id" to jsonPath("$.id"),
        "email" to jsonPath("$.email")
    ))
}
```
