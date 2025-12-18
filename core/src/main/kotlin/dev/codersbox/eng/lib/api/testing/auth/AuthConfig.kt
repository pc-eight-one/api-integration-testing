package dev.codersbox.eng.lib.api.testing.auth

/**
 * Authentication configuration for API requests
 */
sealed class AuthConfig {
    data class Basic(val username: String, val password: String) : AuthConfig()
    data class Bearer(val token: String) : AuthConfig()
    data class ApiKey(val key: String, val value: String, val location: ApiKeyLocation = ApiKeyLocation.HEADER) : AuthConfig()
    data class OAuth2(
        val clientId: String,
        val clientSecret: String,
        val tokenUrl: String,
        val scope: String? = null
    ) : AuthConfig()
    object None : AuthConfig()
}

enum class ApiKeyLocation {
    HEADER,
    QUERY_PARAM
}

/**
 * DSL functions for creating auth configurations
 */
fun basic(username: String, password: String) = AuthConfig.Basic(username, password)

fun bearerToken(token: String) = AuthConfig.Bearer(token)

fun apiKey(key: String, value: String, location: ApiKeyLocation = ApiKeyLocation.HEADER) = 
    AuthConfig.ApiKey(key, value, location)

fun oauth2(clientId: String, clientSecret: String, tokenUrl: String, scope: String? = null) = 
    AuthConfig.OAuth2(clientId, clientSecret, tokenUrl, scope)
