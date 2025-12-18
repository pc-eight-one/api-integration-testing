package dev.codersbox.eng.lib.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class GenerateApiTestAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        Messages.showMessageDialog(
            project,
            "Generate API Test from OpenAPI Specification",
            "Generate Test",
            Messages.getInformationIcon()
        )
        // TODO: Implement OpenAPI to test generation
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
