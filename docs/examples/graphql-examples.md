# GraphQL Examples

Complete examples demonstrating GraphQL API testing with the framework.

## Basic Query

```kotlin
import dev.codersbox.eng.lib.testing.dsl.*

class GraphQLBasicTest : FreeSpec({
    "GraphQL - Fetch user by ID" {
        apiTestSuite("GraphQL User API") {
            baseUrl = "https://api.example.com/graphql"
            
            scenario("Get user details") {
                step("Query user") {
                    graphql {
                        query = """
                            query GetUser($id: ID!) {
                                user(id: $id) {
                                    id
                                    name
                                    email
                                    posts {
                                        id
                                        title
                                    }
                                }
                            }
                        """
                        variables = mapOf("id" to "123")
                    }.expect {
                        status(200)
                        jsonPath("$.data.user.id").equals("123")
                        jsonPath("$.data.user.name").isNotEmpty()
                        jsonPath("$.data.user.posts").isArray()
                    }
                }
            }
        }
    }
})
```

## Mutation Example

```kotlin
"GraphQL - Create new post" {
    apiTestSuite("GraphQL Blog API") {
        baseUrl = "https://api.example.com/graphql"
        
        scenario("Create and verify post") {
            var postId = ""
            
            step("Create post") {
                graphql {
                    query = """
                        mutation CreatePost($input: PostInput!) {
                            createPost(input: $input) {
                                id
                                title
                                content
                                author {
                                    id
                                    name
                                }
                                createdAt
                            }
                        }
                    """
                    variables = mapOf(
                        "input" to mapOf(
                            "title" to "My New Post",
                            "content" to "This is the content",
                            "authorId" to "user-123"
                        )
                    )
                }.expect {
                    status(200)
                    jsonPath("$.data.createPost.id").isNotEmpty()
                    jsonPath("$.data.createPost.title").equals("My New Post")
                }.extract {
                    postId = jsonPath("$.data.createPost.id")
                }
            }
            
            step("Verify post was created") {
                graphql {
                    query = """
                        query GetPost($id: ID!) {
                            post(id: $id) {
                                id
                                title
                                content
                            }
                        }
                    """
                    variables = mapOf("id" to postId)
                }.expect {
                    status(200)
                    jsonPath("$.data.post.id").equals(postId)
                    jsonPath("$.data.post.title").equals("My New Post")
                }
            }
        }
    }
}
```

See [Protocol Plugins Guide](../guides/protocol-plugins/graphql.md) for more details.
