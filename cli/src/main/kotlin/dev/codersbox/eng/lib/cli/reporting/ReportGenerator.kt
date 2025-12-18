package dev.codersbox.eng.lib.cli.reporting

import dev.codersbox.eng.lib.cli.execution.ExecutionResult
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface ReportGenerator {
    fun generate(results: List<ExecutionResult>, outputFile: File)
    fun getFileExtension(): String
}

class HtmlReportGenerator : ReportGenerator {
    override fun generate(results: List<ExecutionResult>, outputFile: File) {
        val html = buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html lang='en'>")
            appendLine("<head>")
            appendLine("<meta charset='UTF-8'>")
            appendLine("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
            appendLine("<title>API Test Report</title>")
            appendLine(getStyles())
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<div class='container'>")
            appendLine(generateHeader(results))
            appendLine(generateSummary(results))
            appendLine(generateResultsTable(results))
            appendLine("</div>")
            appendLine(getScripts())
            appendLine("</body>")
            appendLine("</html>")
        }
        outputFile.writeText(html)
    }

    override fun getFileExtension(): String = "html"

    private fun getStyles(): String = """
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; }
            .container { max-width: 1200px; margin: 20px auto; padding: 20px; }
            .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 20px; }
            .header h1 { font-size: 2em; margin-bottom: 10px; }
            .header p { opacity: 0.9; }
            .summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
            .summary-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
            .summary-card h3 { color: #666; font-size: 0.9em; margin-bottom: 10px; text-transform: uppercase; }
            .summary-card .value { font-size: 2em; font-weight: bold; }
            .summary-card.passed .value { color: #10b981; }
            .summary-card.failed .value { color: #ef4444; }
            .summary-card.skipped .value { color: #f59e0b; }
            .results { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden; }
            .results h2 { padding: 20px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; }
            table { width: 100%; border-collapse: collapse; }
            thead { background: #f9fafb; }
            th { padding: 12px; text-align: left; font-weight: 600; color: #374151; border-bottom: 2px solid #e5e7eb; }
            td { padding: 12px; border-bottom: 1px solid #e5e7eb; }
            tr:hover { background: #f9fafb; }
            .badge { display: inline-block; padding: 4px 12px; border-radius: 12px; font-size: 0.85em; font-weight: 500; }
            .badge.success { background: #d1fae5; color: #065f46; }
            .badge.error { background: #fee2e2; color: #991b1b; }
            .badge.skipped { background: #fef3c7; color: #92400e; }
            .duration { color: #6b7280; font-size: 0.9em; }
            .error-message { color: #ef4444; font-size: 0.9em; margin-top: 5px; }
            .expandable { cursor: pointer; user-select: none; }
            .details { display: none; background: #f9fafb; padding: 15px; }
            .details.show { display: block; }
            .filter-buttons { margin-bottom: 20px; }
            .filter-btn { padding: 8px 16px; margin-right: 10px; border: none; background: white; border-radius: 6px; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
            .filter-btn.active { background: #667eea; color: white; }
        </style>
    """

    private fun getScripts(): String = """
        <script>
            function toggleDetails(id) {
                const details = document.getElementById('details-' + id);
                details.classList.toggle('show');
            }
            
            function filterResults(status) {
                const rows = document.querySelectorAll('tbody tr');
                const buttons = document.querySelectorAll('.filter-btn');
                
                buttons.forEach(btn => btn.classList.remove('active'));
                event.target.classList.add('active');
                
                if (status === 'all') {
                    rows.forEach(row => row.style.display = '');
                } else {
                    rows.forEach(row => {
                        const badge = row.querySelector('.badge');
                        if (badge && badge.classList.contains(status)) {
                            row.style.display = '';
                        } else {
                            row.style.display = 'none';
                        }
                    });
                }
            }
        </script>
    """

