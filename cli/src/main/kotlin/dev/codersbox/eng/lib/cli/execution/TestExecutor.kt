package dev.codersbox.eng.lib.cli.execution

import dev.codersbox.eng.lib.api.testing.dsl.ApiTestSuite
import dev.codersbox.eng.lib.api.testing.dsl.Scenario
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class ExecutionResult(
    val scenario: String,
    val status: ExecutionStatus,
    val duration: Long,
    val error: Throwable? = null,
    val steps: List<StepResult> = emptyList()
)

data class StepResult(
    val name: String,
    val status: ExecutionStatus,
    val duration: Long,
    val error: Throwable? = null
)

enum class ExecutionStatus {
    PASSED, FAILED, SKIPPED, RUNNING
}

class TestExecutor(
    private val config: ExecutionConfig
) {
    private val results = ConcurrentHashMap<String, ExecutionResult>()
    private val passedCount = AtomicInteger(0)
    private val failedCount = AtomicInteger(0)
    private val skippedCount = AtomicInteger(0)

    suspend fun executeTests(
        suites: List<ApiTestSuite>,
        filter: TestFilter = TestFilter()
    ): ExecutionSummary = coroutineScope {
        val startTime = System.currentTimeMillis()
        
        val scenarios = suites.flatMap { suite ->
            suite.scenarios.map { scenario -> suite to scenario }
        }.filter { (_, scenario) -> filter.matches(scenario) }

        if (config.parallel) {
            executeParallel(scenarios)
        } else {
            executeSequential(scenarios)
        }

        val endTime = System.currentTimeMillis()
        
        ExecutionSummary(
            total = results.size,
            passed = passedCount.get(),
            failed = failedCount.get(),
            skipped = skippedCount.get(),
            duration = endTime - startTime,
            results = results.values.toList()
        )
    }

    private suspend fun executeSequential(
        scenarios: List<Pair<ApiTestSuite, Scenario>>
    ) {
        for ((suite, scenario) in scenarios) {
            if (config.failFast && failedCount.get() > 0) {
                skipScenario(scenario)
                continue
            }
            executeScenario(suite, scenario)
        }
    }

    private suspend fun executeParallel(
        scenarios: List<Pair<ApiTestSuite, Scenario>>
    ) = coroutineScope {
        val semaphore = kotlinx.coroutines.sync.Semaphore(config.maxParallel)
        
        scenarios.map { (suite, scenario) ->
            async {
                semaphore.withPermit {
                    if (config.failFast && failedCount.get() > 0) {
                        skipScenario(scenario)
                    } else {
                        executeScenario(suite, scenario)
                    }
                }
            }
        }.awaitAll()
    }

    private suspend fun executeScenario(
        suite: ApiTestSuite,
        scenario: Scenario
    ) {
        val startTime = System.currentTimeMillis()
        
        try {
            config.listeners.forEach { it.onScenarioStart(scenario.name) }
            
            // Execute before hooks
            suite.beforeScenarioHooks.forEach { it() }
            
            // Execute scenario steps
            val stepResults = mutableListOf<StepResult>()
            for (step in scenario.steps) {
                val stepStart = System.currentTimeMillis()
                try {
                    step.execute()
                    stepResults.add(
                        StepResult(
                            name = step.name,
                            status = ExecutionStatus.PASSED,
                            duration = System.currentTimeMillis() - stepStart
                        )
                    )
                } catch (e: Exception) {
                    stepResults.add(
                        StepResult(
                            name = step.name,
                            status = ExecutionStatus.FAILED,
                            duration = System.currentTimeMillis() - stepStart,
                            error = e
                        )
                    )
                    throw e
                }
            }
            
            // Execute after hooks
            suite.afterScenarioHooks.forEach { it() }
            
            val duration = System.currentTimeMillis() - startTime
            val result = ExecutionResult(
                scenario = scenario.name,
                status = ExecutionStatus.PASSED,
                duration = duration,
                steps = stepResults
            )
            
            results[scenario.name] = result
            passedCount.incrementAndGet()
            config.listeners.forEach { it.onScenarioComplete(scenario.name, result) }
            
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            val result = ExecutionResult(
                scenario = scenario.name,
                status = ExecutionStatus.FAILED,
                duration = duration,
                error = e
            )
            
            results[scenario.name] = result
            failedCount.incrementAndGet()
            config.listeners.forEach { it.onScenarioComplete(scenario.name, result) }
        }
    }

    private fun skipScenario(scenario: Scenario) {
        val result = ExecutionResult(
            scenario = scenario.name,
            status = ExecutionStatus.SKIPPED,
            duration = 0
        )
        results[scenario.name] = result
        skippedCount.incrementAndGet()
    }
}

data class ExecutionConfig(
    val parallel: Boolean = false,
    val maxParallel: Int = Runtime.getRuntime().availableProcessors(),
    val failFast: Boolean = false,
    val timeout: Long = 300000, // 5 minutes default
    val retryFailedTests: Int = 0,
    val listeners: List<ExecutionListener> = emptyList()
)

data class ExecutionSummary(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val duration: Long,
    val results: List<ExecutionResult>
)

data class TestFilter(
    val tags: Set<String> = emptySet(),
    val scenarios: Set<String> = emptySet(),
    val excludeTags: Set<String> = emptySet(),
    val pattern: Regex? = null
) {
    fun matches(scenario: Scenario): Boolean {
        // Match by name pattern
        if (pattern != null && !pattern.matches(scenario.name)) {
            return false
        }
        
        // Match by specific scenario names
        if (scenarios.isNotEmpty() && !scenarios.contains(scenario.name)) {
            return false
        }
        
        // Match by tags
        if (tags.isNotEmpty() && scenario.tags.none { it in tags }) {
            return false
        }
        
        // Exclude by tags
        if (excludeTags.isNotEmpty() && scenario.tags.any { it in excludeTags }) {
            return false
        }
        
        return true
    }
}

interface ExecutionListener {
    fun onScenarioStart(name: String) {}
    fun onScenarioComplete(name: String, result: ExecutionResult) {}
    fun onExecutionStart() {}
    fun onExecutionComplete(summary: ExecutionSummary) {}
}
