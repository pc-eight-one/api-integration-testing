# REST API Examples

Comprehensive examples for REST API testing.

## Basic CRUD Operations

```kotlin
@Test
fun `complete CRUD operations`() {
    apiTestSuite("User CRUD") {
        baseUrl = "https://api.example.com"
        var userId: String? = null
        
        scenario("User lifecycle") {
            step("Create user") {
                post("/users") {
                    body = mapOf(
                        "name" to "John Doe",
                        "email" to "john@example.com",
                        "age" to 30
                    )
                }.expect {
                    status(201)
                    jsonPath("$.id").exists()
                    jsonPath("$.name").equals("John Doe")
                }.extract {
                    userId = jsonPath("$.id")
                }
            }
            
            step("Read user") {
                get("/users/$userId") {}.expect {
                    status(200)
                    jsonPath("$.name").equals("John Doe")
                }
            }
            
            step("Update user") {
                put("/users/$userId") {
                    body = mapOf("name" to "Jane Doe")
                }.expect {
                    status(200)
                    jsonPath("$.name").equals("Jane Doe")
                }
            }
            
            step("Delete user") {
                delete("/users/$userId") {}.expect {
                    status(204)
                }
            }
            
            step("Verify deletion") {
                get("/users/$userId") {}.expect {
                    status(404)
                }
            }
        }
    }.execute()
}
```

## Authentication Flow

```kotlin
@Test
fun `authentication and authorized requests`() {
    apiTestSuite("Auth Flow") {
        baseUrl = "https://api.example.com"
        
        scenario("Login and access protected resource") {
            step("Login") {
                post("/auth/login") {
                    body = mapOf(
                        "username" to "admin",
                        "password" to "secret"
                    )
                }.expect {
                    status(200)
                    jsonPath("$.token").exists()
                }.extract {
                    context["token"] = jsonPath("$.token")
                }
            }
            
            step("Access protected endpoint") {
                get("/admin/users") {
                    header("Authorization", "Bearer ${context["token"]}")
                }.expect {
                    status(200)
                    jsonPath("$.users").arrayNotEmpty()
                }
            }
        }
    }.execute()
}
```

## Pagination

```kotlin
@Test
fun `test pagination`() {
    apiTestSuite("Pagination") {
        baseUrl = "https://api.example.com"
        
        scenario("Navigate through pages") {
            step("Get first page") {
                get("/users") {
                    queryParam("page", 1)
                    queryParam("limit", 10)
                }.expect {
                    status(200)
                    jsonPath("$.page").equals(1)
                    jsonPath("$.items").arraySize(10)
                    jsonPath("$.hasNext").isTrue()
                }
            }
            
            step("Get last page") {
                get("/users") {
                    queryParam("page", 5)
                    queryParam("limit", 10)
                }.expect {
                    status(200)
                    jsonPath("$.hasNext").isFalse()
                }
            }
        }
    }.execute()
}
```

## Error Handling

```kotlin
@Test
fun `validate error responses`() {
    apiTestSuite("Error Handling") {
        baseUrl = "https://api.example.com"
        
        scenario("Test error scenarios") {
            step("Invalid request") {
                post("/users") {
                    body = mapOf("name" to "")  // Invalid
                }.expect {
                    status(400)
                    jsonPath("$.error").equals("Validation failed")
                    jsonPath("$.details.name").contains("required")
                }
            }
            
            step("Unauthorized") {
                get("/admin/users") {}.expect {
                    status(401)
                }
            }
            
            step("Not found") {
                get("/users/999999") {}.expect {
                    status(404)
                }
            }
        }
    }.execute()
}
```
