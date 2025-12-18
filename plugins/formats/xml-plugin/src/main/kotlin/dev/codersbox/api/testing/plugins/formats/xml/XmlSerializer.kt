package dev.codersbox.api.testing.plugins.formats.xml

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class XmlSerializer {
    private val xmlMapper = XmlMapper().registerKotlinModule()

    fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray(Charsets.UTF_8)
            is ByteArray -> data
            else -> xmlMapper.writeValueAsBytes(data)
        }
    }

    fun serializeToString(data: Any): String {
        return when (data) {
            is String -> data
            else -> xmlMapper.writeValueAsString(data)
        }
    }
}
