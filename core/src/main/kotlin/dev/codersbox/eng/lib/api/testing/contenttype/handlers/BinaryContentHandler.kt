package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import kotlin.reflect.KClass

class BinaryContentHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "application/octet-stream",
        "application/binary",
        "image/png",
        "image/jpeg",
        "image/gif",
        "application/pdf"
    )
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is ByteArray -> data
            is String -> data.toByteArray()
            else -> throw IllegalArgumentException("Binary handler requires ByteArray or String, got ${data::class}")
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (targetType) {
            ByteArray::class -> bytes as T
            String::class -> bytes.decodeToString() as T
            else -> bytes as T
        }
    }
}
