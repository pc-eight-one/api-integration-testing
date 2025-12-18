package dev.codersbox.eng.lib.api.testing.dsl

import dev.codersbox.eng.lib.api.testing.plugin.PluginRegistry
import dev.codersbox.eng.lib.api.testing.spi.*

/**
 * DSL extension to configure and use plugins
 */
class PluginConfigurationBuilder {
    private val protocols = mutableMapOf<String, PluginConfiguration>()
    private val contentTypes = mutableMapOf<String, PluginConfiguration>()
    private val authentications = mutableMapOf<String, AuthenticationConfig>()
    private val validators = mutableMapOf<String, PluginConfiguration>()
    
    /**
     * Configure a protocol plugin
     */
    fun protocol(name: String, config: PluginConfiguration.() -> Unit = {}) {
        val pluginConfig = PluginConfiguration().apply(config)
        protocols[name] = pluginConfig
        
        PluginRegistry.getProtocolPlugin(name)?.initialize(pluginConfig)
            ?: throw IllegalArgumentException("Protocol plugin '$name' not found")
    }
    
    /**
     * Configure a content type plugin
     */
    fun contentType(type: String, config: PluginConfiguration.() -> Unit = {}) {
        val pluginConfig = PluginConfiguration().apply(config)
        contentTypes[type] = pluginConfig
        
        PluginRegistry.getContentTypePlugin(type)?.initialize(pluginConfig)
            ?: throw IllegalArgumentException("Content type plugin '$type' not found")
    }
    
    /**
     * Configure an authentication plugin
     */
    fun authentication(type: String, config: AuthenticationConfig.() -> Unit) {
        val authConfig = AuthenticationConfig(type).apply(config)
        authentications[type] = authConfig
        
        PluginRegistry.getAuthenticationPlugin(type)?.initialize(authConfig)
            ?: throw IllegalArgumentException("Authentication plugin '$type' not found")
    }
    
    /**
     * Configure a validation plugin
     */
    fun validator(type: String, config: PluginConfiguration.() -> Unit = {}) {
        val pluginConfig = PluginConfiguration().apply(config)
        validators[type] = pluginConfig
        
        PluginRegistry.getValidationPlugin(type)?.initialize(pluginConfig)
            ?: throw IllegalArgumentException("Validation plugin '$type' not found")
    }
    
    internal fun build(): PluginConfigurations {
        return PluginConfigurations(protocols, contentTypes, authentications, validators)
    }
}

/**
 * Holder for all plugin configurations
 */
data class PluginConfigurations(
    val protocols: Map<String, PluginConfiguration>,
    val contentTypes: Map<String, PluginConfiguration>,
    val authentications: Map<String, AuthenticationConfig>,
    val validators: Map<String, PluginConfiguration>
)

/**
 * DSL function to configure plugins
 */
fun configurePlugins(block: PluginConfigurationBuilder.() -> Unit): PluginConfigurations {
    PluginRegistry.initialize()
    return PluginConfigurationBuilder().apply(block).build()
}

/**
 * Extension to list available plugins
 */
object PluginInfo {
    fun listProtocols(): List<String> = PluginRegistry.listProtocolPlugins()
    fun listContentTypes(): List<String> = PluginRegistry.listContentTypePlugins()
    fun listAuthentications(): List<String> = PluginRegistry.listAuthenticationPlugins()
    fun listValidators(): List<String> = PluginRegistry.listValidationPlugins()
    
    fun printAvailablePlugins() {
        println("Available Plugins:")
        println("  Protocols: ${listProtocols().joinToString()}")
        println("  Content Types: ${listContentTypes().joinToString()}")
        println("  Authentications: ${listAuthentications().joinToString()}")
        println("  Validators: ${listValidators().joinToString()}")
    }
}
