package dev.codersbox.eng.lib.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import dev.codersbox.eng.lib.cli.commands.*

class ApiTestCli : CliktCommand(
    name = "api-test",
    help = "API Integration Testing Framework CLI - Run, monitor, and report on API tests"
) {
    override fun run() = Unit
}

fun main(args: Array<String>) = ApiTestCli()
    .subcommands(
        RunCommand(),
        WatchCommand(),
        ServeCommand(),
        ReportCommand(),
        CiCommand(),
        DiffCommand(),
        GenerateCommand()
    )
    .main(args)
