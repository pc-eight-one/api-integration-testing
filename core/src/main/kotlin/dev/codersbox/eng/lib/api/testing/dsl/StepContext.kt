package dev.codersbox.eng.lib.api.testing.dsl

import dev.codersbox.eng.lib.api.testing.auth.AuthConfig
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.context.ScenarioContext
import dev.codersbox.eng.lib.api.testing.graphql.GraphQLRequestBuilder
import dev.codersbox.eng.lib.api.testing.http.ApiResponse
import dev.codersbox.eng.lib.api.testing.http.HttpClient
import dev.codersbox.eng.lib.api.testing.http.HttpMethod
import dev.codersbox.eng.lib.api.testing.http.RequestBuilder

/**
 * Context for executing test steps
 */
class StepContext(
    val context: ScenarioContext,
    val config: ApiTestConfig
) {
    private val httpClient = HttpClient(config)

    /**
     * Make a GET request
     */
    suspend fun get(path: String, block: RequestBuilder.() -> Unit = {}): ApiResponse {
        return request(HttpMethod.GET, path, block)
    }

    /**
     * Make a POST request
     */
    suspend fun post(path: String, block: RequestBuilder.() -> Unit = {}): ApiResponse {
        return request(HttpMethod.POST, path, block)
    }

    /**
     * Make a PUT request
     */
    suspend fun put(path: String, block: RequestBuilder.() -> Unit = {}): ApiResponse {
        return request(HttpMethod.PUT, path, block)
    }

    /**
     * Make a PATCH request
     */
    suspend fun patch(path: String, block: RequestBuilder.() -> Unit = {}): ApiResponse {
        return request(HttpMethod.PATCH, path, block)
    }

    /**
     * Make a DELETE request
     */
    suspend fun delete(path: String, block: RequestBuilder.() -> Unit = {}): ApiResponse {
        return request(HttpMethod.DELETE, path, block)
    }

    /**
     * Make a custom HTTP request
     */
    suspend fun request(method: HttpMethod, path: String, block: RequestBuilder.() -> Unit = {}): ApiResponse {
        val builder = RequestBuilder(method, path, config)
        builder.block()
        return httpClient.execute(builder)
    }

    /**
     * Save a value to the context
     */
    fun save(key: String, value: Any) {
        context.save(key, value)
    }

    /**
     * Retrieve a value from the context
     */
    operator fun get(key: String): Any? = context[key]

    /**
     * Make a GraphQL request
     */
    suspend fun graphql(endpoint: String = "/graphql", block: GraphQLRequestBuilder.() -> Unit): ApiResponse {
        val builder = GraphQLRequestBuilder(config, endpoint)
        builder.block()
        return builder.execute()
    }
}
