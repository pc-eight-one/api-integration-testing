package dev.codersbox.eng.lib.cli.generation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class TestCaseGenerator(
    private val dataGenerator: TestDataGenerator = TestDataGenerator()
) {
    private val jsonMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun generateTestSuite(
        spec: OpenApiSpec,
        outputDir: File,
        packageName: String = "generated.tests",
        baseUrl: String = "http://localhost:8080"
    ) {
        val parser = OpenApiParser()
        val endpoints = parser.extractEndpoints(spec)

        // Group endpoints by tags or path prefix
        val groupedEndpoints = groupEndpoints(endpoints)

        groupedEndpoints.forEach { (groupName, endpoints) ->
            val testClass = generateTestClass(
                groupName = groupName,
                endpoints = endpoints,
                packageName = packageName,
                baseUrl = baseUrl,
                spec = spec
            )

            val fileName = "${groupName.toCamelCase()}Test.kt"
            val file = File(outputDir, fileName)
            file.parentFile.mkdirs()
            file.writeText(testClass)
            println("Generated: ${file.absolutePath}")
        }
    }

    private fun groupEndpoints(endpoints: List<EndpointInfo>): Map<String, List<EndpointInfo>> {
        return endpoints.groupBy { endpoint ->
            // Use first tag if available, otherwise use path segment
            endpoint.operation.tags?.firstOrNull() 
                ?: endpoint.path.split("/").getOrNull(1) 
                ?: "Default"
        }
    }

    private fun generateTestClass(
        groupName: String,
        endpoints: List<EndpointInfo>,
        packageName: String,
        baseUrl: String,
        spec: OpenApiSpec
    ): String {
        val className = "${groupName.toCamelCase()}Test"
        val scenarios = endpoints.mapIndexed { index, endpoint ->
            generateScenario(endpoint, index, spec)
        }.joinToString("\n\n")

        return """
package $packageName

import dev.codersbox.eng.lib.api.testing.dsl.*
import io.kotest.core.spec.style.FunSpec

class $className : FunSpec({
    val suite = apiTestSuite("${groupName.capitalize()} API Tests") {
        baseUrl("$baseUrl")
        
        defaultHeaders {
            "Content-Type" to "application/json"
            "Accept" to "application/json"
        }

$scenarios
    }
})
        """.trimIndent()
    }

    private fun generateScenario(endpoint: EndpointInfo, index: Int, spec: OpenApiSpec): String {
        val scenarioName = endpoint.operation.summary 
            ?: endpoint.operation.operationId 
            ?: "${endpoint.method} ${endpoint.path}"
        
        val pathParams = endpoint.operation.parameters?.filter { it.`in` == "path" } ?: emptyList()
        val queryParams = endpoint.operation.parameters?.filter { it.`in` == "query" } ?: emptyList()
        val headerParams = endpoint.operation.parameters?.filter { it.`in` == "header" } ?: emptyList()
        
        val pathWithParams = generatePathWithParams(endpoint.path, pathParams)
        val requestBody = generateRequestBody(endpoint.operation.requestBody, spec)
        val validations = generateValidations(endpoint.operation.responses)
        
        val indent = "        "
        return """
${indent}scenario("$scenarioName") {
${indent}    step("${endpoint.method} ${endpoint.path}") {
${indent}        ${generateRequest(endpoint.method, pathWithParams, queryParams, headerParams, requestBody)}
${indent}    }
${indent}}
        """.trimIndent()
    }

    private fun generateRequest(
        method: String,
        path: String,
        queryParams: List<Parameter>,
        headerParams: List<Parameter>,
        requestBody: String?
    ): String {
        val methodCall = when (method.uppercase()) {
            "GET" -> "get"
            "POST" -> "post"
            "PUT" -> "put"
            "DELETE" -> "delete"
            "PATCH" -> "patch"
            else -> "get"
        }

        val parts = mutableListOf<String>()
        
        // Add query parameters
        if (queryParams.isNotEmpty()) {
            val queryString = queryParams.joinToString(", ") { param ->
                val value = dataGenerator.generateForSchema(param.schema, OpenApiSpec("3.0.0", ApiInfo("", ""), emptyMap()))
                "\"${param.name}\" to \"$value\""
            }
            parts.add("queryParams($queryString)")
        }

        // Add headers
        if (headerParams.isNotEmpty()) {
            val headersString = headerParams.joinToString(", ") { param ->
                val value = dataGenerator.generateForSchema(param.schema, OpenApiSpec("3.0.0", ApiInfo("", ""), emptyMap()))
                "\"${param.name}\" to \"$value\""
            }
            parts.add("headers($headersString)")
        }

        // Add request body
        requestBody?.let {
            parts.add("body($it)")
        }

        // Add validations
        parts.add("""expect {
                status(200)
                jsonPath("$") exists()
            }""")

        val paramsBlock = if (parts.isNotEmpty()) {
            " {\n            " + parts.joinToString("\n            ") + "\n        }"
        } else {
            ""
        }

        return "$methodCall(\"$path\")$paramsBlock"
    }

    private fun generatePathWithParams(path: String, pathParams: List<Parameter>): String {
        var result = path
        pathParams.forEach { param ->
            val value = when (param.schema.type?.lowercase()) {
                "integer" -> "1"
                "string" -> when (param.schema.format?.lowercase()) {
                    "uuid" -> "123e4567-e89b-12d3-a456-426614174000"
                    else -> "test-${param.name}"
                }
                else -> "test-value"
            }
            result = result.replace("{${param.name}}", value)
        }
        return result
    }

    private fun generateRequestBody(requestBody: RequestBody?, spec: OpenApiSpec): String? {
        requestBody ?: return null
        
        val jsonContent = requestBody.content["application/json"] ?: return null
        val schema = jsonContent.schema
        
        val data = dataGenerator.generateForSchema(schema, spec)
        return try {
            "\"\"\"${jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data)}\"\"\""
        } catch (e: Exception) {
            null
        }
    }

    private fun generateValidations(responses: Map<String, Response>?): String {
        responses ?: return """
            status(200)
        """.trimIndent()
        
        val successResponse = responses["200"] ?: responses["201"] ?: responses["204"]
        successResponse ?: return "status(200)"
        
        return """
            status(${responses.keys.first()})
            jsonPath("$") exists()
        """.trimIndent()
    }

    private fun String.toCamelCase(): String {
        return split("-", "_", " ")
            .joinToString("") { it.capitalize() }
            .replaceFirstChar { it.uppercase() }
    }

    private fun String.capitalize(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
