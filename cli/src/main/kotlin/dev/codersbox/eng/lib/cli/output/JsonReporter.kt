package dev.codersbox.eng.lib.cli.output

import dev.codersbox.eng.lib.cli.execution.ExecutionSummary
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class JsonReporter {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun generate(summary: ExecutionSummary, outputDir: String) {
        val dir = File(outputDir)
        dir.mkdirs()

        val report = TestReport(
            timestamp = System.currentTimeMillis(),
            summary = SummaryData(
                total = summary.total,
                passed = summary.passed,
                failed = summary.failed,
                skipped = summary.skipped,
                duration = summary.duration
            ),
            results = summary.results.map { result ->
                TestResult(
                    scenario = result.scenario,
                    status = result.status.name,
                    duration = result.duration,
                    error = result.error?.message,
                    steps = result.steps.map { step ->
                        StepData(
                            name = step.name,
                            status = step.status.name,
                            duration = step.duration,
                            error = step.error?.message
                        )
                    }
                )
            }
        )

        val outputFile = File(dir, "test-results.json")
        outputFile.writeText(json.encodeToString(report))

        println("${ConsoleColors.GREEN}✓${ConsoleColors.RESET} JSON report generated: ${outputFile.absolutePath}")
    }
}

@Serializable
data class TestReport(
    val timestamp: Long,
    val summary: SummaryData,
    val results: List<TestResult>
)

@Serializable
data class SummaryData(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val duration: Long
)

@Serializable
data class TestResult(
    val scenario: String,
    val status: String,
    val duration: Long,
    val error: String? = null,
    val steps: List<StepData> = emptyList()
)

@Serializable
data class StepData(
    val name: String,
    val status: String,
    val duration: Long,
    val error: String? = null
)
