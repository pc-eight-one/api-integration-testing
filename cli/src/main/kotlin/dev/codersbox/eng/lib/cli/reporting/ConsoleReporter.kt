package dev.codersbox.eng.lib.cli.reporting

import dev.codersbox.eng.lib.cli.execution.ExecutionResult

class ConsoleReporter(
    private val verbose: Boolean = false,
    private val colorOutput: Boolean = true
) {
    private val colors = if (colorOutput) ColorScheme.ANSI else ColorScheme.NONE

    fun reportStart(suiteCount: Int, scenarioCount: Int) {
        println()
        println("${colors.cyan}╔════════════════════════════════════════════════════════════════╗${colors.reset}")
        println("${colors.cyan}║${colors.reset}   ${colors.bold}API Integration Test Execution${colors.reset}                        ${colors.cyan}║${colors.reset}")
        println("${colors.cyan}╠════════════════════════════════════════════════════════════════╣${colors.reset}")
        println("${colors.cyan}║${colors.reset}  Test Suites:  ${colors.bold}$suiteCount${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        println("${colors.cyan}║${colors.reset}  Scenarios:    ${colors.bold}$scenarioCount${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        println("${colors.cyan}╚════════════════════════════════════════════════════════════════╝${colors.reset}")
        println()
    }

    fun reportScenarioStart(suiteName: String, scenarioName: String) {
        if (verbose) {
            println("${colors.blue}▶${colors.reset} ${colors.bold}$suiteName${colors.reset} › $scenarioName")
        }
    }

    fun reportStepStart(stepName: String) {
        if (verbose) {
            print("  ${colors.dim}┌${colors.reset} $stepName ... ")
        }
    }

    fun reportStepSuccess(stepName: String, durationMs: Long) {
        if (verbose) {
            println("${colors.green}✓${colors.reset} ${colors.dim}(${durationMs}ms)${colors.reset}")
        }
    }

    fun reportStepFailure(stepName: String, error: String, durationMs: Long) {
        if (verbose) {
            println("${colors.red}✗${colors.reset} ${colors.dim}(${durationMs}ms)${colors.reset}")
            println("  ${colors.dim}└${colors.reset} ${colors.red}Error: $error${colors.reset}")
        }
    }

    fun reportScenarioComplete(scenarioName: String, success: Boolean, durationMs: Long) {
        val status = if (success) {
            "${colors.green}✓ PASS${colors.reset}"
        } else {
            "${colors.red}✗ FAIL${colors.reset}"
        }
        if (verbose) {
            println("  $status ${colors.dim}($durationMs ms)${colors.reset}")
            println()
        } else {
            print(if (success) "${colors.green}.${colors.reset}" else "${colors.red}F${colors.reset}")
        }
    }

    fun reportSummary(results: List<ExecutionResult>) {
        println()
        println()
        
        val total = results.size
        val passed = results.count { it.success }
        val failed = results.count { !it.success && it.error != null }
        val skipped = results.count { !it.success && it.error == null }
        val totalDuration = results.sumOf { it.durationMs }
        val successRate = if (total > 0) (passed * 100.0 / total) else 0.0

        println("${colors.cyan}╔════════════════════════════════════════════════════════════════╗${colors.reset}")
        println("${colors.cyan}║${colors.reset}   ${colors.bold}Test Results Summary${colors.reset}                                  ${colors.cyan}║${colors.reset}")
        println("${colors.cyan}╠════════════════════════════════════════════════════════════════╣${colors.reset}")
        println("${colors.cyan}║${colors.reset}  Total:         ${colors.bold}$total${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        println("${colors.cyan}║${colors.reset}  ${colors.green}Passed:${colors.reset}        ${colors.green}$passed${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        
        if (failed > 0) {
            println("${colors.cyan}║${colors.reset}  ${colors.red}Failed:${colors.reset}        ${colors.red}$failed${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        }
        
        if (skipped > 0) {
            println("${colors.cyan}║${colors.reset}  ${colors.yellow}Skipped:${colors.reset}       ${colors.yellow}$skipped${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        }
        
        println("${colors.cyan}║${colors.reset}  Success Rate:  ${colors.bold}${"%.1f".format(successRate)}%${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        println("${colors.cyan}║${colors.reset}  Duration:      ${colors.bold}${totalDuration}ms${colors.reset}".padEnd(66) + "${colors.cyan}║${colors.reset}")
        println("${colors.cyan}╚════════════════════════════════════════════════════════════════╝${colors.reset}")
        println()

        if (failed > 0) {
            reportFailures(results.filter { !it.success && it.error != null })
        }
    }

    private fun reportFailures(failures: List<ExecutionResult>) {
        println()
        println("${colors.red}${colors.bold}Failed Tests:${colors.reset}")
        println()
        
        failures.forEach { result ->
            println("${colors.red}✗${colors.reset} ${colors.bold}${result.scenarioName}${colors.reset} › ${result.stepName}")
            println("  ${colors.red}${result.error}${colors.reset}")
            println()
        }
    }

    fun reportProgress(current: Int, total: Int, scenarioName: String) {
        val percentage = (current * 100) / total
        val progressBar = buildProgressBar(percentage, 30)
        print("\r${colors.dim}[$progressBar] $percentage% - $scenarioName${colors.reset}".padEnd(80))
    }

    private fun buildProgressBar(percentage: Int, width: Int): String {
        val filled = (percentage * width) / 100
        val empty = width - filled
        return buildString {
            append(colors.green)
            repeat(filled) { append("█") }
            append(colors.dim)
            repeat(empty) { append("░") }
            append(colors.reset)
        }
    }
}

private object ColorScheme {
    object ANSI {
        const val reset = "\u001B[0m"
        const val bold = "\u001B[1m"
        const val dim = "\u001B[2m"
        const val red = "\u001B[31m"
        const val green = "\u001B[32m"
        const val yellow = "\u001B[33m"
        const val blue = "\u001B[34m"
        const val cyan = "\u001B[36m"
    }

    object NONE {
        const val reset = ""
        const val bold = ""
        const val dim = ""
        const val red = ""
        const val green = ""
        const val yellow = ""
        const val blue = ""
        const val cyan = ""
    }
}
