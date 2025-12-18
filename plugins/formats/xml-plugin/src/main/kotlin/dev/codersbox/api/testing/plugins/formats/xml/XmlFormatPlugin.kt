package dev.codersbox.api.testing.plugins.formats.xml

import dev.codersbox.api.testing.core.plugin.FormatPlugin
import dev.codersbox.api.testing.core.plugin.PluginContext
import dev.codersbox.api.testing.core.plugin.PluginMetadata

class XmlFormatPlugin : FormatPlugin {
    override val metadata = PluginMetadata(
        id = "xml",
        name = "XML Format Plugin",
        version = "1.0.0",
        description = "XML format serialization/deserialization with XPath support and XSD validation"
    )

    override val contentType = "application/xml"
    override val supportedContentTypes = setOf(
        "application/xml",
        "text/xml",
        "application/xhtml+xml"
    )

    private lateinit var serializer: XmlSerializer
    private lateinit var deserializer: XmlDeserializer

    override fun initialize(context: PluginContext) {
        serializer = XmlSerializer()
        deserializer = XmlDeserializer()
    }

    override fun serialize(data: Any): ByteArray {
        return serializer.serialize(data)
    }

    override fun deserialize(data: ByteArray, targetType: Class<*>): Any {
        return deserializer.deserialize(data, targetType)
    }

    override fun isSupported(contentType: String): Boolean {
        return supportedContentTypes.any { contentType.contains(it, ignoreCase = true) }
    }
}
