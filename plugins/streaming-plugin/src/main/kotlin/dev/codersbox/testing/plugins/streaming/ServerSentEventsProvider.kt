package dev.codersbox.testing.plugins.streaming

import com.fasterxml.jackson.databind.ObjectMapper
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ServerSentEventsProvider : StreamingProvider {
    private val objectMapper = ObjectMapper()
    private val client = OkHttpClient()

    override fun execute(request: Request): Response {
        val config = request.config as? SSEConfig
            ?: throw IllegalArgumentException("Request config must be SSEConfig")

        val events = ConcurrentLinkedQueue<SSEvent>()
        val latch = CountDownLatch(1)
        val startTime = System.currentTimeMillis()
        var errorMessage: String? = null

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                config.onOpen?.invoke()
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                val event = SSEvent(
                    id = id,
                    type = type ?: "message",
                    data = data,
                    timestamp = System.currentTimeMillis()
                )
                events.add(event)
                
                config.onEvent?.invoke(event)

                // Close if we've received expected number of events
                if (config.expectedEvents > 0 && events.size >= config.expectedEvents) {
                    eventSource.cancel()
                    latch.countDown()
                }
            }

            override fun onClosed(eventSource: EventSource) {
                latch.countDown()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                errorMessage = t?.message ?: "SSE connection failed"
                latch.countDown()
            }
        }

        val httpRequest = okhttp3.Request.Builder()
            .url(request.url)
            .apply {
                config.headers.forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .build()

        val eventSource = EventSources.createFactory(client)
            .newEventSource(httpRequest, listener)

        // Wait for events or timeout
        latch.await(config.timeout, TimeUnit.MILLISECONDS)
        eventSource.cancel()

        val responseTime = System.currentTimeMillis() - startTime

        return if (errorMessage != null) {
            Response(
                statusCode = 500,
                body = errorMessage!!,
                headers = emptyMap()
            )
        } else {
            Response(
                statusCode = 200,
                body = objectMapper.writeValueAsString(mapOf(
                    "eventCount" to events.size,
                    "events" to events.toList()
                )),
                headers = mapOf(
                    "X-SSE-Events" to events.size.toString(),
                    "X-Connection-Time" to responseTime.toString()
                ),
                responseTime = responseTime
            )
        }
    }

    override fun supports(protocol: String): Boolean = protocol.uppercase() == "SSE"
}

data class SSEConfig(
    val expectedEvents: Int = 0,
    val timeout: Long = 30000L,
    val headers: Map<String, String> = emptyMap(),
    val onOpen: (() -> Unit)? = null,
    val onEvent: ((SSEvent) -> Unit)? = null
)

data class SSEvent(
    val id: String?,
    val type: String,
    val data: String,
    val timestamp: Long
)
