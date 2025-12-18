package dev.codersbox.eng.lib.api.testing.graphql

import dev.codersbox.eng.lib.api.testing.auth.AuthConfig
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.http.ApiResponse
import dev.codersbox.eng.lib.api.testing.http.HttpClient
import dev.codersbox.eng.lib.api.testing.http.HttpMethod
import dev.codersbox.eng.lib.api.testing.http.RequestBuilder

/**
 * GraphQL request builder
 */
class GraphQLRequestBuilder(
    private val config: ApiTestConfig,
    private val endpoint: String = "/graphql"
) {
    var query: String = ""
    var variables: Map<String, Any> = emptyMap()
    var operationName: String? = null
    var authConfig: AuthConfig? = null
    private val headers: MutableMap<String, String> = mutableMapOf()

    /**
     * Set authentication
     */
    fun auth(auth: AuthConfig) {
        this.authConfig = auth
    }

    /**
     * Set bearer token authentication
     */
    fun auth(bearerToken: String) {
        this.authConfig = AuthConfig.Bearer(bearerToken)
    }

    /**
     * Add a header
     */
    fun header(key: String, value: String) {
        headers[key] = value
    }

    /**
     * Execute the GraphQL request
     */
    suspend fun execute(): ApiResponse {
        val httpClient = HttpClient(config)
        
        val requestBody = buildGraphQLRequest()
        
        val requestBuilder = RequestBuilder(HttpMethod.POST, endpoint, config).apply {
            this.authConfig = this@GraphQLRequestBuilder.authConfig
            headers.putAll(this@GraphQLRequestBuilder.headers)
            json(requestBody)
        }

        return httpClient.execute(requestBuilder)
    }

    private fun buildGraphQLRequest(): String {
        val request = mutableMapOf<String, Any>(
            "query" to query
        )
        
        if (variables.isNotEmpty()) {
            request["variables"] = variables
        }
        
        if (operationName != null) {
            request["operationName"] = operationName!!
        }

        return com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            .writeValueAsString(request)
    }
}

/**
 * GraphQL-specific assertions
 */
class GraphQLAssertions(private val response: ApiResponse) {
    
    /**
     * Assert no GraphQL errors
     */
    fun noErrors() {
        assert(!response.body.contains("\"errors\"")) {
            "GraphQL response contains errors: ${response.body}"
        }
    }

    /**
     * Assert GraphQL errors exist
     */
    fun hasErrors() {
        assert(response.body.contains("\"errors\"")) {
            "Expected GraphQL errors but none found"
        }
    }

    /**
     * Get data field from GraphQL response
     */
    fun data(): String {
        // Extract the data field from the response
        return com.jayway.jsonpath.JsonPath.read(response.body, "$.data")
    }
}

/**
 * Extension function for GraphQL assertions
 */
fun ApiResponse.expectGraphQL(block: GraphQLAssertions.() -> Unit): ApiResponse {
    val assertions = GraphQLAssertions(this)
    assertions.block()
    return this
}
