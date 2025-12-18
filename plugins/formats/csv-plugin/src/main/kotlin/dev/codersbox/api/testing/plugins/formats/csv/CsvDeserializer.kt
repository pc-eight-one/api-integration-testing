package dev.codersbox.api.testing.plugins.formats.csv

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class CsvDeserializer {
    private val csvMapper = CsvMapper().registerKotlinModule()

    fun deserialize(data: ByteArray, targetType: Class<*>): Any {
        val csvString = String(data, Charsets.UTF_8)
        
        return when {
            targetType == String::class.java -> csvString
            List::class.java.isAssignableFrom(targetType) -> deserializeToList(data)
            else -> deserializeToObject(data, targetType)
        }
    }

    private fun deserializeToList(data: ByteArray): List<Map<String, String>> {
        val reader = InputStreamReader(ByteArrayInputStream(data), Charsets.UTF_8)
        val csvFormat = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .build()

        val records = CSVParser(reader, csvFormat).use { parser ->
            parser.records.map { record ->
                parser.headerNames.associateWith { header ->
                    record.get(header) ?: ""
                }
            }
        }

        return records
    }

    private fun deserializeToObject(data: ByteArray, targetType: Class<*>): Any {
        val schema = CsvSchema.emptySchema().withHeader()
        val reader = csvMapper.readerFor(targetType).with(schema)
        
        return reader.readValue<Any>(data)
    }

    fun deserializeToTypedList(data: ByteArray, itemType: Class<*>): List<*> {
        val schema = CsvSchema.emptySchema().withHeader()
        val reader = csvMapper.readerFor(itemType).with(schema)
        
        return reader.readValues<Any>(data).readAll()
    }

    fun deserializeWithCustomFormat(
        data: ByteArray,
        delimiter: Char = ',',
        hasHeader: Boolean = true,
        headers: List<String>? = null
    ): List<Map<String, String>> {
        val reader = InputStreamReader(ByteArrayInputStream(data), Charsets.UTF_8)
        
        val formatBuilder = CSVFormat.DEFAULT.builder()
            .setDelimiter(delimiter)
            .setSkipHeaderRecord(hasHeader)

        if (headers != null) {
            formatBuilder.setHeader(*headers.toTypedArray())
        } else if (hasHeader) {
            formatBuilder.setHeader()
        }

        val csvFormat = formatBuilder.build()

        val records = CSVParser(reader, csvFormat).use { parser ->
            parser.records.map { record ->
                parser.headerNames.associateWith { header ->
                    record.get(header) ?: ""
                }
            }
        }

        return records
    }
}
