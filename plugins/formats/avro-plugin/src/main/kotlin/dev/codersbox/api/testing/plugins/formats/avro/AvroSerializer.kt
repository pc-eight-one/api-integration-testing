package dev.codersbox.api.testing.plugins.formats.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.specific.SpecificRecordBase
import java.io.ByteArrayOutputStream

class AvroSerializer {
    
    fun serialize(data: Any): ByteArray {
        return when (data) {
            is ByteArray -> data
            is GenericRecord -> serializeGenericRecord(data)
            is SpecificRecordBase -> serializeSpecificRecord(data)
            else -> throw IllegalArgumentException(
                "Avro serialization requires GenericRecord or SpecificRecordBase, got ${data::class.java}"
            )
        }
    }

    fun serializeWithSchema(data: GenericRecord, schema: Schema): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(outputStream, null)
        val writer = GenericDatumWriter<GenericRecord>(schema)
        
        writer.write(data, encoder)
        encoder.flush()
        
        return outputStream.toByteArray()
    }

    private fun serializeGenericRecord(record: GenericRecord): ByteArray {
        return serializeWithSchema(record, record.schema)
    }

    private fun serializeSpecificRecord(record: SpecificRecordBase): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(outputStream, null)
        val writer = SpecificDatumWriter<SpecificRecordBase>(record.schema)
        
        writer.write(record, encoder)
        encoder.flush()
        
        return outputStream.toByteArray()
    }

    fun serializeToJson(data: GenericRecord): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().jsonEncoder(data.schema, outputStream)
        val writer = GenericDatumWriter<GenericRecord>(data.schema)
        
        writer.write(data, encoder)
        encoder.flush()
        
        return outputStream.toByteArray()
    }
}
