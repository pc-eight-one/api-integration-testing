package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import kotlin.reflect.KClass

class YamlContentHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "application/yaml",
        "application/x-yaml",
        "text/yaml",
        "text/x-yaml"
    )
    
    private val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray()
            is ByteArray -> data
            else -> yamlMapper.writeValueAsBytes(data)
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        if (targetType == String::class) {
            @Suppress("UNCHECKED_CAST")
            return bytes.decodeToString() as T
        }
        return yamlMapper.readValue(bytes, targetType.java)
    }
}
