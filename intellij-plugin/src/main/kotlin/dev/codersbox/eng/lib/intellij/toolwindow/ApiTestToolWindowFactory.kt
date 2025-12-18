package dev.codersbox.eng.lib.intellij.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class ApiTestToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val apiTestToolWindow = ApiTestToolWindow(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(apiTestToolWindow.getContent(), "", false)
        toolWindow.contentManager.addContent(content)
    }
}
