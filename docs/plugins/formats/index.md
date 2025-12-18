# Format Plugins Overview

Format plugins handle serialization and deserialization of request/response bodies in different data formats. The framework provides a flexible plugin system that allows you to work with various data formats seamlessly.

## Available Format Plugins

The framework includes built-in support for:

- **[JSON](json.md)** - JavaScript Object Notation (default)
- **[XML](xml.md)** - Extensible Markup Language
- **[CSV](csv.md)** - Comma-Separated Values
- **[Protocol Buffers](protobuf.md)** - Google's data serialization format

## Architecture

Format plugins implement the `FormatPlugin` interface and are registered with the `FormatPluginRegistry`:

```kotlin
interface FormatPlugin {
    val name: String
    val contentType: String
    fun serialize(data: Any): String
    fun deserialize(content: String, type: Class<*>): Any
}
```

## Plugin Registration

Format plugins are automatically discovered via Java's ServiceLoader mechanism:

```kotlin
// In META-INF/services/dev.codersbox.eng.lib.core.plugins.FormatPlugin
dev.codersbox.eng.lib.plugins.formats.json.JsonFormatPlugin
dev.codersbox.eng.lib.plugins.formats.xml.XmlFormatPlugin
dev.codersbox.eng.lib.plugins.formats.csv.CsvFormatPlugin
dev.codersbox.eng.lib.plugins.formats.protobuf.ProtobufFormatPlugin
```

## Using Format Plugins

### Automatic Content Type Detection

The framework automatically selects the appropriate format plugin based on the `Content-Type` header:

```kotlin
scenario("JSON request") {
    step("Send JSON data") {
        post("/api/users") {
            contentType = "application/json"
            body = User(name = "John", email = "john@example.com")
        }
    }
}

scenario("XML request") {
    step("Send XML data") {
        post("/api/users") {
            contentType = "application/xml"
            body = """<user><name>John</name><email>john@example.com</email></user>"""
        }
    }
}
```

### Explicit Format Selection

You can explicitly specify the format plugin to use:

```kotlin
scenario("Explicit format selection") {
    step("Use CSV format") {
        post("/api/bulk-users") {
            format = "csv"
            body = listOf(
                mapOf("name" to "John", "email" to "john@example.com"),
                mapOf("name" to "Jane", "email" to "jane@example.com")
            )
        }
    }
}
```

## Response Parsing

Format plugins handle response deserialization automatically:

```kotlin
scenario("Parse response") {
    step("Get user data") {
        get("/api/users/1") {
            accept = "application/json"
        }.expect {
            status(200)
            body<User> { user ->
                user.name shouldBe "John"
                user.email shouldContain "@example.com"
            }
        }
    }
}
```

## Content Type Mapping

Default content type mappings:

| Format | Content Type | File Extension |
|--------|-------------|----------------|
| JSON | `application/json` | `.json` |
| XML | `application/xml`, `text/xml` | `.xml` |
| CSV | `text/csv` | `.csv` |
| Protobuf | `application/x-protobuf`, `application/protobuf` | `.proto` |

## Creating Custom Format Plugins

See [Custom Format Plugins](../../advanced/custom-plugins.md#custom-format-plugins) for details on creating your own format plugins.

## Next Steps

- Learn about specific format plugins: [JSON](json.md), [XML](xml.md), [CSV](csv.md), [Protocol Buffers](protobuf.md)
- Create [Custom Format Plugins](../../advanced/custom-plugins.md)
- Explore [Protocol Plugins](../overview.md)
