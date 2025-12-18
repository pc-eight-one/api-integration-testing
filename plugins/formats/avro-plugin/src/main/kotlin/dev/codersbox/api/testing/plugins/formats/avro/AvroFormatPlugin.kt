package dev.codersbox.api.testing.plugins.formats.avro

import dev.codersbox.api.testing.core.plugin.FormatPlugin
import dev.codersbox.api.testing.core.plugin.PluginContext
import dev.codersbox.api.testing.core.plugin.PluginMetadata
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

class AvroFormatPlugin : FormatPlugin {
    override val metadata = PluginMetadata(
        id = "avro",
        name = "Avro Format Plugin",
        version = "1.0.0",
        description = "Apache Avro binary format serialization/deserialization with schema registry support"
    )

    override val contentType = "application/avro"
    override val supportedContentTypes = setOf(
        "application/avro",
        "application/vnd.apache.avro+json",
        "application/vnd.apache.avro+binary"
    )

    private lateinit var serializer: AvroSerializer
    private lateinit var deserializer: AvroDeserializer
    private var schemaRegistry: SchemaRegistry? = null

    override fun initialize(context: PluginContext) {
        serializer = AvroSerializer()
        deserializer = AvroDeserializer()
        
        // Initialize schema registry if configured
        context.getProperty("avro.schema.registry.url")?.let { url ->
            schemaRegistry = SchemaRegistry(url)
        }
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

    fun serializeWithSchema(data: GenericRecord, schema: Schema): ByteArray {
        return serializer.serializeWithSchema(data, schema)
    }

    fun deserializeWithSchema(data: ByteArray, schema: Schema): GenericRecord {
        return deserializer.deserializeWithSchema(data, schema)
    }

    fun getSchemaRegistry(): SchemaRegistry? = schemaRegistry
}
