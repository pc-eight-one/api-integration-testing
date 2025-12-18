# GraphQL Plugin

The GraphQL plugin provides comprehensive support for testing GraphQL APIs with a Kotlin DSL.

## Installation

Add the GraphQL plugin dependency:

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>plugin-graphql</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Basic Usage

### Simple Query

```kotlin
import dev.codersbox.eng.lib.testing.plugins.graphql.*

apiTestSuite("GraphQL API") {
    baseUrl = "https://api.example.com/graphql"
    
    scenario("Get user by ID") {
        step("Query user") {
            graphql {
                query = """
                    query GetUser(${'$'}id: ID!) {
                        user(id: ${'$'}id) {
                            id
                            name
                            email
                        }
                    }
                """
                variables {
                    "id" to "123"
                }
            }.expect {
                status(200)
                jsonPath("$.data.user.name") isEqualTo "John Doe"
                jsonPath("$.data.user.email") isEqualTo "john@example.com"
            }
        }
    }
}
```

### Mutation

```kotlin
scenario("Create user") {
    step("Execute mutation") {
        graphql {
            query = """
                mutation CreateUser(${'$'}input: CreateUserInput!) {
                    createUser(input: ${'$'}input) {
                        id
                        name
                        email
                        createdAt
                    }
                }
            """
            variables {
                "input" to mapOf(
                    "name" to "Jane Doe",
                    "email" to "jane@example.com",
                    "age" to 28
                )
            }
        }.expect {
            status(200)
            jsonPath("$.data.createUser.id") isNotNull()
            jsonPath("$.data.createUser.name") isEqualTo "Jane Doe"
        }.extractTo { userId = it.jsonPath("$.data.createUser.id") }
    }
}
```

## Advanced Features

### Fragments

```kotlin
scenario("Query with fragments") {
    step("Use fragments") {
        graphql {
            query = """
                fragment UserFields on User {
                    id
                    name
                    email
                }
                
                query GetUsers {
                    users {
                        ...UserFields
                        posts {
                            id
                            title
                        }
                    }
                }
            """
        }.expect {
            jsonPath("$.data.users[0].id") isNotNull()
            jsonPath("$.data.users[0].posts") isNotEmpty()
        }
    }
}
```

### Inline Fragments

```kotlin
scenario("Query polymorphic types") {
    step("Query with inline fragments") {
        graphql {
            query = """
                query GetContent {
                    content {
                        ... on Article {
                            title
                            body
                        }
                        ... on Video {
                            title
                            duration
                        }
                    }
                }
            """
        }
    }
}
```

### Directives

```kotlin
scenario("Conditional queries") {
    step("Use @include directive") {
        graphql {
            query = """
                query GetUser(${'$'}id: ID!, ${'$'}withPosts: Boolean!) {
                    user(id: ${'$'}id) {
                        id
                        name
                        posts @include(if: ${'$'}withPosts) {
                            id
                            title
                        }
                    }
                }
            """
            variables {
                "id" to "123"
                "withPosts" to true
            }
        }
    }
}
```

## Error Handling

### GraphQL Errors

```kotlin
scenario("Handle GraphQL errors") {
    step("Query non-existent user") {
        graphql {
            query = """
                query GetUser(${'$'}id: ID!) {
                    user(id: ${'$'}id) {
                        id
                        name
                    }
                }
            """
            variables {
                "id" to "non-existent"
            }
        }.expect {
            status(200) // GraphQL returns 200 even for errors
            jsonPath("$.errors[0].message") contains "User not found"
            jsonPath("$.errors[0].path[0]") isEqualTo "user"
        }
    }
}
```

### Validation Errors

```kotlin
scenario("Handle validation errors") {
    step("Send invalid input") {
        graphql {
            query = """
                mutation CreateUser(${'$'}input: CreateUserInput!) {
                    createUser(input: ${'$'}input) {
                        id
                    }
                }
            """
            variables {
                "input" to mapOf(
                    "name" to "", // Invalid: empty name
                    "email" to "invalid-email" // Invalid format
                )
            }
        }.expect {
            jsonPath("$.errors") isNotEmpty()
            jsonPath("$.errors[*].extensions.code") contains "VALIDATION_ERROR"
        }
    }
}
```

## Subscriptions

### WebSocket Subscriptions

```kotlin
scenario("Subscribe to updates") {
    step("Create subscription") {
        graphqlSubscription {
            query = """
                subscription OnUserCreated {
                    userCreated {
                        id
                        name
                        email
                    }
                }
            """
            
            onMessage { message ->
                println("Received: ${message.jsonPath("$.data.userCreated.name")}")
            }
            
            // Keep subscription alive for 30 seconds
            duration = 30.seconds
        }
    }
}
```

## Schema Validation

### Validate Against Schema

```kotlin
scenario("Validate response schema") {
    step("Query with schema validation") {
        graphql {
            query = """
                query GetUser(${'$'}id: ID!) {
                    user(id: ${'$'}id) {
                        id
                        name
                        email
                    }
                }
            """
            variables {
                "id" to "123"
            }
        }.expect {
            matchesGraphQLSchema(File("schema.graphql"))
        }
    }
}
```

## Batching

### Batch Queries

```kotlin
scenario("Batch multiple queries") {
    step("Execute batch") {
        graphqlBatch {
            query {
                query = "query { users { id name } }"
                operationName = "GetUsers"
            }
            query {
                query = "query { posts { id title } }"
                operationName = "GetPosts"
            }
        }.expect {
            status(200)
            jsonPath("$[0].data.users") isNotEmpty()
            jsonPath("$[1].data.posts") isNotEmpty()
        }
    }
}
```

## Authentication

### Bearer Token

```kotlin
apiTestSuite("Authenticated GraphQL") {
    auth {
        bearer("your-api-token")
    }
    
    scenario("Query authenticated data") {
        step("Get current user") {
            graphql {
                query = "query { me { id name email } }"
            }
        }
    }
}
```

### Custom Headers

```kotlin
scenario("Custom auth headers") {
    step("Query with API key") {
        graphql {
            query = "query { protectedData { id value } }"
            headers {
                "X-API-Key" to "your-api-key"
            }
        }
    }
}
```

## Performance Testing

### Load Testing GraphQL

```kotlin
loadTestSuite("GraphQL Performance") {
    baseUrl = "https://api.example.com/graphql"
    
    loadConfig {
        virtualUsers = 50
        duration = 2.minutes
    }
    
    scenario("Query load test") {
        step("Concurrent queries") {
            graphql {
                query = """
                    query GetUsers(${'$'}limit: Int!) {
                        users(limit: ${'$'}limit) {
                            id
                            name
                        }
                    }
                """
                variables {
                    "limit" to 10
                }
            }.expect {
                responseTime lessThan 500.milliseconds
            }
        }
    }
}
```

## Best Practices

1. **Use Variables**: Always use variables instead of string interpolation
2. **Request Minimal Data**: Only query fields you need
3. **Handle Errors**: Check both HTTP status and GraphQL errors
4. **Use Fragments**: Reduce duplication with fragments
5. **Validate Schema**: Use schema validation in CI/CD
6. **Monitor Performance**: Track query complexity and response times

## Next Steps

- [REST Plugin](./rest.md)
- [WebSocket Plugin](./websocket.md)
- [Authentication](../guide/authentication.md)
