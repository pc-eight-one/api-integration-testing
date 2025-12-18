# Basic Authentication

See the comprehensive Basic Authentication guide in [Authentication Overview](index.md#basic-authentication).

## Quick Example

```kotlin
apiTestSuite("Basic Auth") {
    authentication {
        basicAuth("username", "password")
    }
}
```
