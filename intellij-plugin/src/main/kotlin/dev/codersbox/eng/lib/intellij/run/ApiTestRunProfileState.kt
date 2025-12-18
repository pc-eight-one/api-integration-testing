package dev.codersbox.eng.lib.intellij.run

import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ProgramRunner

class ApiTestRunProfileState(
    environment: ExecutionEnvironment,
    private val configuration: ApiTestRunConfiguration
) : CommandLineState(environment) {
    
    override fun startProcess(): ProcessHandler {
        val commandLine = createCommandLine()
        val processHandler = OSProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }
    
    private fun createCommandLine(): GeneralCommandLine {
        val commandLine = GeneralCommandLine()
        commandLine.exePath = "mvn"
        
        when (configuration.testType) {
            ApiTestRunConfiguration.TestType.SCENARIO -> {
                commandLine.addParameter("test")
                commandLine.addParameter("-Dtest=${configuration.testPath}")
            }
            ApiTestRunConfiguration.TestType.SUITE -> {
                commandLine.addParameter("test")
                if (configuration.suiteName.isNotEmpty()) {
                    commandLine.addParameter("-Dtest=*${configuration.suiteName}*")
                }
            }
            ApiTestRunConfiguration.TestType.LOAD_TEST -> {
                commandLine.addParameter("test")
                commandLine.addParameter("-Dtest=${configuration.testPath}")
                commandLine.addParameter("-DloadTest=true")
            }
        }
        
        commandLine.workDirectory = environment.project.basePath
        return commandLine
    }
}
