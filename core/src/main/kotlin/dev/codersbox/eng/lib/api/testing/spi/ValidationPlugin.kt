package dev.codersbox.eng.lib.api.testing.spi

/**
 * Service Provider Interface for response validation plugins
 * (JSON Schema, OpenAPI Contract, Custom validators, etc.)
 */
interface ValidationPlugin {
    /**
     * Validator type identifier (e.g., "json-schema", "openapi", "custom")
     */
    val validatorType: String
    
    /**
     * Initialize the plugin
     */
    fun initialize(config: PluginConfiguration)
    
    /**
     * Validate response against rules/schema
     */
    fun validate(response: ProtocolResponse, rules: ValidationRules): ValidationResult
    
    /**
     * Load validation schema from file or URL
     */
    fun loadSchema(source: String): ValidationSchema
}

/**
 * Validation rules
 */
data class ValidationRules(
    val schema: ValidationSchema? = null,
    val statusCode: Int? = null,
    val jsonPath: Map<String, Any> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val customRules: List<CustomValidationRule> = emptyList()
)

/**
 * Validation schema wrapper
 */
interface ValidationSchema {
    val schemaType: String
    val content: String
}

/**
 * Custom validation rule
 */
interface CustomValidationRule {
    val ruleName: String
    fun validate(response: ProtocolResponse): ValidationResult
}
