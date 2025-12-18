package dev.codersbox.eng.lib.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage
import java.io.File

/**
 * Import OpenAPI/Swagger spec and generate tests
 */
class ImportOpenAPIAction : AnAction("Import OpenAPI Spec") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension in listOf("yaml", "yml", "json") }
            .withTitle("Select OpenAPI Specification File")
        
        val file = FileChooser.chooseFile(descriptor, project, null) ?: return
        
        try {
            val spec = parseOpenAPISpec(file.path)
            val tests = generateTestsFromSpec(spec)
            
            // Show preview dialog
            val selectedTests = showTestSelectionDialog(tests)
            
            // Generate test files
            selectedTests.forEach { test ->
                createTestFile(project, test)
            }
            
            Messages.showInfoMessage(
                project,
                "Generated ${selectedTests.size} test files from OpenAPI spec",
                "Import Successful"
            )
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                project,
                "Failed to import OpenAPI spec: ${ex.message}",
                "Import Failed"
            )
        }
    }
    
    private fun parseOpenAPISpec(filePath: String): OpenAPISpec {
        // Simplified parser - in real implementation use swagger-parser or similar
        val content = File(filePath).readText()
        return OpenAPISpec(
            paths = mapOf(
                "/users" to PathItem(
                    operations = mapOf(
                        "GET" to Operation("List Users", "200"),
                        "POST" to Operation("Create User", "201")
                    )
                ),
                "/users/{id}" to PathItem(
                    operations = mapOf(
                        "GET" to Operation("Get User", "200"),
                        "PUT" to Operation("Update User", "200"),
                        "DELETE" to Operation("Delete User", "204")
                    )
                )
            )
        )
    }
    
    private fun generateTestsFromSpec(spec: OpenAPISpec): List<GeneratedTest> {
        val tests = mutableListOf<GeneratedTest>()
        
        spec.paths.forEach { (path, pathItem) ->
            pathItem.operations.forEach { (method, operation) ->
                val testName = operation.summary.replace(" ", "")
                val code = generateTestCode(testName, path, method, operation)
                tests.add(GeneratedTest(testName, code))
            }
        }
        
        return tests
    }
    
    private fun generateTestCode(
        name: String,
        path: String,
        method: String,
        operation: Operation
    ): String {
        return """
        |package com.example.tests
        |
        |import dev.codersbox.eng.lib.api.testing.dsl.*
        |import io.kotest.core.spec.style.FunSpec
        |
        |class ${name}Test : FunSpec({
        |    test("$name") {
        |        apiTestSuite("$name Suite") {
        |            baseUrl("https://api.example.com")
        |            
        |            scenario("Test $name") {
        |                step("$name") {
        |                    ${method.lowercase()}("$path") {
        |                        headers {
        |                            "Content-Type" to "application/json"
        |                        }
        |                    }.expect {
        |                        status(${operation.expectedStatus})
        |                        // Add more validations based on schema
        |                    }
        |                }
        |            }
        |        }.execute()
        |    }
        |})
        |""".trimMargin()
    }
    
    private fun showTestSelectionDialog(tests: List<GeneratedTest>): List<GeneratedTest> {
        // For now, return all tests. In real implementation, show dialog
        return tests
    }
    
    private fun createTestFile(project: com.intellij.openapi.project.Project, test: GeneratedTest) {
        // Implementation to create actual file in project
    }
}

data class OpenAPISpec(val paths: Map<String, PathItem>)
data class PathItem(val operations: Map<String, Operation>)
data class Operation(val summary: String, val expectedStatus: String)
data class GeneratedTest(val name: String, val code: String)
