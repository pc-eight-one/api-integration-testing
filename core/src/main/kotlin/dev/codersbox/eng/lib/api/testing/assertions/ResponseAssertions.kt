package dev.codersbox.eng.lib.api.testing.assertions

import com.jayway.jsonpath.JsonPath
import dev.codersbox.eng.lib.api.testing.http.ApiResponse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

/**
 * Extension functions for API response assertions
 */
class ResponseAssertions(private val response: ApiResponse) {

    /**
     * Assert status code
     */
    infix fun status(expectedStatus: Int) {
        response.statusCode shouldBe expectedStatus
    }

    /**
     * Assert response is successful (2xx)
     */
    fun isSuccess() {
        response.isSuccess shouldBe true
    }

    /**
     * Assert response body contains text
     */
    fun bodyContains(text: String) {
        response.body shouldContain text
    }

    /**
     * Assert response body equals
     */
    infix fun bodyEquals(expected: String) {
        response.body shouldBe expected
    }

    /**
     * Assert header exists and has value
     */
    fun header(name: String, expectedValue: String) {
        response.headers[name]?.firstOrNull() shouldBe expectedValue
    }

    /**
     * Assert header exists
     */
    fun headerExists(name: String) {
        response.headers[name] shouldNotBe null
    }

    /**
     * Extract value using JSONPath
     */
    fun jsonPath(path: String): JsonPathExtractor {
        return JsonPathExtractor(response.body, path)
    }

    /**
     * Assert JSONPath value equals
     */
    fun jsonPath(path: String, expectedValue: Any) {
        val actualValue = JsonPath.read<Any>(response.body, path)
        actualValue shouldBe expectedValue
    }

    /**
     * Assert response time is less than threshold
     */
    fun responseTimeUnder(thresholdMs: Long) {
        assert(response.responseTimeMs < thresholdMs) {
            "Response time ${response.responseTimeMs}ms exceeded threshold ${thresholdMs}ms"
        }
    }

    /**
     * Get the raw response for custom assertions
     */
    fun getResponse(): ApiResponse = response
}

/**
 * Helper for extracting values from JSON responses
 */
class JsonPathExtractor(private val json: String, private val path: String) {
    
    fun asString(): String = JsonPath.read(json, path)
    
    fun asInt(): Int = JsonPath.read(json, path)
    
    fun asLong(): Long = JsonPath.read(json, path)
    
    fun asDouble(): Double = JsonPath.read(json, path)
    
    fun asBoolean(): Boolean = JsonPath.read(json, path)
    
    fun <T> asList(): List<T> = JsonPath.read(json, path)
    
    fun <T> asObject(): T = JsonPath.read(json, path)

    /**
     * Extract and save to a variable
     */
    infix fun extractTo(block: (JsonPathExtractor) -> Unit) {
        block(this)
    }

    infix fun shouldBe(expected: Any) {
        val actual = JsonPath.read<Any>(json, path)
        actual shouldBe expected
    }
}

/**
 * Extension function to add expect block to ApiResponse
 */
fun ApiResponse.expect(block: ResponseAssertions.() -> Unit): ApiResponse {
    val assertions = ResponseAssertions(this)
    assertions.block()
    return this
}
