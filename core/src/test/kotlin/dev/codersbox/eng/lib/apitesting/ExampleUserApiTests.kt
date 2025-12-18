package dev.codersbox.eng.lib.api.testing

import dev.codersbox.eng.lib.api.testing.assertions.expect
import dev.codersbox.eng.lib.api.testing.auth.bearerToken
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.dsl.ApiTestSuite
import io.kotest.core.spec.style.scopes.FunSpecContainerScope
import io.kotest.matchers.shouldBe

/**
 * Example test suite demonstrating REST API testing
 */
class ExampleUserApiTests : ApiTestSuite("User Management API") {

    init {
        // Configure base settings for this test suite
        val testConfig = ApiTestConfig(
            baseUrl = "https://jsonplaceholder.typicode.com",
            logRequests = true,
            logResponses = true
        )

        // Simple test scenario - this is commented out as it requires actual API
        // Uncomment and modify for your actual API testing
        
        /*
        scenario("Get user by ID") {
            var userId: String? = null

            step("Retrieve user 1") {
                val response = get("/users/1")
                response.expect {
                    status(200)
                    jsonPath("$.id") shouldBe 1
                    jsonPath("$.name") extractTo { userId = it.asString() }
                    responseTimeUnder(2000)
                }
            }

            step("Verify user name was captured") {
                val name = context["userName"]
                println("Captured user name: $name")
            }
        }

        scenario("Create and retrieve user") {
            var userId: Int? = null

            step("Create new user") {
                val response = post("/users") {
                    body(mapOf(
                        "name" to "John Doe",
                        "email" to "john.doe@example.com",
                        "username" to "johndoe"
                    ))
                }
                response.expect {
                    status(201)
                    jsonPath("$.id") extractTo { userId = it.asInt() }
                    jsonPath("$.name") shouldBe "John Doe"
                }
            }

            step("Retrieve created user") {
                val response = get("/users/$userId")
                response.expect {
                    status(200)
                    bodyContains("John Doe")
                }
            }
        }

        scenario("Update user") {
            step("Update user 1") {
                val response = put("/users/1") {
                    body(mapOf(
                        "name" to "Updated Name",
                        "email" to "updated@example.com"
                    ))
                }
                response.expect {
                    status(200)
                    jsonPath("$.name") shouldBe "Updated Name"
                }
            }
        }

        scenario("Delete user") {
            step("Delete user 1") {
                val response = delete("/users/1")
                response.expect {
                    status(200)
                }
            }
        }

        scenario("Authentication example", setOf("auth", "smoke")) {
            step("Make authenticated request") {
                val response = get("/users/1") {
                    auth(bearerToken("fake-token-for-demo"))
                    header("X-Custom-Header", "value")
                }
                response.expect {
                    status(200)
                }
            }
        }
        */
    }
}
