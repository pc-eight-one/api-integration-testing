package dev.codersbox.api.testing.plugins.formats.protobuf

import com.google.protobuf.Message

class ProtobufDeserializer {
    
    fun deserialize(data: ByteArray, targetType: Class<*>): Any {
        if (!Message::class.java.isAssignableFrom(targetType)) {
            throw IllegalArgumentException(
                "Target type must be a com.google.protobuf.Message, got $targetType"
            )
        }

        return try {
            val parseFromMethod = targetType.getMethod("parseFrom", ByteArray::class.java)
            parseFromMethod.invoke(null, data) as Message
        } catch (e: Exception) {
            throw ProtobufDeserializationException(
                "Failed to deserialize protobuf message of type ${targetType.name}", e
            )
        }
    }

    fun <T : Message> deserialize(data: ByteArray, parser: (ByteArray) -> T): T {
        return try {
            parser(data)
        } catch (e: Exception) {
            throw ProtobufDeserializationException("Failed to deserialize protobuf message", e)
        }
    }
}

class ProtobufDeserializationException(message: String, cause: Throwable? = null) : Exception(message, cause)
