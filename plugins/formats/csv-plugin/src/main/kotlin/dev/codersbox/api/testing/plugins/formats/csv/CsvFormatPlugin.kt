package dev.codersbox.api.testing.plugins.formats.csv

import dev.codersbox.api.testing.core.plugin.FormatPlugin
import dev.codersbox.api.testing.core.plugin.PluginContext
import dev.codersbox.api.testing.core.plugin.PluginMetadata

class CsvFormatPlugin : FormatPlugin {
    override val metadata = PluginMetadata(
        id = "csv",
        name = "CSV Format Plugin",
        version = "1.0.0",
        description = "CSV format serialization/deserialization for bulk data APIs"
    )

    override val contentType = "text/csv"
    override val supportedContentTypes = setOf(
        "text/csv",
        "application/csv",
        "text/comma-separated-values"
    )

    private lateinit var serializer: CsvSerializer
    private lateinit var deserializer: CsvDeserializer

    override fun initialize(context: PluginContext) {
        serializer = CsvSerializer()
        deserializer = CsvDeserializer()
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
