package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import kotlin.reflect.KClass

class XmlContentHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "application/xml",
        "text/xml",
        "application/xml; charset=utf-8",
        "text/xml; charset=utf-8"
    )
    
    private val xmlMapper = XmlMapper().registerKotlinModule()
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray()
            is ByteArray -> data
            else -> xmlMapper.writeValueAsBytes(data)
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        if (targetType == String::class) {
            @Suppress("UNCHECKED_CAST")
            return bytes.decodeToString() as T
        }
        return xmlMapper.readValue(bytes, targetType.java)
    }
}
