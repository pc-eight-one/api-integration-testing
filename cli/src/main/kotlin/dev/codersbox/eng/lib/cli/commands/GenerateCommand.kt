package dev.codersbox.eng.lib.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import dev.codersbox.eng.lib.cli.generation.OpenApiParser
import dev.codersbox.eng.lib.cli.generation.TestCaseGenerator
import java.io.File

class GenerateCommand : CliktCommand(
    name = "generate",
    help = "Auto-generate test cases from OpenAPI specifications"
) {
    private val openApiFile by option(
        "--spec", "-s",
        help = "Path to OpenAPI specification file (YAML or JSON)"
    ).file(mustExist = true, canBeDir = false, mustBeReadable = true).required()

    private val outputDir by option(
        "--output", "-o",
        help = "Output directory for generated tests"
    ).file(canBeFile = false).default(File("src/test/kotlin/generated"))

    private val packageName by option(
        "--package", "-p",
        help = "Package name for generated tests"
    ).default("generated.tests")

    private val baseUrl by option(
        "--base-url", "-b",
        help = "Base URL for the API"
    ).default("http://localhost:8080")

    private val includeNegativeTests by option(
        "--negative",
        help = "Generate negative test cases"
    ).default("false")

    private val includeEdgeCases by option(
        "--edge-cases",
        help = "Generate edge case tests"
    ).default("false")

    override fun run() {
        echo("🚀 Generating test cases from OpenAPI specification...")
        echo("   Spec file: ${openApiFile.absolutePath}")
        echo("   Output directory: ${outputDir.absolutePath}")
        echo("   Package: $packageName")
        echo("   Base URL: $baseUrl")
        echo()

        try {
            // Parse OpenAPI spec
            val parser = OpenApiParser()
            val spec = parser.parse(openApiFile)
            
            echo("✅ Parsed OpenAPI spec: ${spec.info.title} (v${spec.info.version})")
            echo("   Found ${spec.paths.size} endpoints")
            echo()

            // Generate test cases
            val generator = TestCaseGenerator()
            
            outputDir.mkdirs()
            
            generator.generateTestSuite(
                spec = spec,
                outputDir = outputDir,
                packageName = packageName,
                baseUrl = baseUrl
            )

            echo()
            echo("✅ Test generation complete!")
            echo("   Generated tests in: ${outputDir.absolutePath}")
            echo()
            echo("Next steps:")
            echo("  1. Review the generated tests")
            echo("  2. Customize assertions and validations")
            echo("  3. Run tests with: mvn test")
            
        } catch (e: Exception) {
            echo("❌ Error: ${e.message}", err = true)
            e.printStackTrace()
        }
    }
}
