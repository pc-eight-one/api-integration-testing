package dev.codersbox.eng.lib.api.testing.chaining

import dev.codersbox.eng.lib.api.testing.dsl.Scenario
import dev.codersbox.eng.lib.api.testing.context.ScenarioContext
import dev.codersbox.eng.lib.api.testing.dsl.ApiTestSuite
import dev.codersbox.eng.lib.api.testing.config.ApiTestConfig

/**
 * Scenario chaining allows executing multiple scenarios in sequence
 * with shared context between them
 */
class ScenarioChain(val name: String, private val config: ApiTestConfig) {
    private val scenarios = mutableListOf<ChainedScenario>()
    private val sharedContext = ScenarioContext()
    
    /**
     * Add a scenario to the chain
     */
    fun scenario(name: String, block: suspend Scenario.() -> Unit): ScenarioChain {
        scenarios.add(ChainedScenario(name, block))
        return this
    }
    
    /**
     * Execute all scenarios in the chain
     */
    suspend fun execute() {
        scenarios.forEach { chainedScenario ->
            val scenario = Scenario(
                chainedScenario.name,
                sharedContext,
                config
            )
            chainedScenario.block(scenario)
        }
    }
    
    /**
     * Get the shared context
     */
    fun getSharedContext(): ScenarioContext = sharedContext
    
    private data class ChainedScenario(
        val name: String,
        val block: suspend Scenario.() -> Unit
    )
}

/**
 * Scenario pipeline for complex workflows
 */
class ScenarioPipeline(val name: String, private val config: ApiTestConfig) {
    private val stages = mutableListOf<PipelineStage>()
    private val sharedContext = ScenarioContext()
    
    /**
     * Add a stage to the pipeline
     */
    fun stage(
        name: String,
        condition: (ScenarioContext) -> Boolean = { true },
        block: suspend Scenario.() -> Unit
    ): ScenarioPipeline {
        stages.add(PipelineStage(name, condition, block))
        return this
    }
    
    /**
     * Execute the pipeline
     */
    suspend fun execute() {
        for (stage in stages) {
            if (stage.condition(sharedContext)) {
                println("Executing pipeline stage: ${stage.name}")
                val scenario = Scenario(
                    stage.name,
                    sharedContext,
                    config
                )
                stage.block(scenario)
            } else {
                println("Skipping pipeline stage: ${stage.name} (condition not met)")
            }
        }
    }
    
    /**
     * Get the shared context
     */
    fun getSharedContext(): ScenarioContext = sharedContext
    
    private data class PipelineStage(
        val name: String,
        val condition: (ScenarioContext) -> Boolean,
        val block: suspend Scenario.() -> Unit
    )
}

/**
 * Scenario dependency graph for managing complex dependencies
 */
class ScenarioDependencyGraph(val name: String, private val config: ApiTestConfig) {
    private val nodes = mutableMapOf<String, GraphNode>()
    private val executedScenarios = mutableSetOf<String>()
    private val sharedContext = ScenarioContext()
    
    /**
     * Add a scenario node
     */
    fun scenario(
        name: String,
        dependsOn: List<String> = emptyList(),
        block: suspend Scenario.() -> Unit
    ): ScenarioDependencyGraph {
        nodes[name] = GraphNode(name, dependsOn.toSet(), block)
        return this
    }
    
    /**
     * Execute scenarios in dependency order
     */
    suspend fun execute() {
        val executionOrder = topologicalSort()
        
        for (scenarioName in executionOrder) {
            val node = nodes[scenarioName]!!
            println("Executing scenario: $scenarioName")
            
            val scenario = Scenario(
                node.name,
                sharedContext,
                config
            )
            node.block(scenario)
            executedScenarios.add(scenarioName)
        }
    }
    
    /**
     * Topological sort to determine execution order
     */
    private fun topologicalSort(): List<String> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<String>()
        
        fun visit(name: String) {
            if (name in visited) return
            
            val node = nodes[name] 
                ?: throw IllegalArgumentException("Scenario '$name' not found")
            
            // Visit dependencies first
            node.dependsOn.forEach { visit(it) }
            
            visited.add(name)
            result.add(name)
        }
        
        nodes.keys.forEach { visit(it) }
        return result
    }
    
    private data class GraphNode(
        val name: String,
        val dependsOn: Set<String>,
        val block: suspend Scenario.() -> Unit
    )
}

/**
 * Scenario workflow for business process testing
 */
class ScenarioWorkflow(val name: String, private val config: ApiTestConfig) {
    private val steps = mutableListOf<WorkflowStep>()
    private val sharedContext = ScenarioContext()
    
    /**
     * Add a workflow step
     */
    fun step(
        name: String,
        required: Boolean = true,
        onError: ErrorHandling = ErrorHandling.FAIL,
        block: suspend Scenario.() -> Unit
    ): ScenarioWorkflow {
        steps.add(WorkflowStep(name, required, onError, block))
        return this
    }
    
    /**
     * Execute the workflow
     */
    suspend fun execute() {
        for (step in steps) {
            try {
                println("Executing workflow step: ${step.name}")
                val scenario = Scenario(
                    step.name,
                    sharedContext,
                    config
                )
                step.block(scenario)
            } catch (e: Exception) {
                when (step.onError) {
                    ErrorHandling.FAIL -> throw e
                    ErrorHandling.SKIP -> {
                        println("Step ${step.name} failed, skipping (${e.message})")
                        if (step.required) {
                            throw IllegalStateException("Required step failed: ${step.name}", e)
                        }
                    }
                    ErrorHandling.CONTINUE -> {
                        println("Step ${step.name} failed, continuing (${e.message})")
                    }
                }
            }
        }
    }
    
    /**
     * Get the shared context
     */
    fun getSharedContext(): ScenarioContext = sharedContext
    
    enum class ErrorHandling {
        FAIL,      // Fail immediately
        SKIP,      // Skip remaining steps
        CONTINUE   // Continue to next step
    }
    
    private data class WorkflowStep(
        val name: String,
        val required: Boolean,
        val onError: ErrorHandling,
        val block: suspend Scenario.() -> Unit
    )
}

/**
 * DSL extension for scenario chaining
 */
fun ApiTestSuite.scenarioChain(name: String, block: ScenarioChain.() -> Unit): ScenarioChain {
    return ScenarioChain(name, this.config).apply(block)
}

/**
 * DSL extension for scenario pipeline
 */
fun ApiTestSuite.scenarioPipeline(name: String, block: ScenarioPipeline.() -> Unit): ScenarioPipeline {
    return ScenarioPipeline(name, this.config).apply(block)
}

/**
 * DSL extension for scenario dependency graph
 */
fun ApiTestSuite.scenarioDependencyGraph(name: String, block: ScenarioDependencyGraph.() -> Unit): ScenarioDependencyGraph {
    return ScenarioDependencyGraph(name, this.config).apply(block)
}

/**
 * DSL extension for scenario workflow
 */
fun ApiTestSuite.scenarioWorkflow(name: String, block: ScenarioWorkflow.() -> Unit): ScenarioWorkflow {
    return ScenarioWorkflow(name, this.config).apply(block)
}
