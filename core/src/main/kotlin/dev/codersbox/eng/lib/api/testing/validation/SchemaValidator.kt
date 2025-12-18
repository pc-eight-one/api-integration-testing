package dev.codersbox.eng.lib.api.testing.validation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.io.File

interface SchemaValidator {
    fun validate(content: ByteArray): SchemaValidationResult
}

data class SchemaValidationResult(
    val valid: Boolean,
    val errors: List<String> = emptyList()
)

class JsonSchemaValidator(private val schema: JsonSchema) : SchemaValidator {
    companion object {
        private val objectMapper = ObjectMapper()
        private val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
        
        fun fromFile(schemaFile: File): JsonSchemaValidator {
            val schemaNode = objectMapper.readTree(schemaFile)
            return JsonSchemaValidator(schemaFactory.getSchema(schemaNode))
        }
        
        fun fromString(schemaJson: String): JsonSchemaValidator {
            val schemaNode = objectMapper.readTree(schemaJson)
            return JsonSchemaValidator(schemaFactory.getSchema(schemaNode))
        }
        
        fun fromResource(resourcePath: String): JsonSchemaValidator {
            val schemaStream = JsonSchemaValidator::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("Schema resource not found: $resourcePath")
            val schemaNode = objectMapper.readTree(schemaStream)
            return JsonSchemaValidator(schemaFactory.getSchema(schemaNode))
        }
    }
    
    override fun validate(content: ByteArray): SchemaValidationResult {
        val jsonNode: JsonNode = objectMapper.readTree(content)
        val errors = schema.validate(jsonNode)
        
        return if (errors.isEmpty()) {
            SchemaValidationResult(true)
        } else {
            SchemaValidationResult(false, errors.map { it.message })
        }
    }
}

class XmlSchemaValidator(private val schemaContent: String) : SchemaValidator {
    override fun validate(content: ByteArray): SchemaValidationResult {
        return try {
            val factory = javax.xml.validation.SchemaFactory.newInstance(
                javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI
            )
            val schema = factory.newSchema(
                javax.xml.transform.stream.StreamSource(java.io.StringReader(schemaContent))
            )
            val validator = schema.newValidator()
            
            val errorHandler = object : org.xml.sax.ErrorHandler {
                val errors = mutableListOf<String>()
                
                override fun warning(exception: org.xml.sax.SAXParseException) {
                    errors.add("Warning: ${exception.message}")
                }
                
                override fun error(exception: org.xml.sax.SAXParseException) {
                    errors.add("Error: ${exception.message}")
                }
                
                override fun fatalError(exception: org.xml.sax.SAXParseException) {
                    errors.add("Fatal: ${exception.message}")
                }
            }
            
            validator.errorHandler = errorHandler
            validator.validate(javax.xml.transform.stream.StreamSource(
                java.io.ByteArrayInputStream(content)
            ))
            
            if (errorHandler.errors.isEmpty()) {
                SchemaValidationResult(true)
            } else {
                SchemaValidationResult(false, errorHandler.errors)
            }
        } catch (e: Exception) {
            SchemaValidationResult(false, listOf(e.message ?: "Unknown validation error"))
        }
    }
    
    companion object {
        fun fromFile(schemaFile: File): XmlSchemaValidator {
            return XmlSchemaValidator(schemaFile.readText())
        }
        
        fun fromString(schemaXml: String): XmlSchemaValidator {
            return XmlSchemaValidator(schemaXml)
        }
    }
}

fun matchesJsonSchema(schemaValidator: JsonSchemaValidator): Matcher<ByteArray> = object : Matcher<ByteArray> {
    override fun matches(actual: ByteArray): MatchResult {
        val result = schemaValidator.validate(actual)
        return if (result.valid) {
            MatchResult.success("Content matches JSON schema")
        } else {
            MatchResult.failure(
                "Content does not match JSON schema. Errors:\n${result.errors.joinToString("\n")}",
                "valid schema",
                result.errors
            )
        }
    }
}

fun matchesXmlSchema(schemaValidator: XmlSchemaValidator): Matcher<ByteArray> = object : Matcher<ByteArray> {
    override fun matches(actual: ByteArray): MatchResult {
        val result = schemaValidator.validate(actual)
        return if (result.valid) {
            MatchResult.success("Content matches XML schema")
        } else {
            MatchResult.failure(
                "Content does not match XML schema. Errors:\n${result.errors.joinToString("\n")}",
                "valid schema",
                result.errors
            )
        }
    }
}
