package dev.codersbox.eng.lib.cli.generation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

data class OpenApiSpec(
    val openapi: String,
    val info: ApiInfo,
    val paths: Map<String, PathItem>,
    val components: Components? = null
)

data class ApiInfo(
    val title: String,
    val version: String,
    val description: String? = null
)

data class PathItem(
    val get: Operation? = null,
    val post: Operation? = null,
    val put: Operation? = null,
    val delete: Operation? = null,
    val patch: Operation? = null
)

data class Operation(
    val summary: String? = null,
    val description: String? = null,
    val operationId: String? = null,
    val parameters: List<Parameter>? = null,
    val requestBody: RequestBody? = null,
    val responses: Map<String, Response>? = null,
    val tags: List<String>? = null
)

data class Parameter(
    val name: String,
    val `in`: String,
    val required: Boolean = false,
    val schema: Schema,
    val description: String? = null
)

data class RequestBody(
    val required: Boolean = false,
    val content: Map<String, MediaType>
)

data class MediaType(
    val schema: Schema
)

data class Response(
    val description: String,
    val content: Map<String, MediaType>? = null
)

data class Schema(
    val type: String? = null,
    val format: String? = null,
    val properties: Map<String, Schema>? = null,
    val required: List<String>? = null,
    val items: Schema? = null,
    val `$ref`: String? = null,
    val enum: List<String>? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val example: Any? = null
)

data class Components(
    val schemas: Map<String, Schema>? = null
)

class OpenApiParser {
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
    private val jsonMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun parse(file: File): OpenApiSpec {
        return when (file.extension.lowercase()) {
            "yaml", "yml" -> yamlMapper.readValue(file, OpenApiSpec::class.java)
            "json" -> jsonMapper.readValue(file, OpenApiSpec::class.java)
            else -> throw IllegalArgumentException("Unsupported file format: ${file.extension}")
        }
    }

    fun extractEndpoints(spec: OpenApiSpec): List<EndpointInfo> {
        val endpoints = mutableListOf<EndpointInfo>()
        
        spec.paths.forEach { (path, pathItem) ->
            listOf(
                "GET" to pathItem.get,
                "POST" to pathItem.post,
                "PUT" to pathItem.put,
                "DELETE" to pathItem.delete,
                "PATCH" to pathItem.patch
            ).forEach { (method, operation) ->
                operation?.let {
                    endpoints.add(
                        EndpointInfo(
                            path = path,
                            method = method,
                            operation = it,
                            spec = spec
                        )
                    )
                }
            }
        }
        
        return endpoints
    }
}

data class EndpointInfo(
    val path: String,
    val method: String,
    val operation: Operation,
    val spec: OpenApiSpec
)
