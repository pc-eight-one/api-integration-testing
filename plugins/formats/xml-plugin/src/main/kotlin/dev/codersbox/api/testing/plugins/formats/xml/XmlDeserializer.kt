package dev.codersbox.api.testing.plugins.formats.xml

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.dom4j.Document
import org.dom4j.DocumentHelper

class XmlDeserializer {
    private val xmlMapper = XmlMapper().registerKotlinModule()

    fun deserialize(data: ByteArray, targetType: Class<*>): Any {
        val xmlString = String(data, Charsets.UTF_8)
        
        return when (targetType) {
            String::class.java -> xmlString
            Document::class.java -> DocumentHelper.parseText(xmlString)
            else -> xmlMapper.readValue(data, targetType)
        }
    }

    fun parseDocument(xml: String): Document {
        return DocumentHelper.parseText(xml)
    }

    fun parseDocument(xml: ByteArray): Document {
        return parseDocument(String(xml, Charsets.UTF_8))
    }
}
