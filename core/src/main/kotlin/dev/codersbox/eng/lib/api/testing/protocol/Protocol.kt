package dev.codersbox.eng.lib.api.testing.protocol

import dev.codersbox.eng.lib.api.testing.context.ScenarioContext

/**
 * Base interface for all protocol implementations
 */
interface Protocol {
    /**
     * Protocol identifier (e.g., "HTTP", "GRAPHQL", "GRPC")
     */
    val name: String
    
    /**
     * Initialize protocol-specific configuration
     */
    fun initialize(config: Map<String, Any>)
    
    /**
     * Execute a protocol-specific request
     */
    suspend fun execute(request: ProtocolRequest, context: ScenarioContext): ProtocolResponse
    
    /**
     * Cleanup protocol resources
     */
    fun cleanup()
}

/**
 * Generic protocol request
 */
data class ProtocolRequest(
    val operation: String,
    val target: String,
    val headers: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Generic protocol response
 */
data class ProtocolResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>> = emptyMap(),
    val body: String,
    val rawBody: ByteArray? = null,
    val metadata: Map<String, Any> = emptyMap()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProtocolResponse

        if (statusCode != other.statusCode) return false
        if (headers != other.headers) return false
        if (body != other.body) return false
        if (rawBody != null) {
            if (other.rawBody == null) return false
            if (!rawBody.contentEquals(other.rawBody)) return false
        } else if (other.rawBody != null) return false
        if (metadata != other.metadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + (rawBody?.contentHashCode() ?: 0)
        result = 31 * result + metadata.hashCode()
        return result
    }
}

/**
 * Registry for managing protocol implementations
 */
object ProtocolRegistry {
    private val protocols = mutableMapOf<String, Protocol>()
    
    /**
     * Register a protocol implementation
     */
    fun register(protocol: Protocol) {
        protocols[protocol.name.uppercase()] = protocol
    }
    
    /**
     * Get a protocol by name
     */
    fun get(name: String): Protocol {
        return protocols[name.uppercase()] 
            ?: throw IllegalArgumentException("Protocol not found: $name. Available: ${protocols.keys}")
    }
    
    /**
     * Check if protocol is registered
     */
    fun isRegistered(name: String): Boolean {
        return protocols.containsKey(name.uppercase())
    }
    
    /**
     * Get all registered protocols
     */
    fun all(): Map<String, Protocol> = protocols.toMap()
}
