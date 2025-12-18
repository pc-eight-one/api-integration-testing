# CSV Format Plugin

The CSV format plugin provides support for CSV (Comma-Separated Values) data serialization and deserialization.

## Overview

- **Plugin Name**: `csv`
- **Content Type**: `text/csv`
- **Implementation**: Apache Commons CSV
- **Use Cases**: Bulk operations, data import/export, reporting

## Features

- ✅ Header row support
- ✅ Custom delimiters (comma, tab, pipe)
- ✅ Quote handling
- ✅ Custom line endings
- ✅ Bulk data operations
- ✅ Data-driven testing

## Basic Usage

### Sending CSV Requests

```kotlin
scenario("Upload CSV data") {
    step("Send CSV file") {
        post("/api/users/bulk") {
            contentType = "text/csv"
            body = """
                name,email,age
                John Doe,john@example.com,30
                Jane Smith,jane@example.com,25
                Bob Johnson,bob@example.com,35
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}
```

### Using List of Maps

```kotlin
scenario("Send structured CSV") {
    step("Convert data to CSV") {
        post("/api/users/bulk") {
            contentType = "text/csv"
            body = listOf(
                mapOf("name" to "John Doe", "email" to "john@example.com", "age" to "30"),
                mapOf("name" to "Jane Smith", "email" to "jane@example.com", "age" to "25"),
                mapOf("name" to "Bob Johnson", "email" to "bob@example.com", "age" to "35")
            )
        }.expect {
            status(201)
        }
    }
}
```

### Using Data Classes

```kotlin
data class CsvUser(
    val name: String,
    val email: String,
    val age: Int
)

scenario("CSV from objects") {
    step("Serialize objects to CSV") {
        post("/api/users/bulk") {
            format = "csv"
            body = listOf(
                CsvUser("John Doe", "john@example.com", 30),
                CsvUser("Jane Smith", "jane@example.com", 25),
                CsvUser("Bob Johnson", "bob@example.com", 35)
            )
        }.expect {
            status(201)
        }
    }
}
```

## Response Validation

### Parsing CSV Responses

```kotlin
scenario("Parse CSV response") {
    step("Get user export") {
        get("/api/users/export") {
            accept = "text/csv"
        }.expect {
            status(200)
            bodyAsCsv { rows ->
                rows shouldHaveSize 3
                rows[0]["name"] shouldBe "John Doe"
                rows[1]["email"] shouldContain "@example.com"
            }
        }
    }
}
```

### Validating CSV Structure

```kotlin
scenario("Validate CSV structure") {
    step("Check headers and data") {
        get("/api/reports/daily") {
            accept = "text/csv"
        }.expect {
            status(200)
            csvHeaders() shouldContain listOf("date", "users", "revenue")
            csvRowCount() shouldBeGreaterThan 0
        }
    }
}
```

## Advanced Features

### Custom Delimiters

```kotlin
scenario("TSV (Tab-Separated Values)") {
    step("Send TSV data") {
        post("/api/data/import") {
            contentType = "text/tab-separated-values"
            csvConfig {
                delimiter = '\t'
            }
            body = """
                name	email	age
                John Doe	john@example.com	30
                Jane Smith	jane@example.com	25
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}

scenario("Pipe-delimited file") {
    step("Send pipe-delimited data") {
        post("/api/data/import") {
            csvConfig {
                delimiter = '|'
            }
            body = """
                name|email|age
                John Doe|john@example.com|30
                Jane Smith|jane@example.com|25
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}
```

### Quote Handling

```kotlin
scenario("CSV with quotes") {
    step("Handle quoted fields") {
        post("/api/data/import") {
            contentType = "text/csv"
            csvConfig {
                quoteChar = '"'
                escapeChar = '\\'
            }
            body = """
                name,description,price
                "Product A","This is a ""great"" product",29.99
                "Product B","Another, excellent, item",39.99
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}
```

### No Header Row

```kotlin
scenario("CSV without headers") {
    step("Send data without header row") {
        post("/api/data/import") {
            contentType = "text/csv"
            csvConfig {
                hasHeader = false
            }
            body = """
                John Doe,john@example.com,30
                Jane Smith,jane@example.com,25
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}
```

## Data-Driven Testing with CSV

### Load Test Data from CSV Files

```kotlin
scenario("Data-driven from CSV file") {
    val testData = loadCsvFile("testdata/users.csv")
    
    testData.forEach { row ->
        step("Test user: ${row["name"]}") {
            post("/api/users") {
                contentType = "application/json"
                body = mapOf(
                    "name" to row["name"],
                    "email" to row["email"],
                    "age" to row["age"]?.toInt()
                )
            }.expect {
                status(201)
            }
        }
    }
}
```

