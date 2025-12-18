package dev.codersbox.eng.lib.api.testing.load

import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.minutes

/**
 * Simple load test configuration from file (no Jackson dependency)
 */
object LoadTestConfigLoader {
    
    /**
     * Load from properties-style format
     */
    fun fromProperties(filePath: String): LoadTestConfigFile {
        val file = File(filePath)
        val props = file.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .associate {
                val parts = it.split("=", limit = 2)
                parts[0].trim() to parts[1].trim()
            }
        
        return LoadTestConfigFile(
            name = props["name"] ?: "Load Test",
            description = props["description"],
            load = LoadConfigSpec(
                users = props["load.users"]?.toInt() ?: 1,
                duration = props["load.duration"] ?: "1m",
                rampUp = props["load.rampUp"] ?: "10s",
                profile = props["load.profile"] ?: "constant",
                requestsPerSecond = props["load.requestsPerSecond"]?.toInt(),
                thinkTime = props["load.thinkTime"],
                maxResponseTime = props["load.maxResponseTime"],
                successRateThreshold = props["load.successRateThreshold"]?.toDouble(),
                reportInterval = props["load.reportInterval"]
            ),
            scenarios = emptyList(),
            thresholds = props["thresholds.maxResponseTime"]?.let {
                PerformanceThresholdsSpec(
                    maxResponseTime = it,
                    p50ResponseTime = props["thresholds.p50ResponseTime"],
                    p90ResponseTime = props["thresholds.p90ResponseTime"],
                    p95ResponseTime = props["thresholds.p95ResponseTime"],
                    p99ResponseTime = props["thresholds.p99ResponseTime"],
                    minRequestsPerSecond = props["thresholds.minRequestsPerSecond"]?.toInt(),
                    maxErrorRate = props["thresholds.maxErrorRate"]?.toDouble(),
                    minSuccessRate = props["thresholds.minSuccessRate"]?.toDouble()
                )
            }
        )
    }
}

/**
 * Load test configuration file structure
 */
data class LoadTestConfigFile(
    val name: String,
    val description: String? = null,
    val load: LoadConfigSpec,
    val scenarios: List<LoadScenarioSpec>,
    val thresholds: PerformanceThresholdsSpec? = null
) {
    fun toLoadTestConfig(): LoadTestConfig {
        return LoadTestConfig(
            users = load.users,
            duration = parseDuration(load.duration),
            rampUp = parseDuration(load.rampUp),
            requestsPerSecond = load.requestsPerSecond,
            thinkTime = load.thinkTime?.let { parseDuration(it) } ?: 0.seconds,
            maxResponseTime = load.maxResponseTime?.let { parseDuration(it) },
            successRateThreshold = load.successRateThreshold ?: 0.95,
            reportInterval = load.reportInterval?.let { parseDuration(it) } ?: 10.seconds,
            profile = LoadProfile.valueOf(load.profile.uppercase())
        )
    }
    
    fun toPerformanceThresholds(): PerformanceThresholds? {
        return thresholds?.let {
            PerformanceThresholds(
                maxResponseTime = it.maxResponseTime?.let { d -> parseDuration(d) },
                p50ResponseTime = it.p50ResponseTime?.let { d -> parseDuration(d) },
                p90ResponseTime = it.p90ResponseTime?.let { d -> parseDuration(d) },
                p95ResponseTime = it.p95ResponseTime?.let { d -> parseDuration(d) },
                p99ResponseTime = it.p99ResponseTime?.let { d -> parseDuration(d) },
                minRequestsPerSecond = it.minRequestsPerSecond,
                maxErrorRate = it.maxErrorRate ?: 0.05,
                minSuccessRate = it.minSuccessRate ?: 0.95
            )
        }
    }
    
    private fun parseDuration(duration: String): Duration {
        val regex = Regex("""(\d+)(s|m|h)""")
        val match = regex.find(duration) ?: throw IllegalArgumentException("Invalid duration: $duration")
        
        val value = match.groupValues[1].toInt()
        val unit = match.groupValues[2]
        
        return when (unit) {
            "s" -> value.seconds
            "m" -> value.minutes
            "h" -> (value * 60).minutes
            else -> throw IllegalArgumentException("Invalid duration unit: $unit")
        }
    }
}

data class LoadConfigSpec(
    val users: Int,
    val duration: String,
    val rampUp: String,
    val profile: String = "constant",
    val requestsPerSecond: Int? = null,
    val thinkTime: String? = null,
    val maxResponseTime: String? = null,
    val successRateThreshold: Double? = null,
    val reportInterval: String? = null
)

data class LoadScenarioSpec(
    val name: String,
    val weight: Double = 1.0,
    val enabled: Boolean = true
)

data class PerformanceThresholdsSpec(
    val maxResponseTime: String? = null,
    val p50ResponseTime: String? = null,
    val p90ResponseTime: String? = null,
    val p95ResponseTime: String? = null,
    val p99ResponseTime: String? = null,
    val minRequestsPerSecond: Int? = null,
    val maxErrorRate: Double? = null,
    val minSuccessRate: Double? = null
)
