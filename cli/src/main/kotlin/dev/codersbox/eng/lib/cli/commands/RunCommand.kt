package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import dev.codersbox.eng.lib.cli.execution.*
import dev.codersbox.eng.lib.cli.discovery.TestDiscovery
import dev.codersbox.eng.lib.cli.output.JUnitReporter
import dev.codersbox.eng.lib.cli.output.HtmlReporter
import dev.codersbox.eng.lib.cli.output.JsonReporter
import kotlinx.coroutines.runBlocking

class RunCommand : BaseCommand(
    name = "run",
    help = "Execute API test scenarios"
) {
    private val tags by option("--tags", "-t", help = "Filter by tags (comma-separated)")
    private val exclude by option("--exclude", "-e", help = "Exclude tags (comma-separated)")
    private val scenarios by option("--scenarios", "-s", help = "Specific scenarios to run (comma-separated)")
    private val pattern by option("--pattern", "-p", help = "Regex pattern to match scenario names")
    private val parallel by option("--parallel", help = "Run tests in parallel").flag(default = false)
    private val threads by option("--threads", help = "Max parallel threads").int().default(Runtime.getRuntime().availableProcessors())
    private val failFast by option("--fail-fast", "-f", help = "Stop on first failure").flag(default = false)
    private val retry by option("--retry", "-r", help = "Retry failed tests N times").int().default(0)
    private val timeout by option("--timeout", help = "Test timeout in seconds").int().default(300)
    private val verbose by option("--verbose", "-v", help = "Verbose output").flag(default = false)
    private val reportFormat by option("--report", help = "Report format (junit,html,json,all)").default("all")
    private val reportDir by option("--report-dir", help = "Report output directory").default("./test-reports")

    override fun run() = runBlocking {
        val cliConfig = loadConfig()
        
        // Discover tests
        val discovery = TestDiscovery(cliConfig.testPaths)
        val suites = discovery.discoverTests()
        
        if (suites.isEmpty()) {
            echo("No test suites found!", err = true)
            return@runBlocking
        }

        // Create filter
        val filter = TestFilter(
            tags = tags?.split(",")?.map { it.trim() }?.toSet() ?: emptySet(),
            excludeTags = exclude?.split(",")?.map { it.trim() }?.toSet() ?: emptySet(),
            scenarios = scenarios?.split(",")?.map { it.trim() }?.toSet() ?: emptySet(),
            pattern = pattern?.let { Regex(it) }
        )

        // Count total scenarios
        val totalScenarios = suites.sumOf { suite ->
            suite.scenarios.count { filter.matches(it) }
        }

        if (totalScenarios == 0) {
            echo("No scenarios match the filter criteria!", err = true)
            return@runBlocking
        }

        // Create listeners
        val listeners = mutableListOf<ExecutionListener>(
            ProgressListener(totalScenarios, verbose)
        )

        // Create execution config
        val executionConfig = ExecutionConfig(
            parallel = parallel,
            maxParallel = threads,
            failFast = failFast,
            timeout = timeout * 1000L,
            retryFailedTests = retry,
            listeners = listeners
        )

        // Execute tests
        val executor = TestExecutor(executionConfig)
        val summary = executor.executeTests(suites, filter)

        // Generate reports
        generateReports(summary, reportDir, reportFormat)

        // Exit with appropriate code
        if (summary.failed > 0) {
            throw ProgramExit(1)
        }
    }

    private fun generateReports(
        summary: ExecutionSummary,
        reportDir: String,
        format: String
    ) {
        val formats = if (format == "all") {
            listOf("junit", "html", "json")
        } else {
            format.split(",").map { it.trim() }
        }

        formats.forEach { fmt ->
            when (fmt) {
                "junit" -> JUnitReporter().generate(summary, "$reportDir/junit")
                "html" -> HtmlReporter().generate(summary, "$reportDir/html")
                "json" -> JsonReporter().generate(summary, "$reportDir/json")
            }
        }

        echo("\nReports generated in: $reportDir")
    }
}

class ProgramExit(val code: Int) : Exception()
