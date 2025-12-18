package dev.codersbox.eng.lib.api.testing.plugins.graphql

import dev.codersbox.eng.lib.api.testing.spi.*
import dev.codersbox.eng.lib.api.testing.core.TestContext
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * GraphQL Protocol Plugin
 * Provides GraphQL query and mutation support
 */
class GraphQLProtocolPlugin : ProtocolPlugin {
    private lateinit var httpClient: HttpClient
    private var config: PluginConfiguration = PluginConfiguration()
    private val json = Json { ignoreUnknownKeys = true }
    
    override val protocolName = "graphql"
    override val version = "1.0.0"
    
    override fun initialize(config: PluginConfiguration) {
        this.config = config
        httpClient = HttpClient(CIO) {
            engine {
                requestTimeout = config.timeout
            }
        }
    }
    
    override fun createExecutor(context: TestContext): ProtocolExecutor {
        return GraphQLProtocolExecutor(httpClient, json)
    }
    
    override fun supportedContentTypes(): List<String> {
        return listOf("application/json", "application/graphql")
    }
    
    override fun canHandle(request: ProtocolRequest): Boolean {
        return request.method.uppercase() == "POST" && 
               (request.metadata["type"] == "graphql" || request.body.toString().contains("query") || request.body.toString().contains("mutation"))
    }
}

/**
 * GraphQL Protocol Executor Implementation
 */
class GraphQLProtocolExecutor(
    private val httpClient: HttpClient,
    private val json: Json
) : ProtocolExecutor {
    
    override suspend fun execute(request: ProtocolRequest): ProtocolResponse {
        val startTime = System.currentTimeMillis()
        
        // Extract GraphQL specific data from request
        val query = request.body as? String ?: throw IllegalArgumentException("GraphQL query is required")
        val variables = request.metadata["variables"] as? Map<*, *>
        val operationName = request.headers["X-GraphQL-Operation-Name"]
        
        val graphQLRequest = GraphQLRequest(
            query = query,
            variables = variables?.let { json.encodeToJsonElement(it) },
            operationName = operationName
        )
        
        val httpResponse = httpClient.post(request.url) {
            contentType(ContentType.Application.Json)
            
            // Apply headers
            request.headers.forEach { (key, value) ->
                if (key != "X-GraphQL-Operation-Name") {
                    header(key, value)
                }
            }
            
            // Manually build JSON to avoid serialization issues
            val requestBody = buildJsonObject {
                put("query", graphQLRequest.query)
                graphQLRequest.variables?.let { put("variables", it) }
                graphQLRequest.operationName?.let { put("operationName", it) }
            }
            setBody(requestBody.toString())
        }
        
        val responseTime = System.currentTimeMillis() - startTime
        val responseBody = httpResponse.bodyAsText()
        val responseHeaders: Map<String, List<String>> = httpResponse.headers.names()
            .associateWith { name -> httpResponse.headers.getAll(name) ?: emptyList() }
        
        return object : ProtocolResponse {
            override val statusCode = httpResponse.status.value
            override val headers = responseHeaders
            override val body = responseBody
            override val responseTime = responseTime
            override val metadata = emptyMap<String, Any>()
        }
    }
    
    override fun close() {
        httpClient.close()
    }
}

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: JsonElement? = null,
    val operationName: String? = null
)
