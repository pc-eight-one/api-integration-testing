package dev.codersbox.api.testing.plugins.formats.csv

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

class CsvSerializer {
    private val csvMapper = CsvMapper().registerKotlinModule()

    fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray(Charsets.UTF_8)
            is ByteArray -> data
            is List<*> -> serializeList(data)
            else -> serializeObject(data)
        }
    }

    private fun serializeList(data: List<*>): ByteArray {
        if (data.isEmpty()) {
            return ByteArray(0)
        }

        val firstItem = data.first() ?: return ByteArray(0)
        val schema = csvMapper.schemaFor(firstItem::class.java).withHeader()
        
        return csvMapper.writer(schema).writeValueAsBytes(data)
    }

    private fun serializeObject(data: Any): ByteArray {
        val schema = csvMapper.schemaFor(data::class.java).withHeader()
        return csvMapper.writer(schema).writeValueAsBytes(data)
    }

    fun serializeWithCustomFormat(
        data: List<Map<String, Any>>,
        headers: List<String>,
        delimiter: Char = ',',
        includeHeader: Boolean = true
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = OutputStreamWriter(outputStream, Charsets.UTF_8)
        
        val csvFormat = CSVFormat.Builder.create()
            .setDelimiter(delimiter)
            .setHeader(*headers.toTypedArray())
            .setSkipHeaderRecord(!includeHeader)
            .build()

        CSVPrinter(writer, csvFormat).use { printer ->
            data.forEach { row ->
                printer.printRecord(headers.map { row[it] })
            }
        }

        return outputStream.toByteArray()
    }
}
