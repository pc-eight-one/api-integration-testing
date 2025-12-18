package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import java.net.ServerSocket
import java.nio.file.Files

class ServeCommand : CliktCommand(
    name = "serve",
    help = "Start a web server to view test reports"
) {
    private val port by option("-p", "--port", help = "Port to serve on")
        .int()
        .default(8080)

    private val directory by option("-d", "--dir", help = "Directory containing reports")
        .file(mustExist = true, canBeFile = false, mustBeReadable = true)

    override fun run() {
        val reportDir = directory ?: File("./reports")
        
        if (!reportDir.exists()) {
            echo("Error: Report directory not found: ${reportDir.absolutePath}", err = true)
            return
        }

        echo("🚀 Starting web server...")
        echo("   Directory: ${reportDir.absolutePath}")
        echo("   URL: http://localhost:$port")
        echo("   Press Ctrl+C to stop")
        echo()

        try {
            ServerSocket(port).use { serverSocket ->
                echo("✓ Server started successfully!")
                echo("   Open http://localhost:$port in your browser")
                echo()

                while (true) {
                    val clientSocket = serverSocket.accept()
                    
                    Thread {
                        handleClient(clientSocket, reportDir)
                    }.start()
                }
            }
        } catch (e: Exception) {
            echo("Error starting server: ${e.message}", err = true)
        }
    }

    private fun handleClient(socket: java.net.Socket, reportDir: File) {
        try {
            socket.use { client ->
                val input = client.getInputStream().bufferedReader()
                val output = client.getOutputStream()

                // Read HTTP request
                val requestLine = input.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return

                val method = parts[0]
                val path = parts[1].removePrefix("/")

                // Consume the rest of the request
                while (input.ready() && input.readLine().isNotEmpty()) {
                    // Skip headers
                }

                if (method != "GET") {
                    sendResponse(output, 405, "text/plain", "Method Not Allowed".toByteArray())
                    return
                }

                // Serve file
                val file = if (path.isEmpty() || path == "/") {
                    findIndexFile(reportDir)
                } else {
                    File(reportDir, path)
                }

                if (file == null || !file.exists() || !file.isFile) {
                    val notFoundHtml = """
                        <!DOCTYPE html>
                        <html>
                        <head><title>404 Not Found</title></head>
                        <body>
                            <h1>404 Not Found</h1>
                            <p>The requested file was not found.</p>
                        </body>
                        </html>
                    """.trimIndent()
                    sendResponse(output, 404, "text/html", notFoundHtml.toByteArray())
                    return
                }

                val contentType = getContentType(file.extension)
                val content = Files.readAllBytes(file.toPath())
                sendResponse(output, 200, contentType, content)
            }
        } catch (e: Exception) {
            // Client disconnected or error occurred
        }
    }

    private fun findIndexFile(dir: File): File? {
        val indexFiles = listOf("index.html", "index.htm", "report.html")
        return indexFiles
            .map { File(dir, it) }
            .firstOrNull { it.exists() && it.isFile }
    }

    private fun sendResponse(output: java.io.OutputStream, statusCode: Int, contentType: String, content: ByteArray) {
        val statusText = when (statusCode) {
            200 -> "OK"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            else -> "Unknown"
        }

        val response = buildString {
            appendLine("HTTP/1.1 $statusCode $statusText")
            appendLine("Content-Type: $contentType")
            appendLine("Content-Length: ${content.size}")
            appendLine("Connection: close")
            appendLine()
        }

        output.write(response.toByteArray())
        output.write(content)
        output.flush()
    }

    private fun getContentType(extension: String): String = when (extension.lowercase()) {
        "html", "htm" -> "text/html"
        "css" -> "text/css"
        "js" -> "application/javascript"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        else -> "application/octet-stream"
    }
}
