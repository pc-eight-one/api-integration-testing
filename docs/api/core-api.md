# Core API Reference

Complete reference for the core framework APIs.

## ApiTestSuite

Main entry point for creating test suites.

### Constructor

```kotlin
fun apiTestSuite(
    name: String,
    block: ApiTestSuiteContext.() -> Unit
)
```

### Properties and Methods

See [DSL Reference](dsl-reference.md) for complete syntax.

## Quick Reference

| Component | Description |
|-----------|-------------|
| `apiTestSuite` | Create test suite |
| `scenario` | Define test scenario |
| `step` | Individual test step |
| `authentication` | Configure auth |
| `loadTest` | Performance testing |
| `reusableStep` | Reusable step definition |

## Next Steps

- [Plugin API](plugin-api.md)
- [DSL Reference](dsl-reference.md)
- [Examples](../examples/rest-api-examples.md)
