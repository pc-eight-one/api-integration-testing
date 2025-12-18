package dev.codersbox.eng.lib.api.testing.plugins.grpc

import dev.codersbox.eng.lib.api.testing.core.plugin.*
import dev.codersbox.eng.lib.api.testing.core.request.RequestContext
import dev.codersbox.eng.lib.api.testing.core.response.Response
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import java.util.concurrent.TimeUnit

/**
 * gRPC Protocol Plugin
 * Provides gRPC service testing support
 */
class GrpcProtocolPlugin : ProtocolPlugin {
    private val channels = mutableMapOf<String, ManagedChannel>()
    
    override val metadata = PluginMetadata(
        id = "grpc",
        name = "gRPC Protocol Plugin",
        version = "1.0.0",
        description = "Provides gRPC protocol support for API testing",
        author = "CodersBox Engineering",
        tags = listOf("grpc", "protobuf", "api")
    )
    
    override fun initialize(context: PluginContext) {
        // Channel configuration will be done per request
    }
    
    override fun createClient(): ProtocolClient {
        return GrpcProtocolClient(channels)
    }
    
    override fun shutdown() {
        channels.values.forEach { channel ->
            channel.shutdown()
            channel.awaitTermination(5, TimeUnit.SECONDS)
        }
        channels.clear()
    }
    
    override fun supportsProtocol(protocol: String): Boolean {
        return protocol.lowercase() == "grpc"
    }
}

/**
 * gRPC Protocol Client Implementation
 */
class GrpcProtocolClient(private val channels: MutableMap<String, ManagedChannel>) : ProtocolClient {
    
    override suspend fun execute(request: RequestContext): Response {
        val channel = getOrCreateChannel(request.url)
        
        // Extract metadata from headers
        val metadata = Metadata()
        request.headers.forEach { (key, value) ->
            metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value)
        }
        
        // Note: Actual gRPC call implementation would require:
        // 1. Generated service stubs from .proto files
        // 2. Method name extraction from request
        // 3. Request message serialization
        // This is a skeleton implementation showing the structure
        
        return Response(
            statusCode = 0, // gRPC uses status codes differently
            headers = mapOf("grpc-status" to "0"), // OK status
            body = """{"message": "gRPC call executed - actual implementation requires generated stubs"}""",
            contentType = "application/grpc+proto"
        )
    }
    
    private fun getOrCreateChannel(url: String): ManagedChannel {
        return channels.getOrPut(url) {
            val (host, port) = parseGrpcUrl(url)
            ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext() // Use TLS in production
                .build()
        }
    }
    
    private fun parseGrpcUrl(url: String): Pair<String, Int> {
        val cleanUrl = url.removePrefix("grpc://").removePrefix("grpcs://")
        val parts = cleanUrl.split(":")
        return Pair(
            parts[0],
            parts.getOrNull(1)?.toIntOrNull() ?: 50051 // Default gRPC port
        )
    }
}
