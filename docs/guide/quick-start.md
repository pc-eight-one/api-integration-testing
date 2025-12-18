# Quick Start

Get up and running with the framework in 5 minutes.

## Step 1: Add Dependencies

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-core</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>api-testing-plugin-rest</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Step 2: Write Your First Test

```kotlin
@Test
fun `test user API`() {
    apiTestSuite("Quick Start") {
        baseUrl = "https://jsonplaceholder.typicode.com"
        
        scenario("Get user") {
            step("Fetch user 1") {
                get("/users/1") {}.expect {
                    status(200)
                    jsonPath("$.name").notEmpty()
                }
            }
        }
    }.execute()
}
```

## Step 3: Run

```bash
mvn test
```

That's it! 🎉

## What's Next?

- [Full Getting Started Guide](/guide/getting-started)
- [Core Concepts](/guide/architecture)
- [More Examples](/examples/rest-examples)
