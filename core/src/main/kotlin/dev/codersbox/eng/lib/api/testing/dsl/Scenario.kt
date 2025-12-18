package dev.codersbox.eng.lib.api.testing.dsl

import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.context.ScenarioContext
import org.slf4j.LoggerFactory

/**
 * Represents a test scenario containing multiple steps
 */
class Scenario(
    val name: String,
    val context: ScenarioContext,
    val config: ApiTestConfig
) {
    private val logger = LoggerFactory.getLogger(Scenario::class.java)
    private val steps = mutableListOf<String>()

    /**
     * Execute a test step
     */
    suspend fun step(stepName: String, block: suspend StepContext.() -> Unit) {
        logger.info("Executing step: $stepName")
        steps.add(stepName)
        
        val stepContext = StepContext(context, config)
        try {
            stepContext.block()
            logger.info("Step completed: $stepName")
        } catch (e: Exception) {
            logger.error("Step failed: $stepName", e)
            throw e
        }
    }

    /**
     * Get the list of executed steps
     */
    fun getExecutedSteps(): List<String> = steps.toList()
}
