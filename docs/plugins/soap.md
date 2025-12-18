# SOAP Plugin

The SOAP plugin enables testing of SOAP web services with full support for WSDL parsing and XML handling.

## Installation

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>plugin-soap</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

## Basic Usage

### Simple SOAP Request

```kotlin
import dev.codersbox.eng.lib.testing.plugins.soap.*

apiTestSuite("SOAP Calculator Service") {
    baseUrl = "http://www.dneonline.com/calculator.asmx"
    
    scenario("Add two numbers") {
        step("Call Add operation") {
            soap {
                operation = "Add"
                body = """
                    <Add xmlns="http://tempuri.org/">
                        <intA>10</intA>
                        <intB>20</intB>
                    </Add>
                """
            }.expect {
                status(200)
                xpath("//*[local-name()='AddResult']/text()") isEqualTo "30"
            }
        }
    }
}
```

### Using WSDL

```kotlin
apiTestSuite("User Service") {
    wsdl = "http://localhost:8080/services/UserService?wsdl"
    
    scenario("Get user") {
        step("Call GetUser operation") {
            soap {
                operation = "GetUser"
                namespace = "http://example.com/users"
                body = """
                    <GetUser>
                        <userId>123</userId>
                    </GetUser>
                """
            }.expect {
                xpath("//user/name") isEqualTo "John Doe"
                xpath("//user/email") isEqualTo "john@example.com"
            }
        }
    }
}
```

## SOAP Envelope

### Custom Envelope

```kotlin
scenario("Custom SOAP envelope") {
    step("Send full envelope") {
        soapEnvelope {
            envelope = """
                <soapenv:Envelope 
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:usr="http://example.com/users">
                    <soapenv:Header>
                        <usr:AuthToken>token123</usr:AuthToken>
                    </soapenv:Header>
                    <soapenv:Body>
                        <usr:GetUser>
                            <usr:userId>123</usr:userId>
                        </usr:GetUser>
                    </soapenv:Body>
                </soapenv:Envelope>
            """
        }.expect {
            status(200)
            xpath("//soapenv:Body//user") isNotNull()
        }
    }
}
```

### SOAP 1.2

```kotlin
soap {
    version = SoapVersion.SOAP_1_2
    operation = "GetUser"
    body = """<GetUser><userId>123</userId></GetUser>"""
}
```

## Headers

### SOAP Headers

```kotlin
scenario("Add SOAP headers") {
    step("Call with security header") {
        soap {
            operation = "SecureOperation"
            
            soapHeaders {
                header("Security", "http://security.example.com") {
                    """
                    <wsse:Security>
                        <wsse:UsernameToken>
                            <wsse:Username>admin</wsse:Username>
                            <wsse:Password>secret</wsse:Password>
                        </wsse:UsernameToken>
                    </wsse:Security>
                    """
                }
            }
            
            body = """<GetData/>"""
        }
    }
}
```

### HTTP Headers

```kotlin
soap {
    operation = "GetUser"
    body = """<GetUser><userId>123</userId></GetUser>"""
    
    httpHeaders {
        "Authorization" to "Bearer token"
        "X-Request-ID" to UUID.randomUUID().toString()
    }
}
```

## XPath Assertions

### Basic XPath

```kotlin
step("Validate response with XPath") {
    soap {
        operation = "GetUser"
        body = """<GetUser><userId>123</userId></GetUser>"""
    }.expect {
        // Basic path
        xpath("//user/name") isEqualTo "John Doe"
        
        // With namespaces
        xpath("//ns:user/ns:email", mapOf("ns" to "http://example.com")) 
            isEqualTo "john@example.com"
        
        // Text content
        xpath("//user/age/text()") isEqualTo "30"
        
        // Attribute
        xpath("//user/@id") isEqualTo "123"
        
        // Count elements
        xpath("count(//user/addresses/address)") isEqualTo "2"
    }
}
```

### Complex XPath

```kotlin
expect {
    // Conditional checks
    xpath("//user[age>18]/name") isEqualTo "John Doe"
    
    // Multiple results
    xpath("//users/user/name") hasSize 3
    
    // Contains
    xpath("//user/name") contains "John"
    
    // Exists
    xpath("//user/address") exists()
}
```

## WS-Security

### Username Token

