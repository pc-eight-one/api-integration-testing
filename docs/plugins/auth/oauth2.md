# OAuth 2.0 Authentication

OAuth 2.0 support for various grant types.

## Client Credentials Flow

```kotlin
apiTestSuite("OAuth 2.0") {
    authentication {
        oauth2 {
            tokenUrl = "https://auth.example.com/oauth/token"
            clientId = "your-client-id"
            clientSecret = "your-client-secret"
            grantType = "client_credentials"
        }
    }
}
```

## Authorization Code Flow

```kotlin
authentication {
    oauth2 {
        authorizationUrl = "https://auth.example.com/oauth/authorize"
        tokenUrl = "https://auth.example.com/oauth/token"
        clientId = "client-id"
        clientSecret = "client-secret"
        redirectUri = "http://localhost:8080/callback"
        scope = "read write"
    }
}
```

See [Authentication Overview](index.md) for more OAuth 2.0 examples.
