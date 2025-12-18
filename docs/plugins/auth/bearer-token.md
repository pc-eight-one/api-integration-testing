# Bearer Token Authentication

Bearer tokens (JWT, OAuth access tokens) are commonly used for API authentication.

## Basic Usage

```kotlin
apiTestSuite("Bearer Token Auth") {
    authentication {
        bearerToken("your-jwt-token")
    }
}
```

## From Login

```kotlin
scenario("Login and get token") {
    var token = ""
    
    step("Login") {
        post("/api/login") {
            body = LoginRequest("user", "pass")
        }.extract {
            token = it.jsonPath("$.token")
        }
    }
    
    step("Use token") {
        get("/api/profile") {
            auth { bearerToken(token) }
        }.expect {
            status(200)
        }
    }
}
```

See [Authentication Overview](index.md) for more details.
