package dev.codersbox.eng.lib.demo

import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.dsl.ApiTestSuite
import dev.codersbox.eng.lib.api.testing.dsl.expectJsonPath
import dev.codersbox.eng.lib.api.testing.dsl.expectStatus

class ContextAndDataSharingDemoTest : ApiTestSuite(
    "Context and Data Sharing Demo",
    ApiTestConfig(baseUrl = "https://jsonplaceholder.typicode.com")
) {
    init {
        scenario("Fetch user and validate data") {
            step("Fetch user with ID 1") {
                val response = get("/users/1") {
                    headers["Accept"] = "application/json"
                }
                
                response.expectStatus(200)
                val userId = response.expectJsonPath("$.id").exists()
                val userName = response.expectJsonPath("$.name").exists()

                // Store user ID and name in context for later use
                save("userId", userId.getValue()!!)
                save("userName", userName.getValue()!!)
            }
            
            step("Verify stored user data") {
                val storedUserId = get("userId")!!
                val storedName = get("userName")!!
                val response = get("/users/$storedUserId") {
                    headers["Accept"] = "application/json"
                }
                
                response.expectStatus(200)
                response.expectJsonPath("$.id").equalsTo(storedUserId)
                response.expectJsonPath("$.name").equalsTo(storedName)
            }
        }

        scenario("Work with posts and comments") {
            step("Fetch a post") {
                val response = get("/posts/1") {
                    headers["Accept"] = "application/json"
                }

                response.expectStatus(200)
                val postId = response.expectJsonPath("$.id").exists()
                val postTitle = response.expectJsonPath("$.title").exists()

                // Store post ID and details in context
                save("postId", postId.getValue()!!)
                save("postTitle", postTitle.getValue()!!)
            }

            step("Fetch comments for the post") {
                val postId = get("postId")!!
                val response = get("/posts/$postId/comments") {
                    headers["Accept"] = "application/json"
                }

                response.expectStatus(200)
                val firstCommentId = response.expectJsonPath("$.[0].id").exists()
                response.expectJsonPath("$.[0].postId").equalsTo(postId)

                // Store first comment ID
                save("commentId", firstCommentId.getValue()!!)
            }
        }
    }
}
