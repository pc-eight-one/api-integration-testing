package dev.codersbox.eng.lib.cli.ci

import dev.codersbox.eng.lib.cli.execution.TestResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class CiTestReport(
    val environment: String,
    val buildId: String?,
    val branch: String?,
    val commitSha: String?,
    val timestamp: String,
    val summary: TestSummary,
    val tests: List<TestCase>
)

@Serializable
data class TestSummary(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val duration: Long,
    val successRate: Double
)

@Serializable
data class TestCase(
    val name: String,
    val status: String,
    val duration: Long,
    val error: String? = null
)

class CiReporter(
    private val ciEnv: CiEnvironment,
    private val outputDir: File
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    fun generateReports(result: TestResult) {
        // Generate JSON report
        generateJsonReport(result)
        
        // Generate JUnit XML for CI integration
        generateJUnitXml(result)
        
        // Generate Markdown summary
        generateMarkdownSummary(result)
        
        // Generate badge data
        generateBadgeData(result)
    }
    
    private fun generateJsonReport(result: TestResult) {
        val report = CiTestReport(
            environment = ciEnv.name,
            buildId = ciEnv.buildId,
            branch = ciEnv.branch,
            commitSha = ciEnv.commitSha,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            summary = TestSummary(
                total = result.total,
                passed = result.passed,
                failed = result.failed,
                skipped = result.skipped,
                duration = result.duration,
                successRate = if (result.total > 0) (result.passed.toDouble() / result.total * 100) else 0.0
            ),
            tests = result.tests.map { 
                TestCase(
                    name = it.name,
                    status = it.status,
                    duration = it.duration,
                    error = it.error
                )
            }
        )
        
        val reportFile = File(outputDir, "ci-report.json")
        reportFile.writeText(json.encodeToString(report))
        println("Generated CI report: ${reportFile.absolutePath}")
    }
    
    private fun generateJUnitXml(result: TestResult) {
        val xml = buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<testsuite name=\"API Integration Tests\" " +
                "tests=\"${result.total}\" " +
                "failures=\"${result.failed}\" " +
                "skipped=\"${result.skipped}\" " +
                "time=\"${result.duration / 1000.0}\">")
            
            result.tests.forEach { test ->
                appendLine("  <testcase name=\"${escapeXml(test.name)}\" " +
                    "time=\"${test.duration / 1000.0}\">")
                
                when (test.status) {
                    "FAILED" -> {
                        appendLine("    <failure message=\"${escapeXml(test.error ?: "Test failed")}\">")
                        appendLine("      ${escapeXml(test.error ?: "")}")
                        appendLine("    </failure>")
                    }
                    "SKIPPED" -> {
                        appendLine("    <skipped/>")
                    }
                }
                
                appendLine("  </testcase>")
            }
            
            appendLine("</testsuite>")
        }
        
        val xmlFile = File(outputDir, "junit-report.xml")
        xmlFile.writeText(xml)
        println("Generated JUnit XML: ${xmlFile.absolutePath}")
    }
    
    private fun generateMarkdownSummary(result: TestResult) {
        val successRate = if (result.total > 0) 
            (result.passed.toDouble() / result.total * 100) else 0.0
        
        val status = if (result.failed == 0) "✅ PASSED" else "❌ FAILED"
        
        val markdown = buildString {
            appendLine("# API Integration Test Results")
            appendLine()
            appendLine("## Summary")
            appendLine()
            appendLine("**Status:** $status")
            appendLine()
            appendLine("| Metric | Value |")
            appendLine("|--------|-------|")
            appendLine("| Total Tests | ${result.total} |")
            appendLine("| Passed | ${result.passed} ✓ |")
            appendLine("| Failed | ${result.failed} ✗ |")
            appendLine("| Skipped | ${result.skipped} ⊘ |")
            appendLine("| Success Rate | ${"%.2f".format(successRate)}% |")
            appendLine("| Duration | ${result.duration}ms |")
            appendLine()
            
            if (ciEnv.buildUrl != null) {
                appendLine("**Build URL:** ${ciEnv.buildUrl}")
                appendLine()
            }
            
            appendLine("## Environment")
            appendLine()
            appendLine("- **CI:** ${ciEnv.name}")
            appendLine("- **Branch:** ${ciEnv.branch ?: "N/A"}")
            appendLine("- **Commit:** ${ciEnv.commitSha?.take(8) ?: "N/A"}")
            if (ciEnv.isPullRequest) {
                appendLine("- **PR:** #${ciEnv.pullRequestNumber}")
            }
            appendLine()
            
            if (result.failed > 0) {
                appendLine("## Failed Tests")
                appendLine()
                result.tests.filter { it.status == "FAILED" }.forEach { test ->
                    appendLine("### ${test.name}")
                    appendLine()
                    appendLine("```")
                    appendLine(test.error ?: "No error message")
                    appendLine("```")
                    appendLine()
                }
            }
        }
        
        val mdFile = File(outputDir, "test-summary.md")
        mdFile.writeText(markdown)
        println("Generated Markdown summary: ${mdFile.absolutePath}")
        
        // For GitHub Actions, also output to step summary
        if (ciEnv.name == "GitHub Actions") {
            System.getenv("GITHUB_STEP_SUMMARY")?.let { summaryPath ->
                File(summaryPath).appendText(markdown)
            }
        }
    }
    
    private fun generateBadgeData(result: TestResult) {
        val successRate = if (result.total > 0) 
            (result.passed.toDouble() / result.total * 100) else 0.0
        
        val color = when {
            successRate >= 95 -> "brightgreen"
            successRate >= 80 -> "green"
            successRate >= 60 -> "yellow"
            successRate >= 40 -> "orange"
            else -> "red"
        }
        
        val badgeData = mapOf(
            "schemaVersion" to 1,
            "label" to "tests",
            "message" to "${result.passed}/${result.total} passed",
            "color" to color
        )
        
        val badgeFile = File(outputDir, "badge.json")
        badgeFile.writeText(json.encodeToString(badgeData))
        println("Generated badge data: ${badgeFile.absolutePath}")
    }
    
    fun generateCoverageReport(result: TestResult) {
        // API endpoint coverage tracking
        val endpointCoverage = result.tests
            .flatMap { it.endpoints }
            .groupBy { it }
            .mapValues { it.value.size }
        
        val coverageReport = buildString {
            appendLine("# API Coverage Report")
            appendLine()
            appendLine("## Endpoint Coverage")
            appendLine()
            appendLine("| Endpoint | Hit Count |")
            appendLine("|----------|-----------|")
            endpointCoverage.entries.sortedByDescending { it.value }.forEach { (endpoint, count) ->
                appendLine("| `$endpoint` | $count |")
            }
        }
        
        val coverageFile = File(outputDir, "coverage-report.md")
        coverageFile.writeText(coverageReport)
        println("Generated coverage report: ${coverageFile.absolutePath}")
    }
    
    fun compareWithBaseline(result: TestResult, baselineFile: File) {
        if (!baselineFile.exists()) {
            println("Baseline file not found: ${baselineFile.absolutePath}")
            return
        }
        
        val baseline = json.decodeFromString<CiTestReport>(baselineFile.readText())
        
        val comparison = buildString {
            appendLine("# Baseline Comparison")
            appendLine()
            appendLine("| Metric | Current | Baseline | Change |")
            appendLine("|--------|---------|----------|--------|")
            
            val totalChange = result.total - baseline.summary.total
            appendLine("| Total Tests | ${result.total} | ${baseline.summary.total} | ${formatChange(totalChange)} |")
            
            val passedChange = result.passed - baseline.summary.passed
            appendLine("| Passed | ${result.passed} | ${baseline.summary.passed} | ${formatChange(passedChange)} |")
            
            val failedChange = result.failed - baseline.summary.failed
            appendLine("| Failed | ${result.failed} | ${baseline.summary.failed} | ${formatChange(failedChange)} |")
            
            val durationChange = result.duration - baseline.summary.duration
            val durationPercent = if (baseline.summary.duration > 0) 
                (durationChange.toDouble() / baseline.summary.duration * 100) else 0.0
            appendLine("| Duration | ${result.duration}ms | ${baseline.summary.duration}ms | ${formatChange(durationChange)} (${"%+.1f".format(durationPercent)}%) |")
        }
        
        val comparisonFile = File(outputDir, "baseline-comparison.md")
        comparisonFile.writeText(comparison)
        println("Generated baseline comparison: ${comparisonFile.absolutePath}")
    }
    
    private fun formatChange(change: Int): String {
        return when {
            change > 0 -> "+$change"
            change < 0 -> "$change"
            else -> "0"
        }
    }
    
    private fun formatChange(change: Long): String {
        return when {
            change > 0 -> "+$change"
            change < 0 -> "$change"
            else -> "0"
        }
    }
    
    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
