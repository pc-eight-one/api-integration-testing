package dev.codersbox.eng.lib.cli.output

import dev.codersbox.eng.lib.cli.execution.ExecutionResult
import dev.codersbox.eng.lib.cli.execution.ExecutionStatus
import dev.codersbox.eng.lib.cli.execution.ExecutionSummary
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class JUnitReporter {
    fun generate(summary: ExecutionSummary, outputDir: String) {
        val dir = File(outputDir)
        dir.mkdirs()

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.newDocument()

        // Create testsuite element
        val testsuiteElement = doc.createElement("testsuite").apply {
            setAttribute("name", "API Integration Tests")
            setAttribute("tests", summary.total.toString())
            setAttribute("failures", summary.failed.toString())
            setAttribute("skipped", summary.skipped.toString())
            setAttribute("time", (summary.duration / 1000.0).toString())
            setAttribute("timestamp", java.time.Instant.now().toString())
        }
        doc.appendChild(testsuiteElement)

        // Add test cases
        summary.results.forEach { result ->
            val testcaseElement = doc.createElement("testcase").apply {
                setAttribute("name", result.scenario)
                setAttribute("classname", "APITests")
                setAttribute("time", (result.duration / 1000.0).toString())
            }

            when (result.status) {
                ExecutionStatus.FAILED -> {
                    val failureElement = doc.createElement("failure").apply {
                        setAttribute("message", result.error?.message ?: "Test failed")
                        setAttribute("type", result.error?.javaClass?.name ?: "AssertionError")
                        textContent = result.error?.stackTraceToString() ?: ""
                    }
                    testcaseElement.appendChild(failureElement)
                }
                ExecutionStatus.SKIPPED -> {
                    val skippedElement = doc.createElement("skipped")
                    testcaseElement.appendChild(skippedElement)
                }
                else -> {}
            }

            testsuiteElement.appendChild(testcaseElement)
        }

        // Write to file
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        }

        val outputFile = File(dir, "TEST-results.xml")
        transformer.transform(DOMSource(doc), StreamResult(outputFile))

        println("${ConsoleColors.GREEN}✓${ConsoleColors.RESET} JUnit report generated: ${outputFile.absolutePath}")
    }
}
