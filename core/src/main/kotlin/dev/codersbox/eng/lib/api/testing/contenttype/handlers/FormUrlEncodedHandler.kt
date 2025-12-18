package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import java.net.URLEncoder
import java.net.URLDecoder
import kotlin.reflect.KClass

class FormUrlEncodedHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "application/x-www-form-urlencoded",
        "application/x-www-form-urlencoded; charset=utf-8"
    )
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray()
            is ByteArray -> data
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val params = data as Map<String, Any?>
                params.entries.joinToString("&") { (key, value) ->
                    val encodedKey = URLEncoder.encode(key, "UTF-8")
                    val encodedValue = URLEncoder.encode(value?.toString() ?: "", "UTF-8")
                    "$encodedKey=$encodedValue"
                }.toByteArray()
            }
            else -> data.toString().toByteArray()
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        val formString = bytes.decodeToString()
        
        @Suppress("UNCHECKED_CAST")
        return when (targetType) {
            String::class -> formString as T
            Map::class -> parseFormData(formString) as T
            else -> formString as T
        }
    }
    
    private fun parseFormData(formString: String): Map<String, String> {
        if (formString.isBlank()) return emptyMap()
        
        return formString.split("&")
            .map { it.split("=", limit = 2) }
            .filter { it.size == 2 }
            .associate { (key, value) ->
                URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
            }
    }
}
