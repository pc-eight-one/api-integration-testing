package dev.codersbox.eng.lib.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class ApiTestRunConfigurationTest : BasePlatformTestCase() {

    @Test
    fun testConfigurationCreation() {
        val configurationType = ApiTestConfigurationType()
        
        assertNotNull("Configuration type should be created", configurationType)
        assertEquals("API Test", configurationType.displayName)
        assertEquals("Run API Integration Test", configurationType.configurationTypeDescription)
    }

    @Test
    fun testConfigurationFactory() {
        val configurationType = ApiTestConfigurationType()
        val factory = configurationType.configurationFactories[0]
        
        assertNotNull("Factory should be created", factory)
        
        val configuration = factory.createTemplateConfiguration(project)
        assertTrue("Should create ApiTestRunConfiguration", configuration is ApiTestRunConfiguration)
    }

    @Test
    fun testRunConfigurationState() {
        val configurationType = ApiTestConfigurationType()
        val factory = configurationType.configurationFactories[0]
        val configuration = factory.createTemplateConfiguration(project) as ApiTestRunConfiguration
        
        configuration.scenarioName = "Test Scenario"
        configuration.testClass = "com.example.MyTest"
        
        assertEquals("Test Scenario", configuration.scenarioName)
        assertEquals("com.example.MyTest", configuration.testClass)
    }
}
