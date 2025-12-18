package dev.codersbox.eng.lib.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class ApiTestToolWindow(private val project: Project) {
    
    private val tableModel = DefaultTableModel(
        arrayOf("Scenario", "Status", "Duration", "Result"),
        0
    )
    
    private val resultsTable = JBTable(tableModel)
    
    fun getContent(): JComponent {
        val scrollPane = JBScrollPane(resultsTable)
        
        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel("API Test Results"))
            .addComponentFillVertically(scrollPane, 0)
            .panel
    }
    
    fun addTestResult(scenario: String, status: String, duration: Long, result: String) {
        tableModel.addRow(arrayOf(scenario, status, "${duration}ms", result))
    }
    
    fun clearResults() {
        tableModel.rowCount = 0
    }
}
