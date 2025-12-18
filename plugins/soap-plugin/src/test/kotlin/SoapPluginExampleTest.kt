package dev.codersbox.testing.plugins.soap

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SoapPluginExampleTest : FunSpec({
    
    test("SOAP 1.1 Request Example") {
        val plugin = SoapPlugin()
        
        val config = SoapRequestConfig(
            body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://www.example.com/webservice">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <web:GetUser>
                            <web:userId>123</web:userId>
                        </web:GetUser>
                    </soapenv:Body>
                </soapenv:Envelope>
            """.trimIndent(),
            soapAction = "http://www.example.com/GetUser",
            timeout = 5000
        )
        
        plugin.supports(
            dev.codersbox.testing.core.request.Request(
                method = "POST",
                url = "http://example.com/soap",
                protocol = "SOAP"
            )
        ) shouldBe true
    }
    
    test("SOAP Envelope Builder") {
        val envelope = SoapEnvelope.soap11(
            namespace = "http://example.com/",
            body = """
                <ns:GetUser>
                    <ns:userId>123</ns:userId>
                </ns:GetUser>
            """.trimIndent()
        )
        
        envelope shouldContain "http://schemas.xmlsoap.org/soap/envelope/"
        envelope shouldContain "<ns:GetUser>"
    }
})
