package dev.codersbox.eng.lib.api.testing.spi

/**
 * Service Provider Interface for content type serialization/deserialization plugins
 * (JSON, XML, CSV, Protobuf, etc.)
 */
interface ContentTypePlugin {
    /**
     * Content type identifier (e.g., "application/json", "application/xml")
     */
    val contentType: String
    
    /**
     * File extensions supported by this plugin
     */
    val fileExtensions: List<String>
    
    /**
     * Initialize the plugin
     */
    fun initialize(config: PluginConfiguration)
    
    /**
     * Serialize object to string
     */
    fun serialize(data: Any): String
    
    /**
     * Deserialize string to object
     */
    fun <T> deserialize(content: String, type: Class<T>): T
    
    /**
     * Check if this plugin can handle the content type
     */
    fun canHandle(contentType: String): Boolean
    
    /**
     * Validate content against schema (optional)
     */
    fun validate(content: String, schema: String? = null): ValidationResult
}

/**
 * Validation result
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)
