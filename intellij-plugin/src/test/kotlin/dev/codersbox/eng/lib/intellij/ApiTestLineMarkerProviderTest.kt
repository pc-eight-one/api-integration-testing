package dev.codersbox.eng.lib.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

class ApiTestLineMarkerProviderTest : BasePlatformTestCase() {

    @Test
    fun testScenarioLineMarker() {
        myFixture.configureByText(
            "TestFile.kt",
            """
            import dev.codersbox.eng.lib.api.testing.dsl.*
            
            class MyTest {
                fun test() {
                    apiTestSuite("Test Suite") {
                        scenario("User Login") {
                            step("Login") {
                                // test code
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val lineMarkers = myFixture.findAllGutters()
        assertTrue("Should have line markers for scenario", lineMarkers.isNotEmpty())
    }

    @Test
    fun testStepLineMarker() {
        myFixture.configureByText(
            "TestFile.kt",
            """
            import dev.codersbox.eng.lib.api.testing.dsl.*
            
            class MyTest {
                fun test() {
                    apiTestSuite("Test Suite") {
                        scenario("User Login") {
                            step("Login") {
                                // test code
                            }
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val lineMarkers = myFixture.findAllGutters()
        assertTrue("Should have line markers for steps", lineMarkers.isNotEmpty())
    }
}
