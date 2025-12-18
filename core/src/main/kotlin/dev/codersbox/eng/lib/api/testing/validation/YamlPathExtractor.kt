package dev.codersbox.eng.lib.api.testing.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.jayway.jsonpath.JsonPath

class YamlPathExtractor : PathExtractor {
    override val supportedContentTypes = listOf(
        "application/yaml",
        "application/x-yaml",
        "text/yaml"
    )
    
    private val yamlMapper = ObjectMapper(YAMLFactory())
    
    override fun extract(content: ByteArray, path: String): Any? {
        return try {
            // Convert YAML to JSON, then use JsonPath
            val yamlObj = yamlMapper.readValue(content, Any::class.java)
            val jsonString = ObjectMapper().writeValueAsString(yamlObj)
            JsonPath.parse(jsonString).read<Any>(path)
        } catch (e: Exception) {
            null
        }
    }
    
    override fun extractAll(content: ByteArray, path: String): List<Any?> {
        return try {
            val yamlObj = yamlMapper.readValue(content, Any::class.java)
            val jsonString = ObjectMapper().writeValueAsString(yamlObj)
            val result = JsonPath.parse(jsonString).read<Any>(path)
            when (result) {
                is List<*> -> result
                else -> listOf(result)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun canHandle(contentType: String): Boolean {
        return supportedContentTypes.any { contentType.contains(it, ignoreCase = true) }
    }
}
