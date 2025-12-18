package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import kotlin.reflect.KClass

class TextContentHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "text/plain",
        "text/html",
        "text/plain; charset=utf-8",
        "text/html; charset=utf-8"
    )
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray()
            is ByteArray -> data
            else -> data.toString().toByteArray()
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (targetType) {
            String::class -> bytes.decodeToString() as T
            ByteArray::class -> bytes as T
            else -> bytes.decodeToString() as T
        }
    }
}