```kotlin
scenario("WS-Security authentication") {
    step("Authenticate with username token") {
        soap {
            operation = "SecureOperation"
            
            wsSecurity {
                usernameToken {
                    username = "admin"
                    password = "secret"
                    passwordType = PasswordType.DIGEST
                }
            }
            
            body = """<GetSecureData/>"""
        }
    }
}
```

### Timestamp

```kotlin
wsSecurity {
    timestamp {
        ttl = 300.seconds // Time to live
    }
}
```

### Signature

```kotlin
wsSecurity {
    signature {
        keystore = File("keystore.jks")
        keystorePassword = "password"
        alias = "mykey"
        keyPassword = "keypass"
    }
}
```

### Encryption

```kotlin
wsSecurity {
    encryption {
        certificate = File("cert.pem")
        algorithm = "AES-256"
    }
}
```

## MTOM/XOP Attachments

### Send Attachment

```kotlin
scenario("Send file with MTOM") {
    step("Upload document") {
        soap {
            operation = "UploadDocument"
            mtom = true
            
            body = """
                <UploadDocument>
                    <fileName>document.pdf</fileName>
                    <fileData>
                        <xop:Include href="cid:file1" 
                            xmlns:xop="http://www.w3.org/2004/08/xop/include"/>
                    </fileData>
                </UploadDocument>
            """
            
            attachment("file1") {
                file = File("document.pdf")
                contentType = "application/pdf"
            }
        }
    }
}
```

### Receive Attachment

```kotlin
scenario("Download file") {
    step("Get document") {
        soap {
            operation = "GetDocument"
            body = """<GetDocument><id>123</id></GetDocument>"""
        }.expect {
            hasAttachment("document.pdf")
            attachment("document.pdf") {
                contentType isEqualTo "application/pdf"
                size greaterThan 1024
            }
        }
    }
}
```

## Fault Handling

### SOAP Faults

```kotlin
scenario("Handle SOAP faults") {
    step("Trigger fault") {
        soap {
            operation = "GetUser"
            body = """<GetUser><userId>invalid</userId></GetUser>"""
        }.expect {
            status(500)
            isSoapFault()
            xpath("//faultcode") isEqualTo "Client"
            xpath("//faultstring") contains "Invalid user ID"
        }
    }
}
```

### Custom Fault Details

```kotlin
expect {
    isSoapFault()
    xpath("//detail/errorCode") isEqualTo "USER_NOT_FOUND"
    xpath("//detail/message") isNotEmpty()
}
```

## Performance Testing

```kotlin
loadTestSuite("SOAP Performance") {
    baseUrl = "http://localhost:8080/services/UserService"
    wsdl = "$baseUrl?wsdl"
    
    loadConfig {
        virtualUsers = 50
        duration = 2.minutes
    }
    
    scenario("Load test GetUser") {
        step("Concurrent SOAP calls") {
            soap {
                operation = "GetUser"
                body = """
                    <GetUser>
                        <userId>${randomUserId()}</userId>
                    </GetUser>
                """
            }.expect {
                status(200)
                responseTime lessThan 500.milliseconds
            }
        }
    }
}
```

## Advanced Features

### Schema Validation

```kotlin
soap {
    operation = "CreateUser"
    body = userData
}.expect {
    matchesXmlSchema(File("schemas/user-response.xsd"))
}
```

### WSDL Mocking

```kotlin
scenario("Test with mocked service") {
    withMockSoapService {
        wsdl = File("user-service.wsdl")
        
        stubOperation("GetUser") {
            response = """
                <GetUserResponse>
                    <user>
                        <id>123</id>
                        <name>Mock User</name>
                    </user>
                </GetUserResponse>
            """
        }
        
        // Run tests against mock
        soap {
            operation = "GetUser"
            body = """<GetUser><userId>123</userId></GetUser>"""
        }.expect {
            xpath("//user/name") isEqualTo "Mock User"
        }
    }
}
```

## Best Practices

1. **Use WSDL**: Reference WSDL for auto-validation
2. **Namespace Handling**: Always specify namespaces in XPath
3. **Security**: Use WS-Security for sensitive operations
4. **Error Handling**: Check for both HTTP errors and SOAP faults
5. **Attachments**: Use MTOM for large binary data
6. **Schema Validation**: Validate against XSD schemas

## Next Steps

- [REST Plugin](./rest.md)
- [XML Format Plugin](./formats/xml.md)
- [Authentication](../guide/authentication.md)
