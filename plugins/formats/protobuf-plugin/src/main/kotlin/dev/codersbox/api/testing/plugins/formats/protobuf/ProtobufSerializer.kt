package dev.codersbox.api.testing.plugins.formats.protobuf

import com.google.protobuf.Message

class ProtobufSerializer {
    
    fun serialize(data: Any): ByteArray {
        return when (data) {
            is Message -> data.toByteArray()
            is ByteArray -> data
            else -> throw IllegalArgumentException(
                "Protobuf serialization requires a com.google.protobuf.Message instance, got ${data::class.java}"
            )
        }
    }

    fun serializeToDelimited(messages: List<Message>): ByteArray {
        return messages.flatMap { it.toByteArray().toList() }.toByteArray()
    }
}
