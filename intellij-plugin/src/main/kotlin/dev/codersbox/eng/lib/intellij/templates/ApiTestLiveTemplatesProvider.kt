package dev.codersbox.eng.lib.intellij.templates

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider

class ApiTestLiveTemplatesProvider : DefaultLiveTemplatesProvider {
    override fun getDefaultLiveTemplateFiles(): Array<String> {
        return arrayOf("liveTemplates/ApiTest")
    }
    
    override fun getHiddenLiveTemplateFiles(): Array<String>? {
        return null
    }
}
