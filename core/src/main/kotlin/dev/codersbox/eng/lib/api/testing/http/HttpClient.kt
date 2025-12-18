package dev.codersbox.eng.lib.api.testing.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.codersbox.eng.lib.api.testing.auth.ApiKeyLocation
import dev.codersbox.eng.lib.api.testing.auth.AuthConfig
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/**
 * HTTP client for making API requests
 */
class HttpClient(private val config: ApiTestConfig) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    
    private val ktorClient = io.ktor.client.HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeout.inWholeMilliseconds
            connectTimeoutMillis = config.connectTimeout.inWholeMilliseconds
            socketTimeoutMillis = config.socketTimeout.inWholeMilliseconds
        }

        if (config.logRequests || config.logResponses) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

        followRedirects = config.followRedirects

        engine {
            https {
                trustManager = if (!config.validateSsl) {
                    null // Allow all certificates in test environments
                } else {
                    null
                }
            }
        }
    }

    suspend fun execute(builder: RequestBuilder): ApiResponse {
        val startTime = System.currentTimeMillis()
        
        val response = ktorClient.request {
            method = builder.method.toKtorMethod()
            url(buildUrl(builder.path, builder.queryParams))
            
            // Set headers
            builder.headers.forEach { (key, value) ->
                header(key, value)
            }

            // Set auth
            applyAuth(builder.authConfig ?: config.defaultAuth)

            // Set body
            builder.bodyContent?.let {
                setBody(it)
                contentType(ContentType.Application.Json)
            }
        }

        val endTime = System.currentTimeMillis()
        val responseBody = response.bodyAsText()

        return ApiResponse(
            status = response.status,
            headers = response.headers,
            body = responseBody,
            contentType = response.contentType(),
            responseTimeMs = endTime - startTime
        )
    }

    private fun buildUrl(path: String, queryParams: Map<String, String>): String {
        val baseUrl = config.baseUrl.trimEnd('/')
        val normalizedPath = if (path.startsWith('/')) path else "/$path"
        val fullUrl = "$baseUrl$normalizedPath"
        
        return if (queryParams.isEmpty()) {
            fullUrl
        } else {
            val queryString = queryParams.entries.joinToString("&") { (key, value) ->
                "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
            }
            "$fullUrl?$queryString"
        }
    }

    private fun HttpRequestBuilder.applyAuth(authConfig: AuthConfig) {
        when (authConfig) {
            is AuthConfig.Basic -> {
                basicAuth(authConfig.username, authConfig.password)
            }
            is AuthConfig.Bearer -> {
                bearerAuth(authConfig.token)
            }
            is AuthConfig.ApiKey -> {
                when (authConfig.location) {
                    ApiKeyLocation.HEADER -> header(authConfig.key, authConfig.value)
                    ApiKeyLocation.QUERY_PARAM -> parameter(authConfig.key, authConfig.value)
                }
            }
            is AuthConfig.OAuth2 -> {
                // OAuth2 token would need to be fetched first
                // For now, this is a placeholder
                header("Authorization", "Bearer <oauth2-token>")
            }
            AuthConfig.None -> {
                // No authentication
            }
        }
    }

    private fun HttpMethod.toKtorMethod(): io.ktor.http.HttpMethod {
        return when (this) {
            HttpMethod.GET -> io.ktor.http.HttpMethod.Get
            HttpMethod.POST -> io.ktor.http.HttpMethod.Post
            HttpMethod.PUT -> io.ktor.http.HttpMethod.Put
            HttpMethod.PATCH -> io.ktor.http.HttpMethod.Patch
            HttpMethod.DELETE -> io.ktor.http.HttpMethod.Delete
            HttpMethod.HEAD -> io.ktor.http.HttpMethod.Head
            HttpMethod.OPTIONS -> io.ktor.http.HttpMethod.Options
        }
    }

    fun close() {
        ktorClient.close()
    }
}
