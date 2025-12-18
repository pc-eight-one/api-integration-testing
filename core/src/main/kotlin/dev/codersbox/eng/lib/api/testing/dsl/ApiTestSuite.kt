package dev.codersbox.eng.lib.api.testing.dsl

import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig
import dev.codersbox.eng.lib.api.testing.context.ScenarioContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.names.TestName

/**
 * API Test Suite that groups related scenarios
 */
abstract class ApiTestSuite(
    private val suiteName: String,
    internal val config: ApiTestConfig = ApiTestConfig.get()
) : FunSpec() {

    /**
     * Define a test scenario
     */
    fun scenario(description: String, block: suspend Scenario.() -> Unit) {
        test(description) {
            val scenario = Scenario(description, ScenarioContext(), config)
            scenario.block()
        }
    }

    /**
     * Define a test scenario with tags
     */
    fun scenario(description: String, tags: Set<String>, block: suspend Scenario.() -> Unit) {
        test(description) {
            val scenario = Scenario(description, ScenarioContext(), config)
            scenario.block()
        }
    }
}
