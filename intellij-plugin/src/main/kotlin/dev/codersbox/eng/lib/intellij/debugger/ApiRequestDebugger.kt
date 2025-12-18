package dev.codersbox.eng.lib.intellij.debugger

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Interactive debugger for API requests
 */
class ApiRequestDebugger(private val project: Project) {
    
    private val requestHistory = mutableListOf<ApiRequest>()
    
    fun recordRequest(request: ApiRequest) {
        requestHistory.add(request)
    }
    
    fun createDebugPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        
        // Request history table
        val tableModel = DefaultTableModel(
            arrayOf("Method", "URL", "Status", "Time (ms)", "Size"),
            0
        )
        
        requestHistory.forEach { req ->
            tableModel.addRow(arrayOf(
                req.method,
                req.url,
                req.statusCode,
                req.responseTime,
                req.responseSize
            ))
        }
        
        val table = JBTable(tableModel)
        val scrollPane = JBScrollPane(table)
        
        panel.add(scrollPane)
        
        // Details panel
        val detailsPanel = createDetailsPanel()
        panel.add(detailsPanel)
        
        // Action buttons
        val buttonPanel = createButtonPanel()
        panel.add(buttonPanel)
        
        return panel
    }
    
    private fun createDetailsPanel(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createTitledBorder("Request Details")
        
        val requestArea = JTextArea(10, 50)
        requestArea.isEditable = false
        panel.add(JBScrollPane(requestArea))
        
        val responseArea = JTextArea(10, 50)
        responseArea.isEditable = false
        panel.add(JBScrollPane(responseArea))
        
        return panel
    }
    
    private fun createButtonPanel(): JPanel {
        val panel = JPanel()
        
        val replayButton = JButton("Replay Request")
        replayButton.addActionListener {
            // Replay selected request
        }
        
        val editButton = JButton("Edit & Send")
        editButton.addActionListener {
            // Edit and resend request
        }
        
        val exportButton = JButton("Export as cURL")
        exportButton.addActionListener {
            // Export as cURL command
        }
        
        val compareButton = JButton("Compare Responses")
        compareButton.addActionListener {
            // Show diff between responses
        }
        
        panel.add(replayButton)
        panel.add(editButton)
        panel.add(exportButton)
        panel.add(compareButton)
        
        return panel
    }
}

data class ApiRequest(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val body: String?,
    val statusCode: Int,
    val responseTime: Long,
    val responseSize: Long,
    val responseBody: String,
    val timestamp: Long = System.currentTimeMillis()
)
