package dev.codersbox.eng.lib.intellij.refactoring

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages

/**
 * Refactoring action to extract reusable steps from test code
 */
class ExtractReusableStepRefactoring : AnAction("Extract Reusable Step") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        val selectedText = editor.selectionModel.selectedText ?: return
        
        val stepName = Messages.showInputDialog(
            project,
            "Enter step name:",
            "Extract Reusable Step",
            null
        ) ?: return
        
        val reusableStepCode = generateReusableStep(stepName, selectedText)
        
        WriteCommandAction.runWriteCommandAction(project) {
            // Insert reusable step definition at top of file
            val document = editor.document
            document.insertString(0, reusableStepCode + "\n\n")
            
            // Replace selection with step call
            val selectionStart = editor.selectionModel.selectionStart
            val selectionEnd = editor.selectionModel.selectionEnd
            document.replaceString(
                selectionStart,
                selectionEnd,
                "execute($stepName)"
            )
        }
    }
    
    private fun generateReusableStep(name: String, code: String): String {
        return """
        |val $name = reusableStep("$name") {
        |    $code
        |}
        """.trimMargin()
    }
    
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() ?: false
        e.presentation.isEnabled = hasSelection
    }
}
