package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import dev.codersbox.eng.lib.cli.reporting.*
import dev.codersbox.eng.lib.cli.execution.ExecutionResult
import java.io.File

class ReportCommand : CliktCommand(
    name = "report",
    help = "Generate test reports from execution results"
) {
    private val inputFile by option("-i", "--input", help = "Input results file (JSON)")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .required()

    private val outputFile by option("-o", "--output", help = "Output report file")
        .file(canBeDir = false)
        .required()

    private val format by option("-f", "--format", help = "Report format")
        .choice("html", "json", "junit", ignoreCase = true)
        .default("html")

    override fun run() {
        echo("Generating $format report from ${inputFile.absolutePath}...")

        // Read execution results from input file
        val results = readResults(inputFile)

        // Generate report
        val generator = when (format.lowercase()) {
            "html" -> HtmlReportGenerator()
            "json" -> JsonReportGenerator()
            "junit" -> JunitXmlReportGenerator()
            else -> error("Unsupported format: $format")
        }

        generator.generate(results, outputFile)

        echo("✓ Report generated: ${outputFile.absolutePath}", err = false)
        
        // Print summary
        val total = results.size
        val passed = results.count { it.success }
        val failed = results.count { !it.success && it.error != null }
        
        echo("")
        echo("Summary:")
        echo("  Total:  $total")
        echo("  Passed: $passed")
        echo("  Failed: $failed")
        echo("  Rate:   ${"%.1f".format(if (total > 0) passed * 100.0 / total else 0.0)}%")
    }

    private fun readResults(file: File): List<ExecutionResult> {
        // Parse JSON results file
        val json = file.readText()
        // Simple JSON parsing (in real scenario, use kotlinx.serialization)
        return parseJsonResults(json)
    }

    private fun parseJsonResults(json: String): List<ExecutionResult> {
        // Simplified JSON parsing - in production use proper JSON library
        val results = mutableListOf<ExecutionResult>()
        
        // Extract results array from JSON
        val resultsStart = json.indexOf("\"results\": [")
        if (resultsStart == -1) return emptyList()
        
        val resultsEnd = json.lastIndexOf("]")
        val resultsJson = json.substring(resultsStart + 12, resultsEnd)
        
        // Parse each result object (simplified)
        val resultObjects = resultsJson.split("    {").filter { it.isNotBlank() }
        
        resultObjects.forEach { obj ->
            val scenario = extractJsonValue(obj, "scenario")
            val step = extractJsonValue(obj, "step")
            val success = extractJsonValue(obj, "success") == "true"
            val duration = extractJsonValue(obj, "duration").toLongOrNull() ?: 0L
            val error = if (obj.contains("\"error\"")) extractJsonValue(obj, "error") else null
            
            results.add(ExecutionResult(scenario, step, success, duration, error))
        }
        
        return results
    }

    private fun extractJsonValue(json: String, key: String): String {
        val pattern = "\"$key\": \"([^\"]*)\""
        val regex = Regex(pattern)
        val match = regex.find(json)
        return match?.groupValues?.get(1) ?: ""
    }
}
