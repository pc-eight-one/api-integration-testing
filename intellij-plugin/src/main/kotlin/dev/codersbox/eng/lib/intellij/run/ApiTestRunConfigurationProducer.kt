package dev.codersbox.eng.lib.intellij.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction

class ApiTestRunConfigurationProducer : LazyRunConfigurationProducer<ApiTestRunConfiguration>() {
    
    override fun getConfigurationFactory(): ConfigurationFactory =
        ApiTestConfigurationFactory(ApiTestConfigurationType())
    
    override fun isConfigurationFromContext(
        configuration: ApiTestRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val location = context.location ?: return false
        val element = location.psiElement
        return isApiTestElement(element)
    }
    
    override fun setupConfigurationFromContext(
        configuration: ApiTestRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val element = sourceElement.get() ?: return false
        
        if (isScenarioCall(element)) {
            configuration.testType = ApiTestRunConfiguration.TestType.SCENARIO
            configuration.scenarioName = extractScenarioName(element)
            configuration.name = "Scenario: ${configuration.scenarioName}"
            return true
        }
        
        if (isSuiteCall(element)) {
            configuration.testType = ApiTestRunConfiguration.TestType.SUITE
            configuration.suiteName = extractSuiteName(element)
            configuration.name = "Suite: ${configuration.suiteName}"
            return true
        }
        
        return false
    }
    
    private fun isApiTestElement(element: PsiElement): Boolean {
        return isScenarioCall(element) || isSuiteCall(element)
    }
    
    private fun isScenarioCall(element: PsiElement): Boolean {
        if (element is KtCallExpression) {
            val callName = element.calleeExpression?.text
            return callName == "scenario"
        }
        return false
    }
    
    private fun isSuiteCall(element: PsiElement): Boolean {
        if (element is KtCallExpression) {
            val callName = element.calleeExpression?.text
            return callName == "apiTestSuite"
        }
        return false
    }
    
    private fun extractScenarioName(element: PsiElement): String {
        if (element is KtCallExpression) {
            val args = element.valueArguments
            if (args.isNotEmpty()) {
                return args[0].text.trim('"')
            }
        }
        return "Unknown"
    }
    
    private fun extractSuiteName(element: PsiElement): String {
        if (element is KtCallExpression) {
            val args = element.valueArguments
            if (args.isNotEmpty()) {
                return args[0].text.trim('"')
            }
        }
        return "Unknown"
    }
}
