package dev.codersbox.eng.lib.intellij.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.kotlin.psi.KtCallExpression

class ApiTestCompletionContributor : CompletionContributor() {
    
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            ApiTestCompletionProvider()
        )
    }
}

class ApiTestCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    private val dslKeywords = listOf(
        "apiTestSuite",
        "scenario",
        "step",
        "get",
        "post",
        "put",
        "delete",
        "patch",
        "expect",
        "status",
        "jsonPath",
        "headers",
        "body"
    )
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        dslKeywords.forEach { keyword ->
            result.addElement(
                LookupElementBuilder.create(keyword)
                    .withTypeText("API Test DSL")
                    .bold()
            )
        }
    }
}
