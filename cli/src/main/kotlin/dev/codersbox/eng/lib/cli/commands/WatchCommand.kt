package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.nio.file.*
import java.util.concurrent.TimeUnit

class WatchCommand : CliktCommand(
    name = "watch",
    help = "Watch for file changes and re-run tests automatically"
) {
    private val path by option("-p", "--path", help = "Path to watch for changes")
        .file(mustExist = true, canBeFile = false, mustBeReadable = true)

    private val pattern by option("--pattern", help = "File pattern to watch (e.g., *.kt)")
        .default("*.kt")

    private val debounce by option("-d", "--debounce", help = "Debounce delay in milliseconds")
        .int()
        .default(500)

    private val tags by option("-t", "--tags", help = "Tags to filter scenarios")

    override fun run() {
        val watchPath = path ?: File(".").absoluteFile
        
        echo("👀 Watching ${watchPath.absolutePath} for changes...")
        echo("   Pattern: $pattern")
        echo("   Debounce: ${debounce}ms")
        echo("   Press Ctrl+C to stop")
        echo()

        val watchService = FileSystems.getDefault().newWatchService()
        registerDirectoryTree(watchPath.toPath(), watchService)

        var lastExecutionTime = 0L
        var pendingExecution = false

        try {
            while (true) {
                val key = watchService.poll(100, TimeUnit.MILLISECONDS)
                
                if (key != null) {
                    for (event in key.pollEvents()) {
                        val kind = event.kind()
                        
                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue
                        }

                        val fileName = (event.context() as Path).toString()
                        
                        if (matchesPattern(fileName, pattern)) {
                            echo("📝 File changed: $fileName")
                            pendingExecution = true
                        }
                    }
                    
                    key.reset()
                }

                // Execute tests if pending and debounce time has passed
                if (pendingExecution) {
                    val now = System.currentTimeMillis()
                    if (now - lastExecutionTime > debounce) {
                        echo()
                        echo("🔄 Re-running tests...")
                        echo()
                        
                        runTests(tags)
                        
                        lastExecutionTime = now
                        pendingExecution = false
                        
                        echo()
                        echo("✓ Tests completed. Watching for changes...")
                        echo()
                    }
                }
            }
        } catch (e: InterruptedException) {
            echo("Watch stopped.")
        } finally {
            watchService.close()
        }
    }

    private fun registerDirectoryTree(start: Path, watchService: WatchService) {
        Files.walkFileTree(start, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: java.nio.file.attribute.BasicFileAttributes): FileVisitResult {
                dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                )
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun matchesPattern(fileName: String, pattern: String): Boolean {
        val regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
            .toRegex()
        return fileName.matches(regex)
    }

    private fun runTests(tags: String?) {
        val command = buildList {
            add("run")
            if (tags != null) {
                add("--tags")
                add(tags)
            }
        }
        
        // Execute the run command
        try {
            val processBuilder = ProcessBuilder("api-test", *command.toTypedArray())
            processBuilder.inheritIO()
            val process = processBuilder.start()
            process.waitFor()
        } catch (e: Exception) {
            echo("Error running tests: ${e.message}", err = true)
        }
    }
}
