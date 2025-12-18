package dev.codersbox.eng.lib.api.testing.plugins.rest

import dev.codersbox.eng.lib.api.testing.spi.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * REST/HTTP Protocol Plugin
 * Provides HTTP client functionality for REST API testing
 */
class RestProtocolPlugin : ProtocolPlugin {
    private lateinit var httpClient: HttpClient
    private var config: PluginConfiguration = PluginConfiguration()
    
    override val protocolName = "rest"
    override val version = "1.0.0"
    
    override fun initialize(config: PluginConfiguration) {
        this.config = config
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            // Apply timeout configuration
            engine {
                requestTimeout = config.timeout
            }
        }
    }
    
    override fun createExecutor(context: dev.codersbox.eng.lib.api.testing.core.TestContext): ProtocolExecutor {
        return RestProtocolExecutor(httpClient, config)
    }
    
    override fun supportedContentTypes(): List<String> {
        return listOf("application/json", "application/xml", "text/plain", "application/x-www-form-urlencoded")
    }
    
    override fun canHandle(request: ProtocolRequest): Boolean {
        return request.method.uppercase() in listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")
    }
}

/**
 * REST Protocol Executor Implementation
 */
class RestProtocolExecutor(
    private val httpClient: HttpClient,
    private val config: PluginConfiguration
) : ProtocolExecutor {
    
    override suspend fun execute(request: ProtocolRequest): ProtocolResponse {
        val startTime = System.currentTimeMillis()
        
        val httpResponse = when (request.method.uppercase()) {
            "GET" -> httpClient.get(request.url) {
                applyRequest(request)
            }
            "POST" -> httpClient.post(request.url) {
                applyRequest(request)
                request.body?.let { setBody(it) }
            }
            "PUT" -> httpClient.put(request.url) {
                applyRequest(request)
                request.body?.let { setBody(it) }
            }
            "PATCH" -> httpClient.patch(request.url) {
                applyRequest(request)
                request.body?.let { setBody(it) }
            }
            "DELETE" -> httpClient.delete(request.url) {
                applyRequest(request)
            }
            "HEAD" -> httpClient.head(request.url) {
                applyRequest(request)
            }
            "OPTIONS" -> httpClient.options(request.url) {
                applyRequest(request)
            }
            else -> throw UnsupportedOperationException("HTTP method ${request.method} not supported")
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
    
    private fun HttpRequestBuilder.applyRequest(request: ProtocolRequest) {
        // Apply headers
        request.headers.forEach { (key, value) ->
            header(key, value)
        }
    }
}
