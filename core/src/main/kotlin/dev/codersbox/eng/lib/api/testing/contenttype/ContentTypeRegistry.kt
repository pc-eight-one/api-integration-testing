package dev.codersbox.eng.lib.api.testing.contenttype

import dev.codersbox.eng.lib.api.testing.contenttype.handlers.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Central registry for content type handlers.
 * Supports auto-detection and custom handler registration.
 */
object ContentTypeRegistry {
    private val handlers = ConcurrentHashMap<String, ContentTypeHandler>()
    private val handlersList = mutableListOf<ContentTypeHandler>()
    
    init {
        // Register default handlers
        register(JsonContentHandler())
        register(XmlContentHandler())
        register(CsvContentHandler())
        register(YamlContentHandler())
        register(TextContentHandler())
        register(BinaryContentHandler())
        register(FormUrlEncodedHandler())
    }
    
    /**
     * Register a content type handler
     */
    fun register(handler: ContentTypeHandler) {
        handler.supportedMediaTypes.forEach { mediaType ->
            handlers[mediaType.lowercase()] = handler
        }
        if (!handlersList.contains(handler)) {
            handlersList.add(handler)
        }
    }
    
    /**
     * Get handler for specific media type
     */
    fun getHandler(mediaType: String): ContentTypeHandler {
        val normalizedMediaType = mediaType.split(";")[0].trim().lowercase()
        
        // Try exact match first
        handlers[normalizedMediaType]?.let { return it }
        
        // Try fuzzy match
        handlersList.firstOrNull { it.canHandle(mediaType) }?.let { return it }
        
        // Fallback to text handler
        return TextContentHandler()
    }
    
    /**
     * Auto-detect content type from byte array
     */
    fun autoDetect(content: ByteArray): ContentTypeHandler {
        // Try to detect JSON
        if (content.isNotEmpty() && (content[0].toInt().toChar() in listOf('{', '['))) {
            return handlers["application/json"] ?: JsonContentHandler()
        }
        
        // Try to detect XML
        val contentStr = content.decodeToString()
        if (contentStr.trimStart().startsWith("<?xml") || contentStr.trimStart().startsWith("<")) {
            return handlers["application/xml"] ?: XmlContentHandler()
        }
        
        // Default to text
        return TextContentHandler()
    }
    
    /**
     * Get all registered handlers
     */
    fun getAllHandlers(): List<ContentTypeHandler> = handlersList.toList()
    
    /**
     * Check if handler exists for media type
     */
    fun hasHandler(mediaType: String): Boolean {
        return handlersList.any { it.canHandle(mediaType) }
    }
}
