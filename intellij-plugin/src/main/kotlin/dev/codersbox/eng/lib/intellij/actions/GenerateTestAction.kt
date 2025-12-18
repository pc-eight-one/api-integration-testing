package dev.codersbox.eng.lib.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import javax.swing.*

/**
 * Action to generate API test from template
 */
class GenerateTestAction : AnAction("Generate API Test") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        val dialog = TestGeneratorDialog(project)
        if (dialog.showAndGet()) {
            val testCode = generateTestCode(
                dialog.testName,
                dialog.testType,
                dialog.endpoint,
                dialog.method
            )
            
            WriteCommandAction.runWriteCommandAction(project) {
                val document = editor.document
                document.insertString(editor.caretModel.offset, testCode)
            }
        }
    }
    
    private fun generateTestCode(
        name: String,
        type: String,
        endpoint: String,
        method: String
    ): String {
        return when (type) {
            "REST" -> generateRestTest(name, endpoint, method)
            "GraphQL" -> generateGraphQLTest(name, endpoint)
            "Load Test" -> generateLoadTest(name, endpoint, method)
            else -> generateRestTest(name, endpoint, method)
        }
    }
    
    private fun generateRestTest(name: String, endpoint: String, method: String): String {
        return """
        |
        |test("$name") {
        |    apiTestSuite("$name Suite") {
        |        baseUrl("https://api.example.com")
        |        
        |        scenario("Test $name") {
        |            step("Call $endpoint") {
        |                ${method.lowercase()}("$endpoint") {
        |                    headers {
        |                        "Content-Type" to "application/json"
        |                    }
        |                }.expect {
        |                    status(200)
        |                    jsonPath("$.status") equals "success"
        |                }
        |            }
        |        }
        |    }.execute()
        |}
        |""".trimMargin()
    }
    
    private fun generateGraphQLTest(name: String, endpoint: String): String {
        return """
        |
        |test("$name") {
        |    apiTestSuite("$name Suite") {
        |        baseUrl("https://api.example.com")
        |        
        |        scenario("Test $name") {
        |            step("Query data") {
        |                graphql {
        |                    query = ${"\"\"\""}
        |                        query {
        |                            users {
        |                                id
        |                                name
        |                            }
        |                        }
        |                    ${"\"\"\""}
        |                }.expect {
        |                    noErrors()
        |                    data("users") isNotEmpty()
        |                }
        |            }
        |        }
        |    }.execute()
        |}
        |""".trimMargin()
    }
    
    private fun generateLoadTest(name: String, endpoint: String, method: String): String {
        return """
        |
        |test("$name") {
        |    val config = LoadTestConfig(
        |        virtualUsers = 10,
        |        duration = 60.seconds,
        |        rampUpTime = 10.seconds
        |    )
        |    
        |    val runner = LoadTestRunner(config)
        |    val results = runner.runLoadTest("$name") { user ->
        |        apiTestSuite("Load Test") {
        |            scenario("User ${'$'}user") {
        |                step("Call $endpoint") {
        |                    ${method.lowercase()}("$endpoint")
        |                }.expect {
        |                    status(200)
        |                }
        |            }
        |        }
        |    }
        |    
        |    println(results.generateReport())
        |}
        |""".trimMargin()
    }
    
    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabled = psiFile != null && psiFile.name.endsWith(".kt")
    }
}

class TestGeneratorDialog(private val project: com.intellij.openapi.project.Project) : DialogWrapper(project) {
    private val nameField = JTextField(20)
    private val typeCombo = JComboBox(arrayOf("REST", "GraphQL", "Load Test", "gRPC", "WebSocket"))
    private val endpointField = JTextField("/api/endpoint", 20)
    private val methodCombo = JComboBox(arrayOf("GET", "POST", "PUT", "DELETE", "PATCH"))
    
    val testName: String get() = nameField.text
    val testType: String get() = typeCombo.selectedItem as String
    val endpoint: String get() = endpointField.text
    val method: String get() = methodCombo.selectedItem as String
    
    init {
        title = "Generate API Test"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        panel.add(JLabel("Test Name:"))
        panel.add(nameField)
        panel.add(Box.createVerticalStrut(10))
        
        panel.add(JLabel("Test Type:"))
        panel.add(typeCombo)
        panel.add(Box.createVerticalStrut(10))
        
        panel.add(JLabel("Endpoint:"))
        panel.add(endpointField)
        panel.add(Box.createVerticalStrut(10))
        
        panel.add(JLabel("HTTP Method:"))
        panel.add(methodCombo)
        
        return panel
    }
}
