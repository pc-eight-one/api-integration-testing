package dev.codersbox.eng.lib.api.testing.plugins.grpc

import dev.codersbox.eng.lib.api.testing.core.dsl.ScenarioContext
import dev.codersbox.eng.lib.api.testing.core.request.RequestContext

/**
 * DSL extensions for gRPC protocol
 */

fun ScenarioContext.grpcCall(
    service: String,
    method: String,
    host: String = "localhost",
    port: Int = 50051,
    block: RequestContext.() -> Unit = {}
) {
    val request = RequestContext(
        url = "grpc://$host:$port",
        method = method,
        headers = mutableMapOf("grpc-service" to service)
    ).apply(block)
    
    executeRequest(request)
}
