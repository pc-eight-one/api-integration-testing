package dev.codersbox.eng.lib.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class RunLoadTestAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        Messages.showMessageDialog(
            project,
            "Running Load Test",
            "Load Test",
            Messages.getInformationIcon()
        )
        // TODO: Implement actual load test execution
    }
    
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
