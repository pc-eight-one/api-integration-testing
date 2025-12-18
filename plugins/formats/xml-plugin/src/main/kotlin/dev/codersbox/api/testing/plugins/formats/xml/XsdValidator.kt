package dev.codersbox.api.testing.plugins.formats.xml

import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

class XsdValidator {
    
    fun validate(xml: String, xsdPath: String): ValidationResult {
        return validate(xml.toByteArray(), File(xsdPath).inputStream())
    }

    fun validate(xml: ByteArray, xsdPath: String): ValidationResult {
        return validate(xml, File(xsdPath).inputStream())
    }

    fun validate(xml: String, xsdStream: InputStream): ValidationResult {
        return validate(xml.toByteArray(), xsdStream)
    }

    fun validate(xml: ByteArray, xsdStream: InputStream): ValidationResult {
        return try {
            val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            val schema = schemaFactory.newSchema(StreamSource(xsdStream))
            val validator = schema.newValidator()
            
            validator.validate(StreamSource(ByteArrayInputStream(xml)))
            
            ValidationResult(isValid = true, errors = emptyList())
        } catch (e: SAXException) {
            ValidationResult(isValid = false, errors = listOf(e.message ?: "Unknown validation error"))
        } catch (e: Exception) {
            ValidationResult(isValid = false, errors = listOf("Validation failed: ${e.message}"))
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) {
    fun throwIfInvalid() {
        if (!isValid) {
            throw XmlValidationException("XML validation failed: ${errors.joinToString(", ")}")
        }
    }
}

class XmlValidationException(message: String) : Exception(message)
