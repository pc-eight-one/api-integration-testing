package dev.codersbox.eng.lib.intellij

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class ApiTestCompletionContributorTest : BasePlatformTestCase() {

    @Test
    fun testHttpMethodCompletion() {
        myFixture.configureByText(
            "TestFile.kt",
            """
            import dev.codersbox.eng.lib.api.testing.dsl.*
            
            class MyTest {
                fun test() {
                    apiTestSuite("Test Suite") {
                        scenario("Test") {
                            step("Request") {
                                <caret>
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val completions = myFixture.complete(CompletionType.BASIC)
        assertNotNull(completions)
        
        val lookupStrings = completions.map { it.lookupString }
        assertTrue("Should suggest get", lookupStrings.contains("get"))
        assertTrue("Should suggest post", lookupStrings.contains("post"))
        assertTrue("Should suggest put", lookupStrings.contains("put"))
        assertTrue("Should suggest delete", lookupStrings.contains("delete"))
    }

    @Test
    fun testValidationMethodCompletion() {
        myFixture.configureByText(
            "TestFile.kt",
            """
            import dev.codersbox.eng.lib.api.testing.dsl.*
            
            class MyTest {
                fun test() {
                    apiTestSuite("Test Suite") {
                        scenario("Test") {
                            step("Request") {
                                get("/api/users").expect {
                                    <caret>
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val completions = myFixture.complete(CompletionType.BASIC)
        assertNotNull(completions)
        
        val lookupStrings = completions.map { it.lookupString }
        assertTrue("Should suggest status", lookupStrings.contains("status"))
        assertTrue("Should suggest jsonPath", lookupStrings.contains("jsonPath"))
        assertTrue("Should suggest header", lookupStrings.contains("header"))
    }
}
