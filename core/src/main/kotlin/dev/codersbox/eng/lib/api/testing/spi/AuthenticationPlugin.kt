package dev.codersbox.eng.lib.api.testing.spi

/**
 * Service Provider Interface for authentication plugins
 * (OAuth2, JWT, API Key, Basic Auth, Custom, etc.)
 */
interface AuthenticationPlugin {
    /**
     * Authentication type identifier (e.g., "oauth2", "jwt", "api-key")
     */
    val authType: String
    
    /**
     * Initialize the plugin with credentials and configuration
     */
    fun initialize(config: AuthenticationConfig)
    
    /**
     * Apply authentication to request headers
     */
    suspend fun applyAuthentication(headers: MutableMap<String, String>): Map<String, String>
    
    /**
     * Refresh authentication if expired (e.g., token refresh)
     */
    suspend fun refreshAuthentication(): Boolean
    
    /**
     * Check if authentication is still valid
     */
    suspend fun isValid(): Boolean
    
    /**
     * Clear/logout authentication
     */
    suspend fun clear()
}

/**
 * Authentication configuration
 */
data class AuthenticationConfig(
    val type: String,
    val credentials: Map<String, String> = emptyMap(),
    val tokenUrl: String? = null,
    val refreshUrl: String? = null,
    val scope: String? = null,
    val additionalParams: Map<String, Any> = emptyMap()
)
