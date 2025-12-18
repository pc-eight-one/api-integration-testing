package dev.codersbox.eng.lib.intellij.markers

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression

class ApiTestRunLineMarkerContributor : RunLineMarkerContributor() {
    
    override fun getInfo(element: PsiElement): Info? {
        if (element !is KtCallExpression) return null
        
        val callName = element.calleeExpression?.text ?: return null
        
        return when (callName) {
            "scenario", "apiTestSuite" -> {
                val actions = ExecutorAction.getActions(0)
                Info(
                    null,
                    { "Run $callName" },
                    *actions
                )
            }
            else -> null
        }
    }
}
