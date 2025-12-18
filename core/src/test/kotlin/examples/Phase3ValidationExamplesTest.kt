package examples

import dev.codersbox.eng.lib.api.testing.validation.*
import io.kotest.core.spec.style.FunSpec

class Phase3ValidationExamplesTest : FunSpec({
    
    context("String Matchers") {
        test("Email validation") {
            val email = "user@example.com"
            val result = isValidEmail().matches(email)
            assert(result.passed) { result.message }
        }
        
        test("URL validation") {
            val url = "https://api.example.com/users"
            val result = isValidUrl().matches(url)
            assert(result.passed) { result.message }
        }
        
        test("UUID validation") {
            val uuid = "550e8400-e29b-41d4-a716-446655440000"
            val result = isValidUUID().matches(uuid)
            assert(result.passed) { result.message }
        }
        
        test("Regex pattern matching") {
            val phoneNumber = "123-456-7890"
            val result = matchesRegex("\\d{3}-\\d{3}-\\d{4}").matches(phoneNumber)
            assert(result.passed) { result.message }
        }
        
        test("String contains") {
            val text = "Hello World"
            val result = contains("World").matches(text)
            assert(result.passed) { result.message }
        }
    }
    
    context("Comparison Matchers") {
        test("Greater than") {
            val result = greaterThan(10).matches(15)
            assert(result.passed) { result.message }
        }
        
        test("Between range") {
            val result = between(10, 20).matches(15)
            assert(result.passed) { result.message }
        }
        
        test("Less than or equal") {
            val result = lessThanOrEqual(100).matches(100)
            assert(result.passed) { result.message }
        }
    }
    
    context("Collection Matchers") {
        test("Collection size") {
            val list = listOf(1, 2, 3, 4, 5)
            val result = hasSize<Int>(5).matches(list)
            assert(result.passed) { result.message }
        }
        
        test("Collection contains element") {
            val list = listOf("apple", "banana", "cherry")
            val result = containsElement("banana").matches(list)
            assert(result.passed) { result.message }
        }
        
        test("Collection not empty") {
            val list = listOf(1, 2, 3)
            val result = isNotEmpty<Int>().matches(list)
            assert(result.passed) { result.message }
        }
    }
    
    context("Path Extractors") {
        test("JSON path extraction") {
            val json = """{"user": {"name": "Alice", "age": 30}}""".toByteArray()
            val extractor = JsonPathExtractor()
            val name = extractor.extract(json, "$.user.name")
            assert(name == "Alice")
        }
        
        test("XML path extraction") {
            val xml = """<user><name>Bob</name><age>25</age></user>""".toByteArray()
            val extractor = XmlPathExtractor()
            val name = extractor.extract(xml, "//user/name")
            assert(name == "Bob")
        }
        
        test("CSV path extraction") {
            val csv = """
                name,age,city
                Alice,30,NYC
                Bob,25,LA
            """.trimIndent().toByteArray()
            val extractor = CsvPathExtractor()
            val name = extractor.extract(csv, "row[0].column[name]")
            assert(name == "Alice")
        }
    }
    
    context("Schema Validation") {
        test("JSON schema validation") {
            val schema = """
                {
                  "type": "object",
                  "properties": {
                    "name": { "type": "string" },
                    "age": { "type": "number" }
                  },
                  "required": ["name", "age"]
                }
            """.trimIndent()
            
            val jsonValidator = JsonSchemaValidator.fromString(schema)
            val validJson = """{"name": "Alice", "age": 30}""".toByteArray()
            val result = jsonValidator.validate(validJson)
            assert(result.valid) { "Schema validation should pass" }
        }
        
        test("JSON schema validation failure") {
            val schema = """
                {
                  "type": "object",
                  "properties": {
                    "name": { "type": "string" },
                    "age": { "type": "number" }
                  },
                  "required": ["name", "age"]
                }
            """.trimIndent()
            
            val jsonValidator = JsonSchemaValidator.fromString(schema)
            val invalidJson = """{"name": "Alice"}""".toByteArray() // missing age
            val result = jsonValidator.validate(invalidJson)
            assert(!result.valid) { "Schema validation should fail" }
            assert(result.errors.isNotEmpty()) { "Should have validation errors" }
        }
    }
    
    context("Path Extractor Registry") {
        test("Auto-detect content type and extract") {
            val json = """{"status": "success", "data": {"id": 123}}""".toByteArray()
            val value = PathExtractorRegistry.extract(json, "$.data.id", "application/json")
            assert(value == 123)
        }
        
        test("Extract all values") {
            val json = """{"items": [1, 2, 3, 4, 5]}""".toByteArray()
            val values = PathExtractorRegistry.extractAll(json, "$.items[*]", "application/json")
            assert(values.size == 5)
        }
    }
})
