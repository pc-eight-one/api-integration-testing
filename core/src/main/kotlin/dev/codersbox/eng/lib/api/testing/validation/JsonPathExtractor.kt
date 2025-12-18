package dev.codersbox.eng.lib.api.testing.validation

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException

class JsonPathExtractor : PathExtractor {
    override val supportedContentTypes = listOf(
        "application/json",
        "application/vnd.api+json",
        "application/problem+json",
        "text/json"
    )
    
    override fun extract(content: ByteArray, path: String): Any? {
        return try {
            JsonPath.parse(String(content)).read<Any>(path)
        } catch (e: PathNotFoundException) {
            null
        }
    }
    
    override fun extractAll(content: ByteArray, path: String): List<Any?> {
        return try {
            val result = JsonPath.parse(String(content)).read<Any>(path)
            when (result) {
                is List<*> -> result
                else -> listOf(result)
            }
        } catch (e: PathNotFoundException) {
            emptyList()
        }
    }
    
    override fun canHandle(contentType: String): Boolean {
        return supportedContentTypes.any { contentType.contains(it, ignoreCase = true) }
    }
}
