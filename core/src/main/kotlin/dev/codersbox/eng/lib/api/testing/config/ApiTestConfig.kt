package dev.codersbox.eng.lib.api.testing.config

import dev.codersbox.eng.lib.api.testing.auth.AuthConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Global configuration for API tests
 */
data class ApiTestConfig(
    val baseUrl: String = "",
    val defaultAuth: AuthConfig = AuthConfig.None,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val connectTimeout: Duration = 30.seconds,
    val requestTimeout: Duration = 60.seconds,
    val socketTimeout: Duration = 60.seconds,
    val followRedirects: Boolean = true,
    val validateSsl: Boolean = true,
    val logRequests: Boolean = false,
    val logResponses: Boolean = false
) {
    companion object {
        private var globalConfig = ApiTestConfig()

        fun configure(block: ApiTestConfig.() -> ApiTestConfig) {
            globalConfig = globalConfig.block()
        }

        fun get(): ApiTestConfig = globalConfig

        fun reset() {
            globalConfig = ApiTestConfig()
        }
    }
}
