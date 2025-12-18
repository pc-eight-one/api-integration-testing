# XML Format Plugin

The XML format plugin provides comprehensive support for XML serialization and deserialization.

## Overview

- **Plugin Name**: `xml`
- **Content Types**: `application/xml`, `text/xml`
- **Implementation**: Jackson XML or JAXB
- **Default**: No (explicit content type required)

## Features

- ✅ JAXB annotation support
- ✅ XPath validation support
- ✅ XML namespace handling
- ✅ Attribute and element mapping
- ✅ Pretty printing
- ✅ CDATA section support

## Basic Usage

### Sending XML Requests

```kotlin
import javax.xml.bind.annotation.*

@XmlRootElement(name = "user")
data class User(
    @XmlElement val name: String,
    @XmlElement val email: String,
    @XmlAttribute val id: Int? = null
)

scenario("Create user with XML") {
    step("Send XML request") {
        post("/api/users") {
            contentType = "application/xml"
            body = User(name = "John Doe", email = "john@example.com")
        }.expect {
            status(201)
        }
    }
}
```

### Using Raw XML Strings

```kotlin
scenario("Send raw XML") {
    step("Post XML string") {
        post("/api/users") {
            contentType = "application/xml"
            body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <user>
                    <name>John Doe</name>
                    <email>john@example.com</email>
                    <age>30</age>
                </user>
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}
```

## Response Validation

### XPath Expressions

```kotlin
scenario("Validate XML response") {
    step("Check user data") {
        get("/api/users/1") {
            accept = "application/xml"
        }.expect {
            status(200)
            xpath("/user/name/text()") shouldBe "John Doe"
            xpath("/user/email/text()") shouldContain "@example.com"
            xpath("/user/@id") shouldBe "1"
            xpath("count(/user/tags/tag)") shouldBe "2"
        }
    }
}
```

### Type-Safe Deserialization

```kotlin
scenario("Parse XML to object") {
    step("Deserialize response") {
        get("/api/users/1") {
            accept = "application/xml"
        }.expect {
            status(200)
            body<User> { user ->
                user.name shouldBe "John Doe"
                user.email shouldContain "@example.com"
            }
        }
    }
}
```

## Advanced Features

### XML Namespaces

```kotlin
@XmlRootElement(name = "user", namespace = "http://example.com/users")
@XmlAccessorType(XmlAccessType.FIELD)
data class UserWithNamespace(
    @XmlElement(namespace = "http://example.com/users")
    val name: String,
    
    @XmlElement(namespace = "http://example.com/users")
    val email: String
)

scenario("Handle XML namespaces") {
    step("Send namespaced XML") {
        post("/api/users") {
            contentType = "application/xml"
            body = UserWithNamespace(
                name = "John Doe",
                email = "john@example.com"
            )
        }.expect {
            status(201)
            xpath("/ns:user/ns:name/text()", 
                  namespaces = mapOf("ns" to "http://example.com/users")) shouldBe "John Doe"
        }
    }
}
```

### XML Attributes

```kotlin
@XmlRootElement(name = "product")
data class Product(
    @XmlAttribute val id: String,
    @XmlAttribute val version: String,
    @XmlElement val name: String,
    @XmlElement val price: Double
)

scenario("XML with attributes") {
    step("Create product") {
        post("/api/products") {
            contentType = "application/xml"
            body = Product(
                id = "P123",
                version = "1.0",
                name = "Widget",
                price = 29.99
            )
        }.expect {
            status(201)
            xpath("/product/@id") shouldBe "P123"
            xpath("/product/@version") shouldBe "1.0"
            xpath("/product/name/text()") shouldBe "Widget"
        }
    }
}
```

### Nested Elements

```kotlin
@XmlRootElement(name = "order")
data class Order(
    @XmlElement val orderId: String,
    @XmlElement val customer: Customer,
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    val items: List<OrderItem>
)

@XmlAccessorType(XmlAccessType.FIELD)
data class Customer(
    @XmlElement val name: String,
    @XmlElement val email: String
)

@XmlAccessorType(XmlAccessType.FIELD)
data class OrderItem(
    @XmlAttribute val sku: String,
    @XmlElement val quantity: Int
)

scenario("Nested XML structures") {
    step("Create order") {
        post("/api/orders") {
            contentType = "application/xml"
            body = Order(
                orderId = "ORD-001",
                customer = Customer("John Doe", "john@example.com"),
                items = listOf(
                    OrderItem("SKU-123", 2),
                    OrderItem("SKU-456", 1)
                )
            )
        }.expect {
            status(201)
            xpath("/order/customer/name/text()") shouldBe "John Doe"
            xpath("count(/order/items/item)") shouldBe "2"
        }
    }
}
```

### CDATA Sections

```kotlin
scenario("XML with CDATA") {
    step("Send HTML content") {
        post("/api/articles") {
            contentType = "application/xml"
            body = """
                <article>
                    <title>My Article</title>
                    <content><![CDATA[
                        <p>This is <b>HTML</b> content</p>
                    ]]></content>
                </article>
            """.trimIndent()
        }.expect {
            status(201)
        }
    }
}
```

## XPath Syntax

Common XPath expressions:

| Expression | Description | Example |
|------------|-------------|---------|
| `/root` | Root element | `/user` |
| `/root/child` | Direct child | `/user/name` |
| `//element` | Any descendant | `//email` |
| `/root/@attr` | Attribute | `/user/@id` |
| `/root/child/text()` | Text content | `/user/name/text()` |
| `/root/child[1]` | First element | `/users/user[1]` |
| `count(/root/child)` | Count elements | `count(/users/user)` |

## SOAP Support

For SOAP web services, see the [SOAP Plugin](../soap.md) which provides specialized SOAP support on top of XML.

```kotlin
scenario("SOAP request") {
    step("Call SOAP service") {
        post("/soap/users") {
            contentType = "text/xml"
            body = """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    <soap:Body>
                        <GetUser xmlns="http://example.com/users">
                            <userId>123</userId>
                        </GetUser>
                    </soap:Body>
                </soap:Envelope>
            """.trimIndent()
        }.expect {
            status(200)
            xpath("//GetUserResponse/name/text()", 
                  namespaces = mapOf("soap" to "http://schemas.xmlsoap.org/soap/envelope/"))
        }
    }
}
```

## Configuration

### Default Configuration

```properties
# application.properties
xml.prettyPrint=false
xml.includeXmlDeclaration=true
xml.encoding=UTF-8
```

### Programmatic Configuration

```kotlin
apiTestSuite("XML Config") {
    config {
        xml {
            prettyPrint = true
            includeXmlDeclaration = true
            encoding = "UTF-8"
        }
    }
}
```

## Testing Tips

### Pretty Print for Debugging

```kotlin
scenario("Debug XML") {
    step("View formatted response") {
        get("/api/users/1") {
            accept = "application/xml"
        }.expect {
            status(200)
            printBody() // Pretty prints XML
        }
    }
}
```

### Schema Validation

```kotlin
scenario("Validate against XSD") {
    step("Check XML schema") {
        get("/api/users/1") {
            accept = "application/xml"
        }.expect {
            status(200)
            validateXmlSchema("schemas/user.xsd")
        }
    }
}
```

## See Also

- [JSON Format Plugin](json.md)
- [SOAP Protocol Plugin](../soap.md)
- [Custom Format Plugins](../../advanced/custom-plugins.md)
- [Assertions & Validation](../../guide/assertions.md)
