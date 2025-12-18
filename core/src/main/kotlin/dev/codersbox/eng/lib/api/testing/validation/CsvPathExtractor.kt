package dev.codersbox.eng.lib.api.testing.validation

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.ByteArrayInputStream

class CsvPathExtractor : PathExtractor {
    override val supportedContentTypes = listOf(
        "text/csv",
        "application/csv"
    )
    
    override fun extract(content: ByteArray, path: String): Any? {
        val parser = parseCsv(content)
        val records = parser.records
        
        // Path format: "row[0].column[name]" or "column[name]"
        return when {
            path.startsWith("row[") -> {
                val rowIndex = path.substringAfter("row[").substringBefore("]").toIntOrNull() ?: return null
                val columnName = path.substringAfter("column[").substringBefore("]")
                records.getOrNull(rowIndex)?.get(columnName)
            }
            path.startsWith("column[") -> {
                val columnName = path.substringAfter("column[").substringBefore("]")
                records.firstOrNull()?.get(columnName)
            }
            else -> null
        }
    }
    
    override fun extractAll(content: ByteArray, path: String): List<Any?> {
        val parser = parseCsv(content)
        val records = parser.records
        
        // Path format: "column[name]" returns all values in that column
        return when {
            path.startsWith("column[") -> {
                val columnName = path.substringAfter("column[").substringBefore("]")
                records.mapNotNull { it.get(columnName) }
            }
            else -> emptyList()
        }
    }
    
    override fun canHandle(contentType: String): Boolean {
        return supportedContentTypes.any { contentType.contains(it, ignoreCase = true) }
    }
    
    private fun parseCsv(content: ByteArray): CSVParser {
        return CSVParser.parse(
            ByteArrayInputStream(content),
            Charsets.UTF_8,
            CSVFormat.DEFAULT.withFirstRecordAsHeader()
        )
    }
}
