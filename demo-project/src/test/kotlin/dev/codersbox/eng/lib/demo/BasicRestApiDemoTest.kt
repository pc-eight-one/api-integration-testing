package dev.codersbox.eng.lib.demo

import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.dsl.ApiTestSuite
import dev.codersbox.eng.lib.api.testing.dsl.expectJsonPath
import dev.codersbox.eng.lib.api.testing.dsl.expectStatus

class BasicRestApiDemoTest : ApiTestSuite(
    "Basic REST API Demo",
    ApiTestConfig(baseUrl = "https://jsonplaceholder.typicode.com")
) {
    init {
        scenario("Get all users") {
            step("Fetch users list") {
                val response = get("/users") {
                    headers["Accept"] = "application/json"
                }
                
                response.expectStatus(200)
                response.expectJsonPath("$").isArray()
                response.expectJsonPath("$[0].id").exists()
                response.expectJsonPath("$[0].name").exists()
            }
        }

        scenario("Get single user") {
            step("Fetch user by ID") {
                val response = get("/users/1") {
                    headers["Accept"] = "application/json"
                }
                
                response.expectStatus(200)
                response.expectJsonPath("$.id").equalsTo(1)
                response.expectJsonPath("$.name").exists()
                response.expectJsonPath("$.email").exists()
            }
        }

        scenario("Create new user") {
            step("Post new user") {
                val response = post("/users") {
                    headers["Content-Type"] = "application/json"
                    body("""
                        {
                            "name": "John Doe",
                            "username": "johndoe",
                            "email": "john@example.com"
                        }
                    """.trimIndent())
                }
                
                response.expectStatus(201)
                response.expectJsonPath("$.id").exists()
            }
        }

        scenario("Update user") {
            step("Update user details") {
                val response = put("/users/1") {
                    headers["Content-Type"] = "application/json"
                    body("""
                        {
                            "id": 1,
                            "name": "Jane Doe",
                            "username": "janedoe",
                            "email": "jane@example.com"
                        }
                    """.trimIndent())
                }
                
                response.expectStatus(200)
                response.expectJsonPath("$.name").equalsTo("Jane Doe")
            }
        }

        scenario("Delete user") {
            step("Delete user by ID") {
                val response = delete("/users/1") {
                    headers["Accept"] = "application/json"
                }
                
                response.expectStatus(200)
            }
        }
    }
}
