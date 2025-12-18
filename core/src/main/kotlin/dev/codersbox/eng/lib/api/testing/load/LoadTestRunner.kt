package dev.codersbox.eng.lib.api.testing.load

import dev.codersbox.eng.lib.api.testing.dsl.Scenario
import dev.codersbox.eng.lib.api.testing.context.ScenarioContext
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Load test runner for executing performance tests
 */
class LoadTestRunner(
    private val config: LoadTestConfig,
    private val apiConfig: ApiTestConfig
) {
    private val metrics = ConcurrentLinkedQueue<RequestMetric>()
    private val activeUsers = AtomicLong(0)
    private val completedRequests = AtomicLong(0)
    private val failedRequests = AtomicLong(0)
    
    /**
     * Execute a load test scenario
     */
    suspend fun execute(
        scenarioName: String,
        block: suspend Scenario.() -> Unit
    ): LoadTestResults = coroutineScope {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + config.duration.inWholeMilliseconds
        
        val reportingJob = launch {
            reportProgress(startTime)
        }
        
        val userJobs = generateUserLoad(scenarioName, block, startTime, endTime)
        userJobs.joinAll()
        reportingJob.cancel()
        
        val totalDuration = (System.currentTimeMillis() - startTime).milliseconds
        calculateResults(totalDuration)
    }
    
    /**
     * Execute multiple scenarios with different weights
     */
    suspend fun executeMultiple(
        scenarios: Map<String, Pair<Double, suspend Scenario.() -> Unit>>
    ): LoadTestResults = coroutineScope {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + config.duration.inWholeMilliseconds
        
        val reportingJob = launch {
            reportProgress(startTime)
        }
        
        val totalWeight = scenarios.values.sumOf { it.first }
        val userJobs = mutableListOf<Job>()
        
        scenarios.forEach { (name, pair) ->
            val (weight, block) = pair
            val usersForScenario = (config.users * (weight / totalWeight)).toInt()
            
            userJobs.addAll(
                generateUserLoad(name, block, startTime, endTime, usersForScenario)
            )
        }
        
        userJobs.joinAll()
        reportingJob.cancel()
        
        val totalDuration = (System.currentTimeMillis() - startTime).milliseconds
        calculateResults(totalDuration)
    }
    
    /**
     * Generate user load based on profile
     */
    private fun CoroutineScope.generateUserLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int = config.users
    ): List<Job> {
        return when (config.profile) {
            LoadProfile.CONSTANT -> generateConstantLoad(scenarioName, block, startTime, endTime, userCount)
            LoadProfile.RAMP_UP -> generateRampUpLoad(scenarioName, block, startTime, endTime, userCount)
            LoadProfile.SPIKE -> generateSpikeLoad(scenarioName, block, startTime, endTime, userCount)
            LoadProfile.STEP -> generateStepLoad(scenarioName, block, startTime, endTime, userCount)
            LoadProfile.WAVE -> generateWaveLoad(scenarioName, block, startTime, endTime, userCount)
            LoadProfile.RAMP_DOWN -> generateRampDownLoad(scenarioName, block, startTime, endTime, userCount)
        }
    }
    
    private fun CoroutineScope.generateConstantLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int
    ): List<Job> {
        val delayBetweenUsers = config.rampUp.inWholeMilliseconds / userCount
        
        return (1..userCount).map { userIndex ->
            launch {
                delay((userIndex - 1) * delayBetweenUsers)
                runUser(scenarioName, block, endTime)
            }
        }
    }
    
    private fun CoroutineScope.generateRampUpLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int
    ): List<Job> {
        return generateConstantLoad(scenarioName, block, startTime, endTime, userCount)
    }
    
    private fun CoroutineScope.generateSpikeLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int
    ): List<Job> {
        return (1..userCount).map {
            launch {
                delay(config.rampUp.inWholeMilliseconds)
                runUser(scenarioName, block, endTime)
            }
        }
    }
    
    private fun CoroutineScope.generateStepLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int
    ): List<Job> {
        val steps = 5
        val usersPerStep = userCount / steps
        val stepDuration = config.rampUp.inWholeMilliseconds / steps
        
        return (1..userCount).map { userIndex ->
            val step = (userIndex - 1) / usersPerStep
            launch {
                delay(step * stepDuration)
                runUser(scenarioName, block, endTime)
            }
        }
    }
    
    private fun CoroutineScope.generateWaveLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int
    ): List<Job> {
        val waveDuration = config.rampUp.inWholeMilliseconds / 4
        
        return (1..userCount).map { userIndex ->
            val phase = (userIndex - 1) % (userCount / 4)
            val wave = (userIndex - 1) / (userCount / 4)
            launch {
                delay(wave * waveDuration + phase * 100)
                runUser(scenarioName, block, endTime)
            }
        }
    }
    
    private fun CoroutineScope.generateRampDownLoad(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        startTime: Long,
        endTime: Long,
        userCount: Int
    ): List<Job> {
        return (1..userCount).map { userIndex ->
            launch {
                runUser(scenarioName, block, endTime)
                delay((userIndex - 1) * (config.duration.inWholeMilliseconds / userCount))
            }
        }
    }
    
    private suspend fun runUser(
        scenarioName: String,
        block: suspend Scenario.() -> Unit,
        endTime: Long
    ) {
        activeUsers.incrementAndGet()
        
        try {
            while (System.currentTimeMillis() < endTime) {
                val requestStart = System.currentTimeMillis()
                var success = false
                var statusCode: Int? = null
                var error: String? = null
                
                try {
                    val context = ScenarioContext()
                    val scenario = Scenario(scenarioName, context, apiConfig)
                    block(scenario)
                    success = true
                    completedRequests.incrementAndGet()
                } catch (e: Exception) {
                    success = false
                    error = e.message
                    failedRequests.incrementAndGet()
                }
                
                val duration = (System.currentTimeMillis() - requestStart).milliseconds
                
                metrics.add(
                    RequestMetric(
                        timestamp = requestStart,
                        duration = duration,
                        success = success,
                        statusCode = statusCode,
                        error = error,
                        scenarioName = scenarioName
                    )
                )
                
                if (config.thinkTime > 0.seconds) {
                    delay(config.thinkTime.inWholeMilliseconds)
                }
                
                if (System.currentTimeMillis() >= endTime) break
            }
        } finally {
            activeUsers.decrementAndGet()
        }
    }
    
    private suspend fun reportProgress(startTime: Long) {
        while (true) {
            delay(config.reportInterval.inWholeMilliseconds)
            
            val elapsed = (System.currentTimeMillis() - startTime).milliseconds
            val completed = completedRequests.get()
            val failed = failedRequests.get()
            val active = activeUsers.get()
            val rps = completed.toDouble() / elapsed.inWholeSeconds
            
            println("""
                [${elapsed.inWholeSeconds}s] Active: $active | Completed: $completed | Failed: $failed | RPS: ${String.format("%.2f", rps)}
            """.trimIndent())
        }
    }
    
    private fun calculateResults(totalDuration: Duration): LoadTestResults {
        val metricsList = metrics.toList()
        val successful = metricsList.count { it.success }.toLong()
        val failed = metricsList.count { !it.success }.toLong()
        val total = metricsList.size.toLong()
        
        val sortedDurations = metricsList.map { it.duration }.sorted()
        
        val errors = metricsList
            .filter { !it.success && it.error != null }
            .groupBy { it.error!! }
            .mapValues { it.value.size }
        
        return LoadTestResults(
            totalRequests = total,
            successfulRequests = successful,
            failedRequests = failed,
            totalDuration = totalDuration,
            minResponseTime = sortedDurations.minOrNull() ?: 0.seconds,
            maxResponseTime = sortedDurations.maxOrNull() ?: 0.seconds,
            avgResponseTime = (sortedDurations.sumOf { it.inWholeMilliseconds } / total).milliseconds,
            p50ResponseTime = percentile(sortedDurations, 0.50),
            p90ResponseTime = percentile(sortedDurations, 0.90),
            p95ResponseTime = percentile(sortedDurations, 0.95),
            p99ResponseTime = percentile(sortedDurations, 0.99),
            requestsPerSecond = total.toDouble() / totalDuration.inWholeSeconds,
            successRate = successful.toDouble() / total,
            errorRate = failed.toDouble() / total,
            errors = errors
        )
    }
    
    private fun percentile(sortedList: List<Duration>, percentile: Double): Duration {
        if (sortedList.isEmpty()) return 0.seconds
        val index = ((sortedList.size - 1) * percentile).toInt()
        return sortedList[index]
    }
}

/**
 * DSL for load testing
 */
class LoadTestDsl(private val config: LoadTestConfig, private val apiConfig: ApiTestConfig) {
    private val scenarios = mutableMapOf<String, Pair<Double, suspend Scenario.() -> Unit>>()
    
    fun scenario(name: String, weight: Double = 1.0, block: suspend Scenario.() -> Unit) {
        scenarios[name] = weight to block
    }
    
    suspend fun execute(): LoadTestResults {
        val runner = LoadTestRunner(config, apiConfig)
        
        return if (scenarios.size == 1) {
            val (name, pair) = scenarios.entries.first()
            runner.execute(name, pair.second)
        } else {
            runner.executeMultiple(scenarios)
        }
    }
}

/**
 * DSL function to create load tests
 */
suspend fun loadTest(
    config: LoadTestConfig,
    apiConfig: ApiTestConfig,
    block: LoadTestDsl.() -> Unit
): LoadTestResults {
    val dsl = LoadTestDsl(config, apiConfig)
    dsl.block()
    return dsl.execute()
}