    private fun generateHeader(results: List<ExecutionResult>): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return """
            <div class='header'>
                <h1>API Integration Test Report</h1>
                <p>Generated on: $timestamp</p>
            </div>
        """
    }

    private fun generateSummary(results: List<ExecutionResult>): String {
        val total = results.size
        val passed = results.count { it.success }
        val failed = results.count { !it.success && it.error != null }
        val skipped = results.count { !it.success && it.error == null }
        val totalDuration = results.sumOf { it.durationMs }
        val successRate = if (total > 0) (passed * 100.0 / total) else 0.0

        return """
            <div class='summary'>
                <div class='summary-card'>
                    <h3>Total Tests</h3>
                    <div class='value'>$total</div>
                </div>
                <div class='summary-card passed'>
                    <h3>Passed</h3>
                    <div class='value'>$passed</div>
                </div>
                <div class='summary-card failed'>
                    <h3>Failed</h3>
                    <div class='value'>$failed</div>
                </div>
                <div class='summary-card skipped'>
                    <h3>Skipped</h3>
                    <div class='value'>$skipped</div>
                </div>
                <div class='summary-card'>
                    <h3>Success Rate</h3>
                    <div class='value'>${"%.1f".format(successRate)}%</div>
                </div>
                <div class='summary-card'>
                    <h3>Duration</h3>
                    <div class='value'>${totalDuration}ms</div>
                </div>
            </div>
        """
    }

    private fun generateResultsTable(results: List<ExecutionResult>): String {
        return buildString {
            appendLine("<div class='results'>")
            appendLine("<h2>Test Results</h2>")
            appendLine("<div class='filter-buttons' style='padding: 15px;'>")
            appendLine("<button class='filter-btn active' onclick='filterResults(\"all\")'>All</button>")
            appendLine("<button class='filter-btn' onclick='filterResults(\"success\")'>Passed</button>")
            appendLine("<button class='filter-btn' onclick='filterResults(\"error\")'>Failed</button>")
            appendLine("<button class='filter-btn' onclick='filterResults(\"skipped\")'>Skipped</button>")
            appendLine("</div>")
            appendLine("<table>")
            appendLine("<thead><tr><th>Scenario</th><th>Step</th><th>Status</th><th>Duration</th></tr></thead>")
            appendLine("<tbody>")
            
            results.forEachIndexed { index, result ->
                val statusClass = when {
                    result.success -> "success"
                    result.error != null -> "error"
                    else -> "skipped"
                }
                val statusText = when {
                    result.success -> "PASSED"
                    result.error != null -> "FAILED"
                    else -> "SKIPPED"
                }
                
                appendLine("<tr class='expandable' onclick='toggleDetails($index)'>")
                appendLine("<td>${result.scenarioName}</td>")
                appendLine("<td>${result.stepName}</td>")
                appendLine("<td><span class='badge $statusClass'>$statusText</span></td>")
                appendLine("<td class='duration'>${result.durationMs}ms</td>")
                appendLine("</tr>")
                
                if (result.error != null) {
                    appendLine("<tr><td colspan='4'>")
                    appendLine("<div class='details' id='details-$index'>")
                    appendLine("<strong>Error:</strong><br>${result.error.escapeHtml()}")
                    appendLine("</div>")
                    appendLine("</td></tr>")
                }
            }
            
            appendLine("</tbody>")
            appendLine("</table>")
            appendLine("</div>")
        }
    }

    private fun String.escapeHtml(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}

class JsonReportGenerator : ReportGenerator {
    override fun generate(results: List<ExecutionResult>, outputFile: File) {
        val json = buildString {
            appendLine("{")
            appendLine("  \"timestamp\": \"${LocalDateTime.now()}\",")
            appendLine("  \"summary\": {")
            appendLine("    \"total\": ${results.size},")
            appendLine("    \"passed\": ${results.count { it.success }},")
            appendLine("    \"failed\": ${results.count { !it.success && it.error != null }},")
            appendLine("    \"skipped\": ${results.count { !it.success && it.error == null }},")
            appendLine("    \"duration\": ${results.sumOf { it.durationMs }}")
            appendLine("  },")
            appendLine("  \"results\": [")
            results.forEachIndexed { index, result ->
                appendLine("    {")
                appendLine("      \"scenario\": \"${result.scenarioName.escapeJson()}\",")
                appendLine("      \"step\": \"${result.stepName.escapeJson()}\",")
                appendLine("      \"success\": ${result.success},")
                appendLine("      \"duration\": ${result.durationMs}")
                if (result.error != null) {
                    appendLine("      ,\"error\": \"${result.error.escapeJson()}\"")
                }
                appendLine("    }${if (index < results.size - 1) "," else ""}")
            }
            appendLine("  ]")
            appendLine("}")
        }
        outputFile.writeText(json)
    }

    override fun getFileExtension(): String = "json"

    private fun String.escapeJson(): String = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

class JunitXmlReportGenerator : ReportGenerator {
    override fun generate(results: List<ExecutionResult>, outputFile: File) {
        val xml = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            val total = results.size
            val failures = results.count { !it.success && it.error != null }
            val skipped = results.count { !it.success && it.error == null }
            val time = results.sumOf { it.durationMs } / 1000.0
            
            appendLine("<testsuite name=\"API Tests\" tests=\"$total\" failures=\"$failures\" skipped=\"$skipped\" time=\"$time\">")
            
            results.forEach { result ->
                val time = result.durationMs / 1000.0
                appendLine("  <testcase name=\"${result.stepName.escapeXml()}\" classname=\"${result.scenarioName.escapeXml()}\" time=\"$time\">")
                
                if (!result.success) {
                    if (result.error != null) {
                        appendLine("    <failure message=\"Test failed\">")
                        appendLine("      ${result.error.escapeXml()}")
                        appendLine("    </failure>")
                    } else {
                        appendLine("    <skipped/>")
                    }
                }
                
                appendLine("  </testcase>")
            }
            
            appendLine("</testsuite>")
        }
        outputFile.writeText(xml)
    }

    override fun getFileExtension(): String = "xml"

    private fun String.escapeXml(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
