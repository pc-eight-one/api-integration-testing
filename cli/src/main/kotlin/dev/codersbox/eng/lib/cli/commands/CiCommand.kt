package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import dev.codersbox.eng.lib.cli.ci.CiDetector
import dev.codersbox.eng.lib.cli.ci.CiReporter
import dev.codersbox.eng.lib.cli.execution.TestExecutor
import kotlinx.coroutines.runBlocking
import java.io.File

class CiCommand : CliktCommand(
    name = "ci",
    help = "Run tests in CI/CD mode with environment detection and optimized reporting"
) {
    private val configPath by option("-c", "--config", help = "Path to configuration file")
        .default("api-test-config.yaml")
    
    private val tags by option("-t", "--tags", help = "Comma-separated tags to filter tests")
    
    private val failFast by option("--fail-fast", help = "Stop on first failure")
        .flag(default = false)
    
    private val parallelism by option("-p", "--parallel", help = "Number of parallel workers")
        .int()
    
    private val retryFailed by option("--retry-failed", help = "Number of times to retry failed tests")
        .int()
        .default(0)
    
    private val outputDir by option("-o", "--output", help = "Output directory for reports")
        .default("test-results")
    
    private val coverage by option("--coverage", help = "Generate API coverage report")
        .flag(default = false)
    
    private val baseline by option("--baseline", help = "Compare results with baseline")
    
    override fun run() = runBlocking {
        val ciEnv = CiDetector.detect()
        echo("Detected CI environment: ${ciEnv.name}")
        echo("Build ID: ${ciEnv.buildId}")
        echo("Branch: ${ciEnv.branch}")
        echo("Commit: ${ciEnv.commitSha}")
        
        val configFile = File(configPath)
        if (!configFile.exists()) {
            echo("Error: Configuration file not found: $configPath", err = true)
            return@runBlocking
        }
        
        val outputDirectory = File(outputDir)
        outputDirectory.mkdirs()
        
        val executor = TestExecutor(
            configPath = configPath,
            tags = tags?.split(",")?.map { it.trim() },
            failFast = failFast,
            parallel = parallelism,
            retryCount = retryFailed
        )
        
        echo("\n🚀 Starting test execution in CI mode...")
        
        val result = executor.execute()
        
        // Generate CI-specific reports
        val reporter = CiReporter(ciEnv, outputDirectory)
        reporter.generateReports(result)
        
        if (coverage) {
            echo("\n📊 Generating API coverage report...")
            reporter.generateCoverageReport(result)
        }
        
        if (baseline != null) {
            echo("\n📈 Comparing with baseline: $baseline")
            reporter.compareWithBaseline(result, File(baseline!!))
        }
        
        // Print summary
        echo("\n" + "=".repeat(60))
        echo("Test Results Summary")
        echo("=".repeat(60))
        echo("Total: ${result.total}")
        echo("Passed: ${result.passed} ✓")
        echo("Failed: ${result.failed} ✗")
        echo("Skipped: ${result.skipped} ⊘")
        echo("Duration: ${result.duration}ms")
        echo("=".repeat(60))
        
        // Set exit code based on results
        if (result.failed > 0) {
            echo("\n❌ Tests failed!", err = true)
            throw RuntimeException("Tests failed")
        } else {
            echo("\n✅ All tests passed!")
        }
    }
}
