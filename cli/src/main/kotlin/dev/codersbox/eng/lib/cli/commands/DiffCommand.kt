package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File

class DiffCommand : CliktCommand(
    name = "diff",
    help = "Compare API responses or test results between two runs"
) {
    private val baseline by argument(help = "Baseline file path")
    private val current by argument(help = "Current file path")
    
    private val format by option("-f", "--format", help = "Output format (text, json, html)")
        .default("text")
    
    private val ignoreFields by option("--ignore", help = "Comma-separated fields to ignore in comparison")
    
    private val output by option("-o", "--output", help = "Output file path")
    
    private val verbose by option("-v", "--verbose", help = "Show detailed differences")
        .flag(default = false)
    
    override fun run() {
        val baselineFile = File(baseline)
        val currentFile = File(current)
        
        if (!baselineFile.exists()) {
            echo("Error: Baseline file not found: $baseline", err = true)
            return
        }
        
        if (!currentFile.exists()) {
            echo("Error: Current file not found: $current", err = true)
            return
        }
        
        echo("Comparing files...")
        echo("Baseline: $baseline")
        echo("Current:  $current")
        echo()
        
        val baselineContent = baselineFile.readText()
        val currentContent = currentFile.readText()
        
        val ignoredFields = ignoreFields?.split(",")?.map { it.trim() } ?: emptyList()
        
        val differences = when {
            baselineContent == currentContent -> {
                echo("✅ No differences found")
                return
            }
            else -> computeDifferences(baselineContent, currentContent, ignoredFields)
        }
        
        val diffReport = when (format) {
            "json" -> generateJsonDiff(differences)
            "html" -> generateHtmlDiff(differences)
            else -> generateTextDiff(differences)
        }
        
        if (output != null) {
            File(output!!).writeText(diffReport)
            echo("Diff report saved to: $output")
        } else {
            echo(diffReport)
        }
    }
    
    private fun computeDifferences(
        baseline: String,
        current: String,
        ignoredFields: List<String>
    ): List<Difference> {
        val differences = mutableListOf<Difference>()
        
        val baselineLines = baseline.lines()
        val currentLines = current.lines()
        
        val maxLines = maxOf(baselineLines.size, currentLines.size)
        
        for (i in 0 until maxLines) {
            val baselineLine = baselineLines.getOrNull(i)
            val currentLine = currentLines.getOrNull(i)
            
            when {
                baselineLine == null -> differences.add(
                    Difference(DiffType.ADDED, i + 1, null, currentLine)
                )
                currentLine == null -> differences.add(
                    Difference(DiffType.REMOVED, i + 1, baselineLine, null)
                )
                baselineLine != currentLine -> {
                    // Check if line should be ignored
                    val shouldIgnore = ignoredFields.any { field ->
                        baselineLine.contains(field) || currentLine.contains(field)
                    }
                    
                    if (!shouldIgnore) {
                        differences.add(
                            Difference(DiffType.MODIFIED, i + 1, baselineLine, currentLine)
                        )
                    }
                }
            }
        }
        
        return differences
    }
    
    private fun generateTextDiff(differences: List<Difference>): String {
        return buildString {
            appendLine("Differences found: ${differences.size}")
            appendLine("=".repeat(60))
            appendLine()
            
            differences.forEach { diff ->
                when (diff.type) {
                    DiffType.ADDED -> {
                        appendLine("+ Line ${diff.lineNumber}: ${diff.current}")
                    }
                    DiffType.REMOVED -> {
                        appendLine("- Line ${diff.lineNumber}: ${diff.baseline}")
                    }
                    DiffType.MODIFIED -> {
                        appendLine("~ Line ${diff.lineNumber}:")
                        appendLine("  - ${diff.baseline}")
                        appendLine("  + ${diff.current}")
                    }
                }
                appendLine()
            }
        }
    }
    
    private fun generateJsonDiff(differences: List<Difference>): String {
        return buildString {
            appendLine("{")
            appendLine("  \"differences\": [")
            differences.forEachIndexed { index, diff ->
                appendLine("    {")
                appendLine("      \"type\": \"${diff.type}\",")
                appendLine("      \"line\": ${diff.lineNumber},")
                if (diff.baseline != null) appendLine("      \"baseline\": \"${escapeJson(diff.baseline)}\",")
                if (diff.current != null) appendLine("      \"current\": \"${escapeJson(diff.current)}\"")
                append("    }")
                if (index < differences.size - 1) appendLine(",")
                else appendLine()
            }
            appendLine("  ]")
            appendLine("}")
        }
    }
    
    private fun generateHtmlDiff(differences: List<Difference>): String {
        return buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head>")
            appendLine("<title>API Test Diff Report</title>")
            appendLine("<style>")
            appendLine("body { font-family: monospace; margin: 20px; }")
            appendLine(".added { background-color: #d4edda; }")
            appendLine(".removed { background-color: #f8d7da; }")
            appendLine(".modified { background-color: #fff3cd; }")
            appendLine(".line { padding: 5px; margin: 2px 0; }")
            appendLine("</style>")
            appendLine("</head><body>")
            appendLine("<h1>Diff Report</h1>")
            appendLine("<p>Total differences: ${differences.size}</p>")
            
            differences.forEach { diff ->
                when (diff.type) {
                    DiffType.ADDED -> {
                        appendLine("<div class='line added'>+ Line ${diff.lineNumber}: ${escapeHtml(diff.current ?: "")}</div>")
                    }
                    DiffType.REMOVED -> {
                        appendLine("<div class='line removed'>- Line ${diff.lineNumber}: ${escapeHtml(diff.baseline ?: "")}</div>")
                    }
                    DiffType.MODIFIED -> {
                        appendLine("<div class='line modified'>")
                        appendLine("  ~ Line ${diff.lineNumber}:<br/>")
                        appendLine("  - ${escapeHtml(diff.baseline ?: "")}<br/>")
                        appendLine("  + ${escapeHtml(diff.current ?: "")}")
                        appendLine("</div>")
                    }
                }
            }
            
            appendLine("</body></html>")
        }
    }
    
    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    }
    
    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    }
    
    data class Difference(
        val type: DiffType,
        val lineNumber: Int,
        val baseline: String?,
        val current: String?
    )
    
    enum class DiffType {
        ADDED, REMOVED, MODIFIED
    }
}
