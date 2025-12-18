# Authentication Overview

The framework provides flexible authentication mechanisms through pluggable authentication providers. Multiple authentication methods can be used within the same test suite.

## Available Authentication Methods

- **[Basic Authentication](basic-auth.md)** - Username/password authentication
- **[Bearer Token](bearer-token.md)** - JWT and other token-based auth
- **[OAuth 2.0](oauth2.md)** - OAuth 2.0 flows (authorization code, client credentials, etc.)
- **[API Key](api-key.md)** - API key in headers or query parameters

## Quick Start

### Suite-Level Authentication

Apply authentication to all requests in a suite:

```kotlin
apiTestSuite("Authenticated API Tests") {
    authentication {
        bearerToken("your-jwt-token-here")
    }
    
    scenario("Access protected resource") {
        step("Get user profile") {
            get("/api/profile").expect {
                status(200)
            }
        }
    }
}
```

See [Authentication Overview](index.md) for complete documentation.
