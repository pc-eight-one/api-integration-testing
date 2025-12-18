package dev.codersbox.api.testing.plugins.formats.protobuf

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import dev.codersbox.api.testing.core.plugin.FormatPlugin
import dev.codersbox.api.testing.core.plugin.PluginContext
import dev.codersbox.api.testing.core.plugin.PluginMetadata

class ProtobufFormatPlugin : FormatPlugin {
    override val metadata = PluginMetadata(
        id = "protobuf",
        name = "Protobuf Format Plugin",
        version = "1.0.0",
        description = "Protocol Buffers binary format serialization/deserialization"
    )

    override val contentType = "application/x-protobuf"
    override val supportedContentTypes = setOf(
        "application/x-protobuf",
        "application/protobuf",
        "application/octet-stream"
    )

    private lateinit var serializer: ProtobufSerializer
    private lateinit var deserializer: ProtobufDeserializer

    override fun initialize(context: PluginContext) {
        serializer = ProtobufSerializer()
        deserializer = ProtobufDeserializer()
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

    fun toJson(message: Message): String {
        return JsonFormat.printer()
            .includingDefaultValueFields()
            .print(message)
    }

    fun fromJson(json: String, builder: Message.Builder): Message {
        JsonFormat.parser().merge(json, builder)
        return builder.build()
    }
}
