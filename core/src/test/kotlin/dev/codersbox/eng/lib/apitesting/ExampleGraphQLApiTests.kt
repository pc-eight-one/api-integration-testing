package dev.codersbox.eng.lib.api.testing

import dev.codersbox.eng.lib.api.testing.assertions.expect
import dev.codersbox.eng.lib.api.testing.auth.basic
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.dsl.ApiTestSuite
import dev.codersbox.eng.lib.api.testing.graphql.expectGraphQL

/**
 * Example test suite demonstrating GraphQL API testing
 * 
 * Note: These tests are commented out as they require an actual GraphQL API.
 * Uncomment and modify for your actual GraphQL API testing.
 */
class ExampleGraphQLApiTests : ApiTestSuite("GraphQL API Tests") {

    init {
        val testConfig = ApiTestConfig(
            baseUrl = "https://api.example.com",
            logRequests = true
        )

        /*
        scenario("Query user by ID") {
            var userId: String? = null

            step("Execute GraphQL query") {
                val response = graphql {
                    query = """
                        query GetUser(${'$'}id: ID!) {
                            user(id: ${'$'}id) {
                                id
                                name
                                email
                            }
                        }
                    """.trimIndent()
                    variables = mapOf("id" to "123")
                    auth(basic("user", "password"))
                }
                response.expectGraphQL {
                    noErrors()
                }
                response.expect {
                    status(200)
                    jsonPath("$.data.user.id") extractTo { userId = it.asString() }
                    jsonPath("$.data.user.name") shouldBe "Test User"
                }
            }

            step("Use captured user ID") {
                println("User ID from GraphQL: $userId")
            }
        }

        scenario("Create user mutation") {
            step("Execute mutation") {
                val response = graphql {
                    query = """
                        mutation CreateUser(${'$'}input: UserInput!) {
                            createUser(input: ${'$'}input) {
                                id
                                name
                                email
                            }
                        }
                    """.trimIndent()
                    variables = mapOf(
                        "input" to mapOf(
                            "name" to "New User",
                            "email" to "newuser@example.com"
                        )
                    )
                }
                response.expectGraphQL {
                    noErrors()
                }
                response.expect {
                    status(200)
                    jsonPath("$.data.createUser.name") shouldBe "New User"
                }
            }
        }
        */
    }
}
