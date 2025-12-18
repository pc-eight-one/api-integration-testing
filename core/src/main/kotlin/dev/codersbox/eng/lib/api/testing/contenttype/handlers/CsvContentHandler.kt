package dev.codersbox.eng.lib.api.testing.contenttype.handlers

import dev.codersbox.eng.lib.api.testing.contenttype.ContentTypeHandler
import kotlin.reflect.KClass

class CsvContentHandler : ContentTypeHandler {
    override val supportedMediaTypes = listOf(
        "text/csv",
        "application/csv",
        "text/csv; charset=utf-8"
    )
    
    override fun serialize(data: Any): ByteArray {
        return when (data) {
            is String -> data.toByteArray()
            is ByteArray -> data
            is List<*> -> {
                if (data.isEmpty()) return ByteArray(0)
                
                val firstItem = data.first()
                if (firstItem is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val rows = data as List<Map<String, Any?>>
                    convertMapsToCsv(rows)
                } else {
                    data.joinToString("\n").toByteArray()
                }
            }
            else -> data.toString().toByteArray()
        }
    }
    
    override fun <T : Any> deserialize(bytes: ByteArray, targetType: KClass<T>): T {
        val csvString = bytes.decodeToString()
        
        @Suppress("UNCHECKED_CAST")
        return when (targetType) {
            String::class -> csvString as T
            List::class -> parseCsvToListOfMaps(csvString) as T
            else -> csvString as T
        }
    }
    
    private fun convertMapsToCsv(rows: List<Map<String, Any?>>): ByteArray {
        if (rows.isEmpty()) return ByteArray(0)
        
        val headers = rows.first().keys
        val headerLine = headers.joinToString(",") { escapeCSV(it) }
        
        val dataLines = rows.map { row ->
            headers.joinToString(",") { header ->
                escapeCSV(row[header]?.toString() ?: "")
            }
        }
        
        return (listOf(headerLine) + dataLines).joinToString("\n").toByteArray()
    }
    
    private fun parseCsvToListOfMaps(csv: String): List<Map<String, String>> {
        val lines = csv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()
        
        val headers = lines.first().split(",").map { it.trim() }
        return lines.drop(1).map { line ->
            val values = line.split(",").map { it.trim() }
            headers.zip(values).toMap()
        }
    }
    
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
