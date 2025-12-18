package dev.codersbox.eng.lib.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class LiveTemplatesTest : BasePlatformTestCase() {

    @Test
    fun testApiTestSuiteTemplate() {
        myFixture.configureByText(
            "TestFile.kt",
            "atsuite<caret>"
        )

        myFixture.type("\t")
        
        val text = myFixture.editor.document.text
        assertTrue("Should expand to apiTestSuite", text.contains("apiTestSuite"))
    }

    @Test
    fun testScenarioTemplate() {
        myFixture.configureByText(
            "TestFile.kt",
            """
            apiTestSuite("Test") {
                atscenario<caret>
            }
            """.trimIndent()
        )

        myFixture.type("\t")
        
        val text = myFixture.editor.document.text
        assertTrue("Should expand to scenario", text.contains("scenario"))
    }

    @Test
    fun testStepTemplate() {
        myFixture.configureByText(
            "TestFile.kt",
            """
            apiTestSuite("Test") {
                scenario("Test") {
                    atstep<caret>
                }
            }
            """.trimIndent()
        )

        myFixture.type("\t")
        
        val text = myFixture.editor.document.text
        assertTrue("Should expand to step", text.contains("step"))
    }
}
