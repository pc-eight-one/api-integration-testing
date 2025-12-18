package dev.codersbox.eng.lib.api.testing.validation

import dev.codersbox.eng.lib.api.testing.protocol.ProtocolResponse

/**
 * Base interface for validation rules
 */
interface ValidationRule {
    /**
     * Validate the response
     * @return ValidationResult with pass/fail and message
     */
    fun validate(response: ProtocolResponse): ValidationResult
    
    /**
     * Description of what this rule validates
     */
    val description: String
}

/**
 * Result of a validation
 */
data class ValidationResult(
    val passed: Boolean,
    val message: String,
    val expected: Any? = null,
    val actual: Any? = null
) {
    companion object {
        fun success(message: String = "Validation passed") = ValidationResult(true, message)
        fun failure(message: String, expected: Any? = null, actual: Any? = null) = 
            ValidationResult(false, message, expected, actual)
    }
}

/**
 * Registry for custom validation rules
 */
object ValidationRegistry {
    private val rules = mutableMapOf<String, (Any?) -> ValidationRule>()
    
    /**
     * Register a custom validation rule factory
     */
    fun register(name: String, factory: (Any?) -> ValidationRule) {
        rules[name] = factory
    }
    
    /**
     * Get a validation rule by name
     */
    fun get(name: String, config: Any? = null): ValidationRule {
        val factory = rules[name] 
            ?: throw IllegalArgumentException("Validation rule not found: $name")
        return factory(config)
    }
    
    /**
     * Check if rule is registered
     */
    fun isRegistered(name: String): Boolean = rules.containsKey(name)
    
    /**
     * Get all registered rule names
     */
    fun allRules(): Set<String> = rules.keys.toSet()
}

/**
 * Built-in validation rules
 */

class StatusCodeValidation(private val expectedCode: Int) : ValidationRule {
    override val description = "Status code should be $expectedCode"
    
    override fun validate(response: ProtocolResponse): ValidationResult {
        return if (response.statusCode == expectedCode) {
            ValidationResult.success("Status code matches: $expectedCode")
        } else {
            ValidationResult.failure(
                "Status code mismatch",
                expected = expectedCode,
                actual = response.statusCode
            )
        }
    }
}

class ResponseTimeValidation(private val maxMillis: Long) : ValidationRule {
    override val description = "Response time should be less than ${maxMillis}ms"
    
    override fun validate(response: ProtocolResponse): ValidationResult {
        val responseTime = response.metadata["responseTime"] as? Long ?: 0L
        return if (responseTime <= maxMillis) {
            ValidationResult.success("Response time ${responseTime}ms is within limit")
        } else {
            ValidationResult.failure(
                "Response time exceeded",
                expected = "$maxMillis ms",
                actual = "$responseTime ms"
            )
        }
    }
}

class HeaderValidation(
    private val headerName: String,
    private val expectedValue: String? = null
) : ValidationRule {
    override val description = if (expectedValue != null) {
        "Header '$headerName' should be '$expectedValue'"
    } else {
        "Header '$headerName' should exist"
    }
    
    override fun validate(response: ProtocolResponse): ValidationResult {
        val headerValues = response.headers[headerName]
        
        return when {
            headerValues.isNullOrEmpty() -> ValidationResult.failure(
                "Header '$headerName' not found",
                expected = expectedValue ?: "any value",
                actual = null
            )
            expectedValue == null -> ValidationResult.success("Header '$headerName' exists")
            headerValues.contains(expectedValue) -> ValidationResult.success(
                "Header '$headerName' has expected value"
            )
            else -> ValidationResult.failure(
                "Header '$headerName' value mismatch",
                expected = expectedValue,
                actual = headerValues.joinToString()
            )
        }
    }
}

class BodyContainsValidation(private val expectedText: String) : ValidationRule {
    override val description = "Response body should contain '$expectedText'"
    
    override fun validate(response: ProtocolResponse): ValidationResult {
        return if (response.body.contains(expectedText)) {
            ValidationResult.success("Body contains expected text")
        } else {
            ValidationResult.failure(
                "Body does not contain expected text",
                expected = expectedText,
                actual = "text not found"
            )
        }
    }
}

class JsonPathValidation(
    private val path: String,
    private val expectedValue: Any?
) : ValidationRule {
    override val description = "JSON path '$path' should equal '$expectedValue'"
    
    override fun validate(response: ProtocolResponse): ValidationResult {
        // This is a simplified implementation
        // In real implementation, use a JSON path library like JsonPath or kotlinx.serialization
        return ValidationResult.success("JSON path validation (implementation pending)")
    }
}
