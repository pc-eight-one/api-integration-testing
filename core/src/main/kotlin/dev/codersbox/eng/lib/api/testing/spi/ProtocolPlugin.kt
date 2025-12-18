package dev.codersbox.eng.lib.api.testing.spi

import dev.codersbox.eng.lib.api.testing.core.TestContext

/**
 * Service Provider Interface for protocol plugins (REST, GraphQL, SOAP, gRPC, etc.)
 * 
 * Implementations should be registered via Java ServiceLoader mechanism.
 */
interface ProtocolPlugin {
    /**
     * Unique identifier for this protocol (e.g., "rest", "graphql", "soap")
     */
    val protocolName: String
    
    /**
     * Protocol version supported by this plugin
     */
    val version: String
    
    /**
     * Initialize the plugin with configuration
     */
    fun initialize(config: PluginConfiguration)
    
    /**
     * Create a client executor for this protocol
     */
    fun createExecutor(context: TestContext): ProtocolExecutor
    
    /**
     * Get supported content types for this protocol
     */
    fun supportedContentTypes(): List<String>
    
    /**
     * Validate if this plugin can handle the given request
     */
    fun canHandle(request: ProtocolRequest): Boolean
}

/**
 * Base interface for protocol-specific request execution
 */
interface ProtocolExecutor {
    /**
     * Execute a protocol-specific request
     */
    suspend fun execute(request: ProtocolRequest): ProtocolResponse
    
    /**
     * Close and cleanup resources
     */
    fun close()
}

/**
 * Generic protocol request wrapper
 */
interface ProtocolRequest {
    val url: String
    val method: String
    val headers: Map<String, String>
    val body: Any?
    val metadata: Map<String, Any>
}

/**
 * Generic protocol response wrapper
 */
interface ProtocolResponse {
    val statusCode: Int
    val headers: Map<String, List<String>>
    val body: String
    val responseTime: Long
    val metadata: Map<String, Any>
}

/**
 * Plugin configuration holder
 */
data class PluginConfiguration(
    val properties: Map<String, Any> = emptyMap(),
    val baseUrl: String? = null,
    val timeout: Long = 30000,
    val retryConfig: RetryConfig? = null
)

/**
 * Retry configuration for plugins
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelay: Long = 1000,
    val maxDelay: Long = 10000,
    val multiplier: Double = 2.0
)
