# Protocol Buffers Format Plugin

The Protocol Buffers (Protobuf) format plugin provides support for Google's efficient binary serialization format.

## Overview

- **Plugin Name**: `protobuf`
- **Content Types**: `application/x-protobuf`, `application/protobuf`, `application/octet-stream`
- **Implementation**: Google Protocol Buffers
- **Use Cases**: gRPC, high-performance APIs, microservices

## Features

- ✅ Binary serialization (compact and fast)
- ✅ Schema-based validation
- ✅ Backward/forward compatibility
- ✅ Code generation from .proto files
- ✅ Integration with gRPC
- ✅ JSON/Protobuf conversion

## Prerequisites

### Maven Dependencies

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.24.0</version>
</dependency>

<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java-util</artifactId>
    <version>3.24.0</version>
</dependency>
```

### Proto File Definition

Create `src/main/proto/user.proto`:

```protobuf
syntax = "proto3";

package dev.codersbox.eng.lib.model;

option java_multiple_files = true;
option java_package = "dev.codersbox.eng.lib.model";

message User {
  int32 id = 1;
  string name = 2;
  string email = 3;
  int32 age = 4;
  repeated string tags = 5;
  Address address = 6;
}

message Address {
  string street = 1;
  string city = 2;
  string country = 3;
  string zip_code = 4;
}

message UserList {
  repeated User users = 1;
  int32 total = 2;
}
```

### Code Generation

Add to `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocExecutable>/usr/local/bin/protoc</protocExecutable>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Basic Usage

### Sending Protobuf Requests

```kotlin
import dev.codersbox.eng.lib.model.User
import dev.codersbox.eng.lib.model.Address

scenario("Create user with Protobuf") {
    step("Send protobuf request") {
        val user = User.newBuilder()
            .setName("John Doe")
            .setEmail("john@example.com")
            .setAge(30)
            .addTags("developer")
            .addTags("kotlin")
            .setAddress(
                Address.newBuilder()
                    .setStreet("123 Main St")
                    .setCity("New York")
                    .setCountry("USA")
                    .setZipCode("10001")
                    .build()
            )
            .build()
        
        post("/api/users") {
            contentType = "application/x-protobuf"
            body = user
        }.expect {
            status(201)
        }
    }
}
```

### Binary Data Handling

```kotlin
scenario("Send protobuf bytes") {
    step("Send serialized protobuf") {
        val user = User.newBuilder()
            .setName("John Doe")
            .setEmail("john@example.com")
            .build()
        
        post("/api/users") {
            contentType = "application/x-protobuf"
            bodyBytes = user.toByteArray()
        }.expect {
            status(201)
        }
    }
}
```

## Response Validation

### Parsing Protobuf Responses

```kotlin
import dev.codersbox.eng.lib.model.User

scenario("Parse protobuf response") {
    step("Get user data") {
        get("/api/users/1") {
            accept = "application/x-protobuf"
        }.expect {
            status(200)
            body<User> { user ->
                user.name shouldBe "John Doe"
                user.email shouldContain "@example.com"
                user.age shouldBeGreaterThan 18
                user.tagsList shouldContain "developer"
                user.hasAddress() shouldBe true
                user.address.city shouldBe "New York"
            }
        }
    }
}
```

### Field Validation

```kotlin
scenario("Validate protobuf fields") {
    step("Check required fields") {
        get("/api/users/1") {
            accept = "application/x-protobuf"
        }.expect {
            status(200)
            body<User> { user ->
                // Check field presence
                user.hasAddress() shouldBe true
                user.tagsList shouldNotBeEmpty()
                
                // Validate field values
                user.id shouldBeGreaterThan 0
                user.name shouldNotBe ""
                user.email shouldMatch ".+@.+\\..+"
                
                // Check nested fields
                user.address.apply {
                    city shouldNotBe ""
                    country shouldNotBe ""
                    zipCode shouldMatch "\\d{5}"
                }
            }
        }
    }
}
```

## Advanced Features

### Repeated Fields (Lists)

```kotlin
scenario("Handle repeated fields") {
    step("Send user with tags") {
        val user = User.newBuilder()
            .setName("John Doe")
            .setEmail("john@example.com")
            .addAllTags(listOf("kotlin", "java", "developer", "architect"))
            .build()
        
        post("/api/users") {
            contentType = "application/x-protobuf"
            body = user
        }.expect {
            status(201)
            body<User> { response ->
                response.tagsCount shouldBe 4
                response.tagsList shouldContain "kotlin"
            }
        }
    }
}
```

### Nested Messages

```kotlin
scenario("Nested protobuf messages") {
    step("Create user with address") {
        val address = Address.newBuilder()
            .setStreet("123 Main St")
            .setCity("New York")
            .setCountry("USA")
            .setZipCode("10001")
            .build()
        
        val user = User.newBuilder()
            .setName("John Doe")
            .setEmail("john@example.com")
            .setAddress(address)
            .build()
        
        post("/api/users") {
            contentType = "application/x-protobuf"
            body = user
        }.expect {
            status(201)
        }
    }
}
```

### Collection Messages

```kotlin
import dev.codersbox.eng.lib.model.UserList

scenario("Send list of users") {
    step("Bulk create users") {
        val users = (1..10).map { i ->
            User.newBuilder()
                .setName("User $i")
                .setEmail("user$i@example.com")
                .setAge(20 + i)
                .build()
        }
        
        val userList = UserList.newBuilder()
            .addAllUsers(users)
            .setTotal(users.size)
            .build()
        
        post("/api/users/bulk") {
            contentType = "application/x-protobuf"
            body = userList
        }.expect {
            status(201)
            body<UserList> { response ->
                response.usersCount shouldBe 10
                response.total shouldBe 10
            }
        }
    }
}
```

