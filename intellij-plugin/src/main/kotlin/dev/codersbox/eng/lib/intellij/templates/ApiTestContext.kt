package dev.codersbox.eng.lib.intellij.templates

import com.intellij.codeInsight.template.TemplateActionContext
import com.intellij.codeInsight.template.TemplateContextType

class ApiTestContext : TemplateContextType("API_TEST", "API Test") {
    
    override fun isInContext(templateActionContext: TemplateActionContext): Boolean {
        val file = templateActionContext.file
        return file.name.endsWith(".kt") && 
               file.text.contains("apiTestSuite") || file.text.contains("scenario")
    }
}
