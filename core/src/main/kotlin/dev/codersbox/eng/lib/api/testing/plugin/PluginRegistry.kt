package dev.codersbox.eng.lib.api.testing.plugin

import dev.codersbox.eng.lib.api.testing.spi.*
import org.slf4j.LoggerFactory
import java.util.*

/**
 * Central registry for managing all plugins
 * Uses Java ServiceLoader for automatic plugin discovery
 */
object PluginRegistry {
    private val logger = LoggerFactory.getLogger(PluginRegistry::class.java)
    
    private val protocolPlugins = mutableMapOf<String, ProtocolPlugin>()
    private val contentTypePlugins = mutableMapOf<String, ContentTypePlugin>()
    private val authenticationPlugins = mutableMapOf<String, AuthenticationPlugin>()
    private val validationPlugins = mutableMapOf<String, ValidationPlugin>()
    
    private var initialized = false
    
    /**
     * Initialize and discover all plugins
     */
    fun initialize() {
        if (initialized) return
        
        logger.info("Initializing Plugin Registry...")
        
        // Discover protocol plugins
        ServiceLoader.load(ProtocolPlugin::class.java).forEach { plugin ->
            registerProtocolPlugin(plugin)
        }
        
        // Discover content type plugins
        ServiceLoader.load(ContentTypePlugin::class.java).forEach { plugin ->
            registerContentTypePlugin(plugin)
        }
        
        // Discover authentication plugins
        ServiceLoader.load(AuthenticationPlugin::class.java).forEach { plugin ->
            registerAuthenticationPlugin(plugin)
        }
        
        // Discover validation plugins
        ServiceLoader.load(ValidationPlugin::class.java).forEach { plugin ->
            registerValidationPlugin(plugin)
        }
        
        initialized = true
        logger.info("Plugin Registry initialized with:")
        logger.info("  - ${protocolPlugins.size} protocol plugin(s)")
        logger.info("  - ${contentTypePlugins.size} content type plugin(s)")
        logger.info("  - ${authenticationPlugins.size} authentication plugin(s)")
        logger.info("  - ${validationPlugins.size} validation plugin(s)")
    }
    
    /**
     * Register a protocol plugin
     */
    fun registerProtocolPlugin(plugin: ProtocolPlugin) {
        protocolPlugins[plugin.protocolName] = plugin
        logger.debug("Registered protocol plugin: ${plugin.protocolName} v${plugin.version}")
    }
    
    /**
     * Register a content type plugin
     */
    fun registerContentTypePlugin(plugin: ContentTypePlugin) {
        contentTypePlugins[plugin.contentType] = plugin
        logger.debug("Registered content type plugin: ${plugin.contentType}")
    }
    
    /**
     * Register an authentication plugin
     */
    fun registerAuthenticationPlugin(plugin: AuthenticationPlugin) {
        authenticationPlugins[plugin.authType] = plugin
        logger.debug("Registered authentication plugin: ${plugin.authType}")
    }
    
    /**
     * Register a validation plugin
     */
    fun registerValidationPlugin(plugin: ValidationPlugin) {
        validationPlugins[plugin.validatorType] = plugin
        logger.debug("Registered validation plugin: ${plugin.validatorType}")
    }
    
    /**
     * Get protocol plugin by name
     */
    fun getProtocolPlugin(name: String): ProtocolPlugin? {
        if (!initialized) initialize()
        return protocolPlugins[name]
    }
    
    /**
     * Get content type plugin
     */
    fun getContentTypePlugin(contentType: String): ContentTypePlugin? {
        if (!initialized) initialize()
        return contentTypePlugins[contentType] 
            ?: contentTypePlugins.values.find { it.canHandle(contentType) }
    }
    
    /**
     * Get authentication plugin
     */
    fun getAuthenticationPlugin(authType: String): AuthenticationPlugin? {
        if (!initialized) initialize()
        return authenticationPlugins[authType]
    }
    
    /**
     * Get validation plugin
     */
    fun getValidationPlugin(validatorType: String): ValidationPlugin? {
        if (!initialized) initialize()
        return validationPlugins[validatorType]
    }
    
    /**
     * List all registered protocol plugins
     */
    fun listProtocolPlugins(): List<String> {
        if (!initialized) initialize()
        return protocolPlugins.keys.toList()
    }
    
    /**
     * List all registered content type plugins
     */
    fun listContentTypePlugins(): List<String> {
        if (!initialized) initialize()
        return contentTypePlugins.keys.toList()
    }
    
    /**
     * List all registered authentication plugins
     */
    fun listAuthenticationPlugins(): List<String> {
        if (!initialized) initialize()
        return authenticationPlugins.keys.toList()
    }
    
    /**
     * List all registered validation plugins
     */
    fun listValidationPlugins(): List<String> {
        if (!initialized) initialize()
        return validationPlugins.keys.toList()
    }
    
    /**
     * Clear all plugins (useful for testing)
     */
    fun clear() {
        protocolPlugins.clear()
        contentTypePlugins.clear()
        authenticationPlugins.clear()
        validationPlugins.clear()
        initialized = false
    }
}