### Parameterized Tests with CSV

```kotlin
scenario("Login with CSV data").dataFromCsv("testdata/login-scenarios.csv") { row ->
    step("Login as ${row["username"]}") {
        post("/api/login") {
            body = LoginRequest(
                username = row["username"]!!,
                password = row["password"]!!
            )
        }.expect {
            status(row["expectedStatus"]!!.toInt())
        }
    }
}
```

## Bulk Operations

### Bulk Create

```kotlin
scenario("Bulk user creation") {
    step("Create multiple users from CSV") {
        val users = generateFakeUsers(100) // Generate 100 fake users
        
        post("/api/users/bulk") {
            format = "csv"
            body = users
        }.expect {
            status(201)
            jsonPath("$.created") shouldBe 100
        }
    }
}
```

### Export and Compare

```kotlin
scenario("Export and validate") {
    var exportedData: List<Map<String, String>> = emptyList()
    
    step("Export users to CSV") {
        get("/api/users/export") {
            accept = "text/csv"
        }.expect {
            status(200)
            bodyAsCsv { rows ->
                exportedData = rows
                rows shouldNotBeEmpty()
            }
        }
    }
    
    step("Verify exported data") {
        exportedData.forEach { user ->
            user["email"] shouldContain "@"
            user["age"]?.toIntOrNull() shouldNotBe null
        }
    }
}
```

## Configuration

### CSV Format Options

```kotlin
apiTestSuite("CSV Tests") {
    config {
        csv {
            delimiter = ','
            quoteChar = '"'
            escapeChar = '\\'
            recordSeparator = "\n"
            hasHeader = true
            ignoreEmptyLines = true
            trim = true
        }
    }
}
```

### Apache Commons CSV Formats

```kotlin
import org.apache.commons.csv.CSVFormat

scenario("Use predefined CSV format") {
    step("Send Excel-style CSV") {
        post("/api/data/import") {
            csvConfig {
                format = CSVFormat.EXCEL
            }
            body = csvData
        }
    }
}

// Available formats: DEFAULT, EXCEL, MYSQL, RFC4180, TDF
```

## Testing Tips

### Preview CSV Data

```kotlin
scenario("Debug CSV") {
    step("View CSV content") {
        get("/api/export") {
            accept = "text/csv"
        }.expect {
            status(200)
            printBody() // Prints formatted CSV
            
            bodyAsCsv { rows ->
                println("Rows: ${rows.size}")
                rows.take(5).forEach { println(it) } // Print first 5 rows
            }
        }
    }
}
```

### Validate CSV Quality

```kotlin
scenario("CSV data quality checks") {
    step("Validate exported data") {
        get("/api/users/export") {
            accept = "text/csv"
        }.expect {
            status(200)
            bodyAsCsv { rows ->
                // Check no empty rows
                rows.forEach { row ->
                    row.values.forEach { it shouldNotBe "" }
                }
                
                // Check email format
                rows.forEach { row ->
                    row["email"] shouldMatch "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}"
                }
                
                // Check uniqueness
                val emails = rows.map { it["email"] }
                emails.size shouldBe emails.toSet().size
            }
        }
    }
}
```

## Use Cases

### Reporting

```kotlin
scenario("Generate and validate report") {
    step("Get daily report") {
        get("/api/reports/daily") {
            accept = "text/csv"
            queryParam("date", "2024-01-15")
        }.expect {
            status(200)
            csvHeaders() shouldContain listOf("date", "metric", "value")
            bodyAsCsv { rows ->
                rows.sumOf { it["value"]?.toDouble() ?: 0.0 } shouldBeGreaterThan 0.0
            }
        }
    }
}
```

### Data Migration

```kotlin
scenario("Data migration validation") {
    step("Export from old system") {
        get("/api/v1/users/export") {
            accept = "text/csv"
        }.storeTo("oldData")
    }
    
    step("Import to new system") {
        post("/api/v2/users/import") {
            contentType = "text/csv"
            body = context["oldData"]
        }.expect {
            status(200)
        }
    }
    
    step("Verify migration") {
        get("/api/v2/users/export") {
            accept = "text/csv"
        }.expect {
            status(200)
            // Compare with old data
        }
    }
}
```

## See Also

- [JSON Format Plugin](json.md)
- [Data-Driven Testing](../../guide/data-driven-testing.md)
- [Test Data Management](../../guide/test-data.md)
- [Bulk Operations Examples](../../examples/rest-examples.md#bulk-operations)
