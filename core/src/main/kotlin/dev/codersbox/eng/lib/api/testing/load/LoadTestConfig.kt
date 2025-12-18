package dev.codersbox.eng.lib.api.testing.load

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.minutes

/**
 * Configuration for load testing
 */
data class LoadTestConfig(
    val users: Int = 1,
    val duration: Duration = 1.minutes,
    val rampUp: Duration = 10.seconds,
    val requestsPerSecond: Int? = null,
    val thinkTime: Duration = 0.seconds,
    val maxResponseTime: Duration? = null,
    val successRateThreshold: Double = 0.95,
    val reportInterval: Duration = 10.seconds,
    val profile: LoadProfile = LoadProfile.CONSTANT
) {
    companion object {
        /**
         * Quick load test for smoke testing
         */
        fun smoke() = LoadTestConfig(
            users = 5,
            duration = 30.seconds,
            rampUp = 5.seconds
        )
        
        /**
         * Stress test with gradual load increase
         */
        fun stress(maxUsers: Int = 100) = LoadTestConfig(
            users = maxUsers,
            duration = 5.minutes,
            rampUp = 2.minutes,
            profile = LoadProfile.RAMP_UP
        )
        
        /**
         * Spike test with sudden load
         */
        fun spike(users: Int = 200) = LoadTestConfig(
            users = users,
            duration = 2.minutes,
            rampUp = 5.seconds,
            profile = LoadProfile.SPIKE
        )
        
        /**
         * Endurance test for long duration
         */
        fun endurance(users: Int = 50, hours: Int = 1) = LoadTestConfig(
            users = users,
            duration = (hours * 60).minutes,
            rampUp = 5.minutes,
            profile = LoadProfile.CONSTANT
        )
        
        /**
         * Capacity test to find breaking point
         */
        fun capacity() = LoadTestConfig(
            users = 500,
            duration = 10.minutes,
            rampUp = 5.minutes,
            profile = LoadProfile.STEP
        )
    }
}

/**
 * Load profile types
 */
enum class LoadProfile {
    CONSTANT,    // Constant load throughout test
    RAMP_UP,     // Gradual increase
    RAMP_DOWN,   // Gradual decrease
    SPIKE,       // Sudden spike
    STEP,        // Step-wise increase
    WAVE         // Wave pattern
}

/**
 * Performance thresholds
 */
data class PerformanceThresholds(
    val maxResponseTime: Duration? = null,
    val p50ResponseTime: Duration? = null,
    val p90ResponseTime: Duration? = null,
    val p95ResponseTime: Duration? = null,
    val p99ResponseTime: Duration? = null,
    val minRequestsPerSecond: Int? = null,
    val maxErrorRate: Double = 0.05,
    val minSuccessRate: Double = 0.95
)

/**
 * Load test results
 */
data class LoadTestResults(
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val totalDuration: Duration,
    val minResponseTime: Duration,
    val maxResponseTime: Duration,
    val avgResponseTime: Duration,
    val p50ResponseTime: Duration,
    val p90ResponseTime: Duration,
    val p95ResponseTime: Duration,
    val p99ResponseTime: Duration,
    val requestsPerSecond: Double,
    val successRate: Double,
    val errorRate: Double,
    val errors: Map<String, Int> = emptyMap()
) {
    fun summary(): String {
        return """
            Load Test Results:
            ==================
            Total Requests:     $totalRequests
            Successful:         $successfulRequests
            Failed:             $failedRequests
            Success Rate:       ${String.format("%.2f%%", successRate * 100)}
            Error Rate:         ${String.format("%.2f%%", errorRate * 100)}
            
            Response Times:
            ==================
            Min:                ${minResponseTime.inWholeMilliseconds}ms
            Max:                ${maxResponseTime.inWholeMilliseconds}ms
            Average:            ${avgResponseTime.inWholeMilliseconds}ms
            p50 (median):       ${p50ResponseTime.inWholeMilliseconds}ms
            p90:                ${p90ResponseTime.inWholeMilliseconds}ms
            p95:                ${p95ResponseTime.inWholeMilliseconds}ms
            p99:                ${p99ResponseTime.inWholeMilliseconds}ms
            
            Throughput:
            ==================
            Requests/sec:       ${String.format("%.2f", requestsPerSecond)}
            Duration:           ${totalDuration.inWholeSeconds}s
        """.trimIndent()
    }
    
    fun meetsThresholds(thresholds: PerformanceThresholds): Boolean {
        return listOfNotNull(
            thresholds.maxResponseTime?.let { maxResponseTime <= it },
            thresholds.p50ResponseTime?.let { p50ResponseTime <= it },
            thresholds.p90ResponseTime?.let { p90ResponseTime <= it },
            thresholds.p95ResponseTime?.let { p95ResponseTime <= it },
            thresholds.p99ResponseTime?.let { p99ResponseTime <= it },
            thresholds.minRequestsPerSecond?.let { requestsPerSecond >= it },
            thresholds.maxErrorRate.let { errorRate <= it },
            thresholds.minSuccessRate.let { successRate >= it }
        ).all { it }
    }
}

/**
 * Request metrics
 */
data class RequestMetric(
    val timestamp: Long,
    val duration: Duration,
    val success: Boolean,
    val statusCode: Int?,
    val error: String? = null,
    val scenarioName: String
)
