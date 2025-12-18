package dev.codersbox.eng.lib.cli.generation

import net.datafaker.Faker
import kotlin.random.Random

class TestDataGenerator {
    private val faker = Faker()

    fun generateForSchema(schema: Schema, spec: OpenApiSpec, resolvedRefs: MutableSet<String> = mutableSetOf()): Any? {
        // Handle $ref
        schema.`$ref`?.let { ref ->
            if (ref in resolvedRefs) {
                return null // Avoid circular references
            }
            resolvedRefs.add(ref)
            val refSchema = resolveRef(ref, spec)
            return refSchema?.let { generateForSchema(it, spec, resolvedRefs) }
        }

        // Use example if provided
        schema.example?.let { return it }

        return when (schema.type?.lowercase()) {
            "string" -> generateString(schema)
            "integer" -> generateInteger(schema)
            "number" -> generateNumber(schema)
            "boolean" -> Random.nextBoolean()
            "array" -> generateArray(schema, spec, resolvedRefs)
            "object" -> generateObject(schema, spec, resolvedRefs)
            null -> generateString(schema) // Default to string
            else -> null
        }
    }

    private fun generateString(schema: Schema): String {
        // Check enum first
        schema.enum?.let { return it.random() }

        return when (schema.format?.lowercase()) {
            "email" -> faker.internet().emailAddress()
            "uri", "url" -> faker.internet().url()
            "uuid" -> faker.internet().uuid()
            "date" -> faker.date().birthday().toString().substring(0, 10)
            "date-time" -> faker.date().birthday().toInstant().toString()
            "password" -> faker.internet().password(8, 16, true, true, true)
            "byte" -> faker.internet().base64()
            "binary" -> faker.internet().base64()
            "ipv4" -> faker.internet().ipV4Address()
            "ipv6" -> faker.internet().ipV6Address()
            "hostname" -> faker.internet().domainName()
            else -> generateRandomString(schema)
        }
    }

    private fun generateRandomString(schema: Schema): String {
        val minLength = schema.minLength ?: 5
        val maxLength = schema.maxLength ?: 20
        
        schema.pattern?.let {
            // For common patterns
            return when {
                it.contains("@") -> faker.internet().emailAddress()
                it.contains("http") -> faker.internet().url()
                it.contains("[0-9]") -> faker.number().digits(minLength)
                else -> faker.lorem().characters(minLength, maxLength)
            }
        }

        return faker.lorem().characters(minLength, maxLength)
    }

    private fun generateInteger(schema: Schema): Int {
        val min = schema.minimum?.toInt() ?: 1
        val max = schema.maximum?.toInt() ?: 1000
        return Random.nextInt(min, max + 1)
    }

    private fun generateNumber(schema: Schema): Double {
        val min = schema.minimum?.toDouble() ?: 1.0
        val max = schema.maximum?.toDouble() ?: 1000.0
        return Random.nextDouble(min, max)
    }

    private fun generateArray(schema: Schema, spec: OpenApiSpec, resolvedRefs: MutableSet<String>): List<Any?> {
        schema.items?.let { itemSchema ->
            val size = Random.nextInt(1, 4) // Generate 1-3 items
            return (1..size).map { generateForSchema(itemSchema, spec, resolvedRefs.toMutableSet()) }
        }
        return emptyList()
    }

    private fun generateObject(schema: Schema, spec: OpenApiSpec, resolvedRefs: MutableSet<String>): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        
        schema.properties?.forEach { (propName, propSchema) ->
            val isRequired = schema.required?.contains(propName) == true
            if (isRequired || Random.nextBoolean()) {
                result[propName] = generateForSchema(propSchema, spec, resolvedRefs.toMutableSet())
            }
        }
        
        return result
    }

    private fun resolveRef(ref: String, spec: OpenApiSpec): Schema? {
        // Handle #/components/schemas/SchemaName
        if (ref.startsWith("#/components/schemas/")) {
            val schemaName = ref.substringAfterLast("/")
            return spec.components?.schemas?.get(schemaName)
        }
        return null
    }

    fun generatePathParams(parameters: List<Parameter>): Map<String, String> {
        return parameters
            .filter { it.`in` == "path" }
            .associate { param ->
                param.name to when (param.schema.type?.lowercase()) {
                    "integer" -> faker.number().numberBetween(1, 1000).toString()
                    "string" -> when (param.schema.format?.lowercase()) {
                        "uuid" -> faker.internet().uuid()
                        else -> faker.lorem().word()
                    }
                    else -> faker.lorem().word()
                }
            }
    }

    fun generateQueryParams(parameters: List<Parameter>): Map<String, String> {
        return parameters
            .filter { it.`in` == "query" }
            .filter { it.required || Random.nextBoolean() }
            .associate { param ->
                param.name to when (param.schema.type?.lowercase()) {
                    "integer" -> faker.number().numberBetween(1, 100).toString()
                    "boolean" -> Random.nextBoolean().toString()
                    else -> faker.lorem().word()
                }
            }
    }

    fun generateHeaders(parameters: List<Parameter>): Map<String, String> {
        return parameters
            .filter { it.`in` == "header" }
            .filter { it.required || Random.nextBoolean() }
            .associate { param ->
                param.name to faker.lorem().word()
            }
    }
}
