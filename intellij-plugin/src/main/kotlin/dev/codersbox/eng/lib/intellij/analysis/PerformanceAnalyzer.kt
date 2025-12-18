package dev.codersbox.eng.lib.intellij.analysis

import com.intellij.openapi.project.Project
import javax.swing.*
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.data.category.DefaultCategoryDataset

/**
 * Analyze and visualize API performance metrics
 */
class PerformanceAnalyzer(private val project: Project) {
    
    private val metrics = mutableListOf<PerformanceMetric>()
    
    fun recordMetric(metric: PerformanceMetric) {
        metrics.add(metric)
    }
    
    fun createPerformancePanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Response time chart
        panel.add(createResponseTimeChart())
        
        // Throughput chart
        panel.add(createThroughputChart())
        
        // Statistics panel
        panel.add(createStatisticsPanel())
        
        return panel
    }
    
    private fun createResponseTimeChart(): ChartPanel {
        val dataset = DefaultCategoryDataset()
        
        metrics.groupBy { it.endpoint }.forEach { (endpoint, endpointMetrics) ->
            val avgTime = endpointMetrics.map { it.responseTime }.average()
            dataset.addValue(avgTime, "Response Time", endpoint)
        }
        
        val chart = ChartFactory.createBarChart(
            "Average Response Times",
            "Endpoint",
            "Time (ms)",
            dataset
        )
        
        return ChartPanel(chart)
    }
    
    private fun createThroughputChart(): ChartPanel {
        val dataset = DefaultCategoryDataset()
        
        val requestsPerSecond = metrics
            .groupBy { it.timestamp / 1000 }
            .mapValues { it.value.size }
        
        requestsPerSecond.forEach { (second, count) ->
            dataset.addValue(count.toDouble(), "Requests", "T+${second}s")
        }
        
        val chart = ChartFactory.createLineChart(
            "Throughput Over Time",
            "Time",
            "Requests/sec",
            dataset
        )
        
        return ChartPanel(chart)
    }
    
    private fun createStatisticsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createTitledBorder("Statistics")
        
        val stats = calculateStatistics()
        
        panel.add(JLabel("Total Requests: ${stats.totalRequests}"))
        panel.add(JLabel("Avg Response Time: ${String.format("%.2f", stats.avgResponseTime)} ms"))
        panel.add(JLabel("Min Response Time: ${stats.minResponseTime} ms"))
        panel.add(JLabel("Max Response Time: ${stats.maxResponseTime} ms"))
        panel.add(JLabel("P95 Response Time: ${stats.p95ResponseTime} ms"))
        panel.add(JLabel("P99 Response Time: ${stats.p99ResponseTime} ms"))
        panel.add(JLabel("Success Rate: ${String.format("%.2f", stats.successRate)}%"))
        panel.add(JLabel("Error Rate: ${String.format("%.2f", stats.errorRate)}%"))
        
        return panel
    }
    
    private fun calculateStatistics(): Statistics {
        val responseTimes = metrics.map { it.responseTime }.sorted()
        val successCount = metrics.count { it.statusCode in 200..299 }
        
        return Statistics(
            totalRequests = metrics.size,
            avgResponseTime = responseTimes.average(),
            minResponseTime = responseTimes.minOrNull() ?: 0,
            maxResponseTime = responseTimes.maxOrNull() ?: 0,
            p95ResponseTime = percentile(responseTimes, 95.0),
            p99ResponseTime = percentile(responseTimes, 99.0),
            successRate = if (metrics.isNotEmpty()) {
                (successCount.toDouble() / metrics.size) * 100
            } else 0.0,
            errorRate = if (metrics.isNotEmpty()) {
                ((metrics.size - successCount).toDouble() / metrics.size) * 100
            } else 0.0
        )
    }
    
    private fun percentile(sorted: List<Long>, percentile: Double): Long {
        if (sorted.isEmpty()) return 0
        val index = ((percentile / 100.0) * sorted.size).toInt()
        return sorted[index.coerceIn(0, sorted.lastIndex)]
    }
}

data class PerformanceMetric(
    val endpoint: String,
    val method: String,
    val statusCode: Int,
    val responseTime: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class Statistics(
    val totalRequests: Int,
    val avgResponseTime: Double,
    val minResponseTime: Long,
    val maxResponseTime: Long,
    val p95ResponseTime: Long,
    val p99ResponseTime: Long,
    val successRate: Double,
    val errorRate: Double
)
