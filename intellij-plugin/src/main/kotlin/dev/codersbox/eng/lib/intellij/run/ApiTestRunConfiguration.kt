package dev.codersbox.eng.lib.intellij.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element

class ApiTestRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String
) : RunConfigurationBase<ApiTestRunProfileState>(project, factory, name) {
    
    var testType: TestType = TestType.SCENARIO
    var testPath: String = ""
    var scenarioName: String = ""
    var suiteName: String = ""
    var isLoadTest: Boolean = false
    
    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        ApiTestSettingsEditor()
    
    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        ApiTestRunProfileState(environment, this)
    
    override fun readExternal(element: Element) {
        super.readExternal(element)
        testType = TestType.valueOf(element.getAttributeValue("testType") ?: "SCENARIO")
        testPath = element.getAttributeValue("testPath") ?: ""
        scenarioName = element.getAttributeValue("scenarioName") ?: ""
        suiteName = element.getAttributeValue("suiteName") ?: ""
        isLoadTest = element.getAttributeValue("isLoadTest")?.toBoolean() ?: false
    }
    
    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute("testType", testType.name)
        element.setAttribute("testPath", testPath)
        element.setAttribute("scenarioName", scenarioName)
        element.setAttribute("suiteName", suiteName)
        element.setAttribute("isLoadTest", isLoadTest.toString())
    }
    
    enum class TestType {
        SCENARIO, SUITE, LOAD_TEST
    }
}
