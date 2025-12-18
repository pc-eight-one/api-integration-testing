package dev.codersbox.testing.plugins.soap

import dev.codersbox.testing.core.dsl.TestContext

// SOAP DSL Extensions
fun TestContext.soapRequest(
    url: String,
    action: String? = null,
    block: SoapRequestBuilder.() -> Unit
): dev.codersbox.testing.core.response.Response {
    val builder = SoapRequestBuilder(url, action)
    builder.block()
    return builder.execute(this)
}

class SoapRequestBuilder(
    private val url: String,
    private val soapAction: String?
) {
    private var bodyXml: String? = null
    private var namespace: Pair<String, String>? = null
    private val headers = mutableMapOf<String, String>()
    private var timeout: Int? = null

    fun body(xml: String) {
        bodyXml = xml
    }

    fun namespace(prefix: String, uri: String) {
        namespace = prefix to uri
    }

    fun header(name: String, value: String) {
        headers[name] = value
    }

    fun timeout(milliseconds: Int) {
        timeout = milliseconds
    }

    fun execute(context: TestContext): dev.codersbox.testing.core.response.Response {
        val config = SoapRequestConfig(
            body = bodyXml,
            namespace = namespace,
            soapAction = soapAction,
            headers = headers,
            timeout = timeout
        )

        val request = dev.codersbox.testing.core.request.Request(
            method = "POST",
            url = url,
            protocol = "SOAP",
            config = config
        )

        val plugin = SoapPlugin()
        return plugin.execute(request)
    }
}

// Envelope builders
object SoapEnvelope {
    fun soap11(
        namespace: String = "http://example.com/",
        body: String
    ): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="$namespace">
            <soapenv:Header/>
            <soapenv:Body>
                $body
            </soapenv:Body>
        </soapenv:Envelope>
    """.trimIndent()

    fun soap12(
        namespace: String = "http://example.com/",
        body: String
    ): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:ns="$namespace">
            <soap:Header/>
            <soap:Body>
                $body
            </soap:Body>
        </soap:Envelope>
    """.trimIndent()
}
