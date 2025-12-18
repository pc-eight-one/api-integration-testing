package dev.codersbox.eng.lib.cli.output

import dev.codersbox.eng.lib.cli.execution.ExecutionStatus
import dev.codersbox.eng.lib.cli.execution.ExecutionSummary
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HtmlReporter {
    fun generate(summary: ExecutionSummary, outputDir: String) {
        val dir = File(outputDir)
        dir.mkdirs()

        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang='en'>")
            appendLine("<head>")
            appendLine("    <meta charset='UTF-8'>")
            appendLine("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            appendLine("    <title>API Integration Test Report</title>")
            appendLine("    <style>")
            appendLine(getStyles())
            appendLine("    </style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("    <div class='container'>")
            appendLine("        <h1>API Integration Test Report</h1>")
            appendLine("        <div class='timestamp'>Generated: ${formatTimestamp(System.currentTimeMillis())}</div>")
            
            appendLine("        <div class='summary'>")
            appendLine("            <div class='summary-card total'>")
            appendLine("                <div class='card-value'>${summary.total}</div>")
            appendLine("                <div class='card-label'>Total Tests</div>")
            appendLine("            </div>")
            appendLine("            <div class='summary-card passed'>")
            appendLine("                <div class='card-value'>${summary.passed}</div>")
            appendLine("                <div class='card-label'>Passed</div>")
            appendLine("            </div>")
            appendLine("            <div class='summary-card failed'>")
            appendLine("                <div class='card-value'>${summary.failed}</div>")
            appendLine("                <div class='card-label'>Failed</div>")
            appendLine("            </div>")
            appendLine("            <div class='summary-card skipped'>")
            appendLine("                <div class='card-value'>${summary.skipped}</div>")
            appendLine("                <div class='card-label'>Skipped</div>")
            appendLine("            </div>")
            appendLine("            <div class='summary-card duration'>")
            appendLine("                <div class='card-value'>${formatDuration(summary.duration)}</div>")
            appendLine("                <div class='card-label'>Duration</div>")
            appendLine("            </div>")
            appendLine("        </div>")

            appendLine("        <div class='test-results'>")
            appendLine("            <h2>Test Results</h2>")
            summary.results.forEach { result ->
                val statusClass = when (result.status) {
                    ExecutionStatus.PASSED -> "passed"
                    ExecutionStatus.FAILED -> "failed"
                    ExecutionStatus.SKIPPED -> "skipped"
                    else -> "unknown"
                }
                
                appendLine("            <div class='test-case $statusClass'>")
                appendLine("                <div class='test-header'>")
                appendLine("                    <span class='test-status'>${result.status}</span>")
                appendLine("                    <span class='test-name'>${result.scenario}</span>")
                appendLine("                    <span class='test-duration'>${result.duration}ms</span>")
                appendLine("                </div>")
                
                if (result.error != null) {
                    appendLine("                <div class='test-error'>")
                    appendLine("                    <pre>${result.error.message?.escapeHtml()}</pre>")
                    appendLine("                    <details>")
                    appendLine("                        <summary>Stack Trace</summary>")
                    appendLine("                        <pre>${result.error.stackTraceToString().escapeHtml()}</pre>")
                    appendLine("                    </details>")
                    appendLine("                </div>")
                }
                
                appendLine("            </div>")
            }
            appendLine("        </div>")
            
            appendLine("    </div>")
            appendLine("</body>")
            appendLine("</html>")
        }

        val outputFile = File(dir, "index.html")
        outputFile.writeText(html)

        println("${ConsoleColors.GREEN}✓${ConsoleColors.RESET} HTML report generated: ${outputFile.absolutePath}")
    }

    private fun getStyles(): String = """
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f5f5; padding: 20px; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #333; margin-bottom: 10px; }
        h2 { color: #555; margin: 30px 0 20px; }
        .timestamp { color: #888; font-size: 14px; margin-bottom: 30px; }
        .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .summary-card { padding: 20px; border-radius: 6px; text-align: center; }
        .summary-card.total { background: #e3f2fd; color: #1976d2; }
        .summary-card.passed { background: #e8f5e9; color: #388e3c; }
        .summary-card.failed { background: #ffebee; color: #d32f2f; }
        .summary-card.skipped { background: #fff3e0; color: #f57c00; }
        .summary-card.duration { background: #f3e5f5; color: #7b1fa2; }
        .card-value { font-size: 32px; font-weight: bold; margin-bottom: 5px; }
        .card-label { font-size: 14px; text-transform: uppercase; opacity: 0.8; }
        .test-results { margin-top: 30px; }
        .test-case { border: 1px solid #ddd; border-radius: 6px; margin-bottom: 15px; overflow: hidden; }
        .test-case.passed { border-left: 4px solid #4caf50; }
        .test-case.failed { border-left: 4px solid #f44336; }
        .test-case.skipped { border-left: 4px solid #ff9800; }
        .test-header { display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #fafafa; }
        .test-status { font-weight: bold; padding: 4px 12px; border-radius: 4px; font-size: 12px; text-transform: uppercase; }
        .test-case.passed .test-status { background: #4caf50; color: white; }
        .test-case.failed .test-status { background: #f44336; color: white; }
        .test-case.skipped .test-status { background: #ff9800; color: white; }
        .test-name { flex: 1; margin: 0 20px; font-weight: 500; }
        .test-duration { color: #666; font-size: 14px; }
        .test-error { padding: 15px; background: #fff9f9; border-top: 1px solid #ffcdd2; }
        .test-error pre { background: #f5f5f5; padding: 10px; border-radius: 4px; overflow-x: auto; font-size: 13px; }
        details { margin-top: 10px; }
        summary { cursor: pointer; color: #1976d2; font-weight: 500; }
    """.trimIndent()

    private fun formatTimestamp(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        return when {
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }

    private fun String.escapeHtml(): String {
        return this.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
