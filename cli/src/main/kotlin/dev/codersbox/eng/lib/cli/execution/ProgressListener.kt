package dev.codersbox.eng.lib.cli.execution

import dev.codersbox.eng.lib.cli.output.ConsoleColors
import java.util.concurrent.atomic.AtomicInteger

class ProgressListener(
    private val totalTests: Int,
    private val verbose: Boolean = false
) : ExecutionListener {
    private val completed = AtomicInteger(0)
    private val startTime = System.currentTimeMillis()

    override fun onExecutionStart() {
        println("\n${ConsoleColors.BLUE}Starting test execution...${ConsoleColors.RESET}")
        println("Total tests: $totalTests\n")
    }

    override fun onScenarioStart(name: String) {
        if (verbose) {
            println("${ConsoleColors.CYAN}▶ Running: $name${ConsoleColors.RESET}")
        }
    }

    override fun onScenarioComplete(name: String, result: ExecutionResult) {
        val count = completed.incrementAndGet()
        val percentage = (count * 100) / totalTests
        
        val status = when (result.status) {
            ExecutionStatus.PASSED -> "${ConsoleColors.GREEN}✓${ConsoleColors.RESET}"
            ExecutionStatus.FAILED -> "${ConsoleColors.RED}✗${ConsoleColors.RESET}"
            ExecutionStatus.SKIPPED -> "${ConsoleColors.YELLOW}⊘${ConsoleColors.RESET}"
            else -> "?"
        }
        
        if (verbose) {
            println("$status $name (${result.duration}ms)")
        } else {
            print("\rProgress: [${"=".repeat(percentage / 2)}${" ".repeat(50 - percentage / 2)}] $percentage% ($count/$totalTests)")
        }
    }

    override fun onExecutionComplete(summary: ExecutionSummary) {
        if (!verbose) {
            println() // New line after progress bar
        }
        
        val duration = System.currentTimeMillis() - startTime
        println("\n${ConsoleColors.BLUE}═════════════════════════════════════════════${ConsoleColors.RESET}")
        println("${ConsoleColors.BOLD}Test Execution Summary${ConsoleColors.RESET}")
        println("${ConsoleColors.BLUE}═════════════════════════════════════════════${ConsoleColors.RESET}")
        println("Total:   ${summary.total}")
        println("${ConsoleColors.GREEN}Passed:  ${summary.passed}${ConsoleColors.RESET}")
        println("${ConsoleColors.RED}Failed:  ${summary.failed}${ConsoleColors.RESET}")
        println("${ConsoleColors.YELLOW}Skipped: ${summary.skipped}${ConsoleColors.RESET}")
        println("Duration: ${formatDuration(duration)}")
        println("${ConsoleColors.BLUE}═════════════════════════════════════════════${ConsoleColors.RESET}\n")
    }

    private fun formatDuration(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val remainingMillis = millis % 1000
        
        return when {
            minutes > 0 -> "${minutes}m ${remainingSeconds}s"
            seconds > 0 -> "${seconds}s ${remainingMillis}ms"
            else -> "${millis}ms"
        }
    }
}
