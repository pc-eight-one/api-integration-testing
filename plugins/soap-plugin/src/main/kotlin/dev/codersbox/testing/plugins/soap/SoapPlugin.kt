package dev.codersbox.testing.plugins.soap

import dev.codersbox.testing.core.plugin.ProtocolPlugin
import dev.codersbox.testing.core.request.Request
import dev.codersbox.testing.core.response.Response
import jakarta.xml.soap.*
import java.io.ByteArrayOutputStream
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class SoapPlugin : ProtocolPlugin {
    override val name: String = "soap"
    override val supportedProtocols: List<String> = listOf("SOAP", "SOAP11", "SOAP12")

    private val messageFactory = MessageFactory.newInstance()
    private val documentBuilder = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder()

    override fun execute(request: Request): Response {
        val soapConfig = request.config as? SoapRequestConfig
            ?: throw IllegalArgumentException("Request config must be SoapRequestConfig for SOAP plugin")

        val soapMessage = createSoapMessage(soapConfig)
        val connection = createConnection(request.url, soapConfig)

        return try {
            sendSoapMessage(connection, soapMessage, request)
        } catch (e: Exception) {
            Response(
                statusCode = 500,
                body = e.message ?: "SOAP request failed",
                headers = emptyMap(),
                error = e
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun createSoapMessage(config: SoapRequestConfig): SOAPMessage {
        val message = messageFactory.createMessage()
        val envelope = message.soapPart.envelope

        // Set namespace if provided
        config.namespace?.let { (prefix, uri) ->
            envelope.addNamespaceDeclaration(prefix, uri)
        }

        // Create SOAP Body
        val body = envelope.body
        val bodyElement = config.body?.let { bodyXml ->
            val doc = documentBuilder.parse(org.xml.sax.InputSource(StringReader(bodyXml)))
            body.addDocument(doc)
        }

        // Add SOAP Headers if provided
        config.headers.forEach { (name, value) ->
            val header = message.soapHeader
            val headerElement = header.addHeaderElement(envelope.createName(name))
            headerElement.addTextNode(value)
        }

        message.saveChanges()
        return message
    }

    private fun createConnection(url: String, config: SoapRequestConfig): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true
        
        connection.setRequestProperty("Content-Type", config.contentType)
        config.soapAction?.let {
            connection.setRequestProperty("SOAPAction", it)
        }
        
        config.timeout?.let {
            connection.connectTimeout = it
            connection.readTimeout = it
        }

        return connection
    }

    private fun sendSoapMessage(
        connection: HttpURLConnection,
        soapMessage: SOAPMessage,
        request: Request
    ): Response {
        val startTime = System.currentTimeMillis()

        // Send request
        connection.outputStream.use { outputStream ->
            soapMessage.writeTo(outputStream)
        }

        val statusCode = connection.responseCode
        val responseTime = System.currentTimeMillis() - startTime

        // Read response
        val responseBody = if (statusCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
        }

        val responseHeaders = connection.headerFields
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { it.value.joinToString(", ") }

        return Response(
            statusCode = statusCode,
            body = responseBody,
            headers = responseHeaders,
            responseTime = responseTime
        )
    }

    override fun supports(request: Request): Boolean {
        return request.protocol.uppercase() in supportedProtocols
    }
}

data class SoapRequestConfig(
    val body: String? = null,
    val namespace: Pair<String, String>? = null,
    val soapAction: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val timeout: Int? = null,
    val contentType: String = "text/xml; charset=utf-8"
)
