# Creating Custom Plugins

## Custom Protocol Plugin

```kotlin
class MyProtocolPlugin : ProtocolPlugin {
    override val name = "myprotocol"
    
    override fun execute(request: Request): Response {
        // Your protocol implementation
    }
}
```

## Custom Format Plugin

```kotlin
class YamlFormatPlugin : FormatPlugin {
    override val name = "yaml"
    override val contentType = "application/yaml"
    
    override fun serialize(data: Any): String {
        return Yaml().dump(data)
    }
    
    override fun deserialize(content: String, type: Class<*>): Any {
        return Yaml().loadAs(content, type)
    }
}
```

## Register Plugin

Create `META-INF/services/dev.codersbox.eng.lib.core.plugins.ProtocolPlugin`:
```
com.mycompany.MyProtocolPlugin
```

See [Plugin Developer Guide](../PLUGIN_DEVELOPER_GUIDE.md) for complete documentation.
