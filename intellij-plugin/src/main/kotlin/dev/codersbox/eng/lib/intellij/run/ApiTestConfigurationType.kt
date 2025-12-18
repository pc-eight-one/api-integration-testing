package dev.codersbox.eng.lib.intellij.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import javax.swing.Icon

class ApiTestConfigurationType : ConfigurationType {
    override fun getDisplayName(): String = "API Test"
    
    override fun getConfigurationTypeDescription(): String = 
        "Run API Integration Test scenarios and suites"
    
    override fun getIcon(): Icon? = null // TODO: Add icon
    
    override fun getId(): String = "API_TEST_CONFIGURATION"
    
    override fun getConfigurationFactories(): Array<ConfigurationFactory> = 
        arrayOf(ApiTestConfigurationFactory(this))
}

class ApiTestConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = "API_TEST_FACTORY"
    
    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        ApiTestRunConfiguration(project, this, "API Test")
    
    override fun getName(): String = "API Test Configuration"
}
