package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import kotlin.reflect.KClass

class JsonContentHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "application/json",
        "application/json; charset=utf-8",
        "application/json; charset=UTF-8",
        "text/json"
    )
    
    private val objectMapper = ObjectMapper().registerKotlinModule()
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray()
            is ByteArray -> data
            else -> objectMapper.writeValueAsBytes(data)
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        if (targetType == String::class) {
            @Suppress("UNCHECKED_CAST")
            return bytes.decodeToString() as T
        }
        return objectMapper.readValue(bytes, targetType.java)
    }
}
