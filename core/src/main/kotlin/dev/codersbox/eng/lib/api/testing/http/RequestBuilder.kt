package dev.codersbox.eng.lib.api.testing.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.codersbox.eng.lib.api.testing.auth.AuthConfig
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig

/**
 * Builder for constructing HTTP requests
 */
class RequestBuilder(
    val method: HttpMethod,
    val path: String,
    private val config: ApiTestConfig
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    
    var authConfig: AuthConfig? = null
    val headers: MutableMap<String, String> = mutableMapOf()
    val queryParams: MutableMap<String, String> = mutableMapOf()
    var bodyContent: String? = null

    /**
     * Set authentication for this request
     */
    fun auth(auth: AuthConfig) {
        this.authConfig = auth
    }

    /**
     * Set bearer token authentication
     */
    fun auth(bearerToken: String) {
        this.authConfig = AuthConfig.Bearer(bearerToken)
    }

    /**
     * Add a header
     */
    fun header(key: String, value: String) {
        headers[key] = value
    }

    /**
     * Add multiple headers
     */
    fun headers(vararg pairs: Pair<String, String>) {
        headers.putAll(pairs)
    }

    /**
     * Add a query parameter
     */
    fun queryParam(key: String, value: String) {
        queryParams[key] = value
    }

    /**
     * Add multiple query parameters
     */
    fun queryParams(vararg pairs: Pair<String, String>) {
        queryParams.putAll(pairs)
    }

    /**
     * Set request body as string
     */
    fun body(content: String) {
        this.bodyContent = content
    }

    /**
     * Set request body from an object (will be serialized to JSON)
     */
    fun body(obj: Any) {
        this.bodyContent = objectMapper.writeValueAsString(obj)
    }

    /**
     * Set JSON body
     */
    fun json(content: String) {
        body(content)
        header("Content-Type", "application/json")
    }
}
