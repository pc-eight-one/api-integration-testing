package dev.codersbox.eng.lib.intellij.inspections

import com.intellij.codeInspection.AbstractBaseKotlinInspection
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid

class ApiTestValidationInspection : AbstractBaseKotlinInspection() {
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                super.visitCallExpression(expression)
                
                val callName = expression.calleeExpression?.text
                
                if (callName == "scenario" || callName == "apiTestSuite") {
                    val args = expression.valueArguments
                    if (args.isEmpty()) {
                        holder.registerProblem(
                            expression,
                            "$callName requires a name parameter"
                        )
                    }
                }
            }
        }
    }
}
