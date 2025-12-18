package dev.codersbox.eng.lib.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType

class RunScenarioAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        val element = psiFile.findElementAt(editor.caretModel.offset) ?: return
        val callExpression = element.getParentOfType<KtCallExpression>(false)
        
        if (callExpression != null && callExpression.calleeExpression?.text == "scenario") {
            val scenarioName = extractScenarioName(callExpression)
            Messages.showMessageDialog(
                project,
                "Running scenario: $scenarioName",
                "Run Scenario",
                Messages.getInformationIcon()
            )
            // TODO: Implement actual test execution
        }
    }
    
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        
        e.presentation.isEnabled = editor != null && psiFile != null
    }
    
    private fun extractScenarioName(callExpression: KtCallExpression): String {
        val args = callExpression.valueArguments
        return if (args.isNotEmpty()) {
            args[0].text.trim('"')
        } else {
            "Unknown"
        }
    }
}
