package dev.codersbox.eng.lib.intellij.live

/**
 * Live templates for quick API test creation
 * 
 * Shortcuts:
 * - apitest -> Basic API test template
 * - apiget -> GET request test
 * - apipost -> POST request test
 * - apival -> Validation block
 * - apigql -> GraphQL query test
 * - apiload -> Load test template
 * - apihook -> Test hooks (before/after)
 * - apidata -> Data-driven test
 * - apichain -> Scenario chaining
 */
object LiveTemplates {
    
    const val API_TEST = """
test("${"$"}NAME${"$"}") {
    apiTestSuite("${"$"}SUITE_NAME${"$"}") {
        baseUrl("${"$"}BASE_URL${"$"}")
        
        scenario("${"$"}SCENARIO${"$"}") {
            step("${"$"}STEP${"$"}") {
                ${"$"}END${"$"}
            }
        }
    }.execute()
}
"""
    
    const val API_GET = """
get("${"$"}ENDPOINT${"$"}") {
    headers {
        "Content-Type" to "application/json"
    }
}.expect {
    status(200)
    jsonPath("${"$"}PATH${"$"}") ${"$"}MATCHER${"$"} ${"$"}VALUE${"$"}
}
"""
    
    const val API_POST = """
post("${"$"}ENDPOINT${"$"}") {
    headers {
        "Content-Type" to "application/json"
    }
    body = ${"$"}BODY${"$"}
}.expect {
    status(201)
    jsonPath("${"$"}PATH${"$"}") ${"$"}MATCHER${"$"} ${"$"}VALUE${"$"}
}
"""
    
    const val API_VALIDATION = """
expect {
    status(${"$"}CODE${"$"})
    jsonPath("${"$"}PATH${"$"}") {
        ${"$"}VALIDATION${"$"}
    }
    responseTime lessThan 500.milliseconds
}
"""
    
    const val API_GRAPHQL = """
graphql {
    query = ${"\"\"\""}
        ${"$"}QUERY${"$"}
    ${"\"\"\""}
    variables = mapOf(
        "${"$"}VAR_NAME${"$"}" to ${"$"}VAR_VALUE${"$"}
    )
}.expect {
    noErrors()
    data("${"$"}FIELD${"$"}") ${"$"}MATCHER${"$"} ${"$"}VALUE${"$"}
}
"""
    
    const val API_LOAD_TEST = """
val config = LoadTestConfig(
    virtualUsers = ${"$"}USERS${"$"},
    duration = ${"$"}DURATION${"$"}.seconds,
    rampUpTime = ${"$"}RAMP_UP${"$"}.seconds
)

val runner = LoadTestRunner(config)
val results = runner.runLoadTest("${"$"}TEST_NAME${"$"}") { user ->
    apiTestSuite("Load Test") {
        scenario("User ${"$"}user") {
            ${"$"}TEST_LOGIC${"$"}
        }
    }
}

println(results.generateReport())
"""
    
    const val API_HOOKS = """
apiTestSuite("${"$"}NAME${"$"}") {
    beforeSuite {
        ${"$"}BEFORE_SUITE${"$"}
    }
    
    afterSuite {
        ${"$"}AFTER_SUITE${"$"}
    }
    
    beforeScenario {
        ${"$"}BEFORE_SCENARIO${"$"}
    }
    
    afterScenario {
        ${"$"}AFTER_SCENARIO${"$"}
    }
    
    scenario("${"$"}SCENARIO${"$"}") {
        ${"$"}TEST${"$"}
    }
}
"""
    
    const val API_DATA_DRIVEN = """
scenario("${"$"}NAME${"$"}").dataTable(
    "${"$"}PARAM1${"$"}" to listOf(${"$"}VALUES1${"$"}),
    "${"$"}PARAM2${"$"}" to listOf(${"$"}VALUES2${"$"}),
    "expected" to listOf(${"$"}EXPECTED${"$"})
) {
    step("${"$"}STEP${"$"}") {
        ${"$"}REQUEST${"$"} {
            param<${"$"}TYPE${"$"}>("${"$"}PARAM1${"$"}")
        }.expect {
            status(param<Int>("expected"))
        }
    }
}
"""
    
    const val API_CHAIN = """
val scenario1 = scenario("${"$"}NAME1${"$"}") {
    step("${"$"}STEP1${"$"}") {
        ${"$"}REQUEST1${"$"}
    }
}

scenario("${"$"}NAME2${"$"}") {
    dependsOn(scenario1)
    
    step("${"$"}STEP2${"$"}") {
        ${"$"}REQUEST2${"$"}
    }
}
"""
}
