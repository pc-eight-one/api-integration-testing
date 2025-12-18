# API Key Authentication

API keys can be sent in headers or query parameters.

## Header-Based API Key

```kotlin
apiTestSuite("API Key Auth") {
    authentication {
        apiKey {
            headerName = "X-API-Key"
            keyValue = "your-api-key"
        }
    }
}
```

## Query Parameter API Key

```kotlin
authentication {
    apiKey {
        paramName = "apikey"
        keyValue = "your-api-key"
    }
}
```

## Multiple API Keys

```kotlin
authentication {
    apiKey {
        headers = mapOf(
            "X-API-Key" to "key1",
            "X-Client-Id" to "client123"
        )
    }
}
```

See [Authentication Overview](index.md) for more details.
