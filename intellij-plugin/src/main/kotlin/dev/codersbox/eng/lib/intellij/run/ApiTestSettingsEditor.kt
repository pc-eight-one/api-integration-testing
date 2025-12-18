package dev.codersbox.eng.lib.intellij.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class ApiTestSettingsEditor : SettingsEditor<ApiTestRunConfiguration>() {
    
    private val testPathField = JBTextField()
    private val scenarioNameField = JBTextField()
    private val suiteNameField = JBTextField()
    private val isLoadTestCheckBox = JBCheckBox("Load Test")
    
    override fun createEditor(): JComponent {
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Test Path:", testPathField)
            .addLabeledComponent("Scenario Name:", scenarioNameField)
            .addLabeledComponent("Suite Name:", suiteNameField)
            .addComponent(isLoadTestCheckBox)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
    
    override fun resetEditorFrom(configuration: ApiTestRunConfiguration) {
        testPathField.text = configuration.testPath
        scenarioNameField.text = configuration.scenarioName
        suiteNameField.text = configuration.suiteName
        isLoadTestCheckBox.isSelected = configuration.isLoadTest
    }
    
    override fun applyEditorTo(configuration: ApiTestRunConfiguration) {
        configuration.testPath = testPathField.text
        configuration.scenarioName = scenarioNameField.text
        configuration.suiteName = suiteNameField.text
        configuration.isLoadTest = isLoadTestCheckBox.isSelected
    }
}
