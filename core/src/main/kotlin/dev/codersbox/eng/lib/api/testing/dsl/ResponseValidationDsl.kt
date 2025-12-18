package dev.codersbox.eng.lib.api.testing.dsl

import dev.codersbox.eng.lib.api.testing.http.ApiResponse
import dev.codersbox.eng.lib.api.testing.validation.JsonPathExtractor
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch

/**
 * Extension functions for validating API responses
 */

fun ApiResponse.expectStatus(expectedStatus: Int) {
    statusCode shouldBe expectedStatus
}

fun ApiResponse.expectJsonPath(path: String): JsonPathAssertion {
    return JsonPathAssertion(this, path)
}

fun ApiResponse.expectHeader(name: String): HeaderAssertion {
    return HeaderAssertion(this, name)
}

fun ApiResponse.expectBody(expected: String) {
    body shouldBe expected
}

fun ApiResponse.expectBodyContains(text: String) {
    body shouldContain text
}

class JsonPathAssertion(private val response: ApiResponse, private val path: String) {
    private val extractor = JsonPathExtractor()
    
    fun exists(): JsonPathAssertion {
        val value = extractor.extract(response.body.toByteArray(), path)
        value shouldNotBe null
        return this
    }

    fun getValue(): Any? {
        return extractor.extract(response.body.toByteArray(), path)
    }
    
    fun equalsTo(expected: Any) {
        val value = extractor.extract(response.body.toByteArray(), path)
        value shouldBe expected
    }
    
    fun isNotEmpty() {
        val value = extractor.extract(response.body.toByteArray(), path)
        value shouldNotBe null
        when (value) {
            is String -> value.isNotEmpty() shouldBe true
            is Collection<*> -> value.isNotEmpty() shouldBe true
            is Array<*> -> value.isNotEmpty() shouldBe true
            else -> true shouldBe true
        }
    }
    
    fun isArray() {
        val value = extractor.extract(response.body.toByteArray(), path)
        (value is List<*> || value is Array<*>) shouldBe true
    }
    
    fun matches(regex: String) {
        val value = extractor.extract(response.body.toByteArray(), path)
        value.toString() shouldMatch regex.toRegex()
    }
}

class HeaderAssertion(private val response: ApiResponse, private val headerName: String) {
    fun exists() {
        response.headers[headerName] shouldNotBe null
    }
    
    fun equalsTo(expected: String) {
        response.headers[headerName] shouldBe expected
    }
    
    fun contains(text: String) {
        val headerValue = response.headers[headerName]
        headerValue shouldNotBe null
        headerValue!! shouldContain text
    }
}
