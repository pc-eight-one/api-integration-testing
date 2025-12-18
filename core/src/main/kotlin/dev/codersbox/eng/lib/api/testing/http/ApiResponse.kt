package dev.codersbox.eng.lib.api.testing.http

import io.ktor.http.*

/**
 * Represents an HTTP API response
 */
data class ApiResponse(
    val status: HttpStatusCode,
    val headers: Headers,
    val body: String,
    val contentType: ContentType?,
    val responseTimeMs: Long
) {
    val statusCode: Int get() = status.value
    val isSuccess: Boolean get() = status.isSuccess()
    val isRedirect: Boolean get() = status.value in 300..399
    val isClientError: Boolean get() = status.value in 400..499
    val isServerError: Boolean get() = status.value in 500..599
}
