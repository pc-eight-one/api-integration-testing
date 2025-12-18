package dev.codersbox.api.testing.plugins.formats.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificRecordBase
import java.io.ByteArrayInputStream

class AvroDeserializer {
    
    fun deserialize(data: ByteArray, targetType: Class<*>): Any {
        if (GenericRecord::class.java.isAssignableFrom(targetType)) {
            throw IllegalArgumentException(
                "GenericRecord deserialization requires a schema. Use deserializeWithSchema() instead"
            )
        }

        if (!SpecificRecordBase::class.java.isAssignableFrom(targetType)) {
            throw IllegalArgumentException(
                "Target type must be a SpecificRecordBase, got $targetType"
            )
        }

        return try {
            val schema = targetType.getDeclaredField("SCHEMA$").get(null) as Schema
            deserializeSpecificRecord(data, schema, targetType)
        } catch (e: Exception) {
            throw AvroDeserializationException(
                "Failed to deserialize Avro record of type ${targetType.name}", e
            )
        }
    }

    fun deserializeWithSchema(data: ByteArray, schema: Schema): GenericRecord {
        val inputStream = ByteArrayInputStream(data)
        val decoder = DecoderFactory.get().binaryDecoder(inputStream, null)
        val reader = GenericDatumReader<GenericRecord>(schema)
        
        return reader.read(null, decoder)
    }

    private fun deserializeSpecificRecord(
        data: ByteArray,
        schema: Schema,
        targetType: Class<*>
    ): SpecificRecordBase {
        val inputStream = ByteArrayInputStream(data)
        val decoder = DecoderFactory.get().binaryDecoder(inputStream, null)
        val reader = SpecificDatumReader<SpecificRecordBase>(schema)
        
        return reader.read(null, decoder)
    }

    fun deserializeFromJson(data: ByteArray, schema: Schema): GenericRecord {
        val inputStream = ByteArrayInputStream(data)
        val decoder = DecoderFactory.get().jsonDecoder(schema, inputStream)
        val reader = GenericDatumReader<GenericRecord>(schema)
        
        return reader.read(null, decoder)
    }
}

class AvroDeserializationException(message: String, cause: Throwable? = null) : Exception(message, cause)
