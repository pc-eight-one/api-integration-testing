package dev.codersbox.eng.lib.api.testing.contenttype

import kotlin.reflect.KClass

/**
 * Interface for handling different content types (serialization/deserialization).
 * Implementations provide support for JSON, XML, CSV, YAML, Protobuf, etc.
 */
interface ContentTypeHandler {
    /**
     * List of media types this handler supports (e.g., ["application/json", "application/json; charset=utf-8"])
     */
    val supportedMediaTypes: List<String>
    
    /**
     * Serialize data object to byte array
     */
    fun serialize(data: Any): ByteArray
    
    /**
     * Deserialize byte array to target type
     */
    fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T
    
    /**
     * Check if this handler can process the given media type
     */
    fun canHandle(mediaType: String): Boolean {
        val normalizedMediaType = mediaType.split(";")[0].trim().lowercase()
        return supportedMediaTypes.any { 
            it.lowercase() == normalizedMediaType 
        }
    }
    
    /**
     * Get the default media type for this handler
     */
    fun defaultMediaType(): String = supportedMediaTypes.first()
}

/**
 * Extension function for easier deserialization
 */
inline fun <reified T : Any> ContentTypeHandler.deserialize(bytes: ByteArray): T {
    return deserialize(bytes, T::class)
}