## JSON Interoperability

### Protobuf to JSON

```kotlin
import com.google.protobuf.util.JsonFormat

scenario("Convert protobuf to JSON") {
    step("Get user as JSON") {
        get("/api/users/1") {
            accept = "application/x-protobuf"
        }.expect {
            status(200)
            body<User> { user ->
                val json = JsonFormat.printer()
                    .includingDefaultValueFields()
                    .print(user)
                
                println("User as JSON: $json")
                json shouldContain "\"name\""
                json shouldContain "\"email\""
            }
        }
    }
}
```

### JSON to Protobuf

```kotlin
scenario("Convert JSON to protobuf") {
    step("Send JSON, receive protobuf") {
        val json = """
            {
                "name": "John Doe",
                "email": "john@example.com",
                "age": 30
            }
        """.trimIndent()
        
        val builder = User.newBuilder()
        JsonFormat.parser().merge(json, builder)
        val user = builder.build()
        
        post("/api/users") {
            contentType = "application/x-protobuf"
            body = user
        }.expect {
            status(201)
        }
    }
}
```

## Testing with gRPC

See [gRPC Plugin](../grpc.md) for comprehensive gRPC testing, which uses Protocol Buffers underneath.

```kotlin
scenario("gRPC with protobuf") {
    step("Call gRPC service") {
        grpcCall("/UserService/GetUser") {
            request = User.newBuilder()
                .setId(1)
                .build()
        }.expect {
            status(OK)
            body<User> { user ->
                user.name shouldBe "John Doe"
            }
        }
    }
}
```

## Performance Benefits

### Size Comparison

```kotlin
scenario("Compare serialization sizes") {
    step("Measure JSON vs Protobuf") {
        val user = User.newBuilder()
            .setId(1)
            .setName("John Doe")
            .setEmail("john@example.com")
            .setAge(30)
            .build()
        
        // Protobuf binary
        val protobufBytes = user.toByteArray()
        println("Protobuf size: ${protobufBytes.size} bytes")
        
        // JSON equivalent
        val json = JsonFormat.printer().print(user)
        val jsonBytes = json.toByteArray()
        println("JSON size: ${jsonBytes.size} bytes")
        
        // Protobuf is typically 3-10x smaller
        protobufBytes.size shouldBeLessThan jsonBytes.size
    }
}
```

### Serialization Speed

```kotlin
scenario("Performance testing") {
    step("Benchmark serialization") {
        val user = User.newBuilder()
            .setName("John Doe")
            .setEmail("john@example.com")
            .build()
        
        // Warm-up
        repeat(1000) { user.toByteArray() }
        
        // Measure
        val start = System.nanoTime()
        repeat(10000) { user.toByteArray() }
        val duration = System.nanoTime() - start
        
        println("10k serializations: ${duration / 1_000_000}ms")
    }
}
```

## Schema Evolution

### Backward Compatibility

```protobuf
// Version 1
message User {
  int32 id = 1;
  string name = 2;
  string email = 3;
}

// Version 2 (backward compatible)
message User {
  int32 id = 1;
  string name = 2;
  string email = 3;
  int32 age = 4;  // New optional field
  repeated string tags = 5;  // New repeated field
}
```

```kotlin
scenario("Test backward compatibility") {
    step("Old client reads new message") {
        // Server sends User v2
        get("/api/users/1") {
            accept = "application/x-protobuf"
        }.expect {
            status(200)
            body<User> { user ->
                // Old fields still work
                user.id shouldBeGreaterThan 0
                user.name shouldNotBe ""
                
                // New fields have default values if not set
                if (user.hasAge()) {
                    user.age shouldBeGreaterThan 0
                }
            }
        }
    }
}
```

## Configuration

### Default Configuration

```properties
# application.properties
protobuf.preferBinary=true
protobuf.includeDefaults=false
```

### Programmatic Configuration

```kotlin
apiTestSuite("Protobuf Tests") {
    config {
        protobuf {
            preferBinary = true
            includeDefaultValues = false
            preserveProtoFieldNames = false
        }
    }
}
```

## Testing Tips

### Debug Protobuf Messages

```kotlin
scenario("Debug protobuf") {
    step("View message details") {
        get("/api/users/1") {
            accept = "application/x-protobuf"
        }.expect {
            status(200)
            body<User> { user ->
                // Print as JSON for readability
                println(JsonFormat.printer()
                    .includingDefaultValueFields()
                    .print(user))
                
                // Or use toString()
                println(user)
            }
        }
    }
}
```

### Validate Schema Compliance

```kotlin
scenario("Schema validation") {
    step("Ensure message structure") {
        get("/api/users/1") {
            accept = "application/x-protobuf"
        }.expect {
            status(200)
            body<User> { user ->
                // Check all required fields are set
                user.serializedSize shouldBeGreaterThan 0
                user.isInitialized shouldBe true
            }
        }
    }
}
```

## Best Practices

1. **Use Proto3**: Prefer `syntax = "proto3"` for new projects
2. **Never Change Field Numbers**: Field numbers are part of the binary format
3. **Mark Deprecated Fields**: Use `[deprecated = true]` instead of removing
4. **Use Nested Messages**: Group related fields
5. **Document Your Schema**: Add comments to .proto files
6. **Version Your API**: Include version in package name

## See Also

- [gRPC Protocol Plugin](../grpc.md)
- [JSON Format Plugin](json.md)
- [Custom Format Plugins](../../advanced/custom-plugins.md)
- [Protocol Buffers Documentation](https://protobuf.dev/)
