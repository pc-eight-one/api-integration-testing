package dev.codersbox.eng.lib.api.testing.validation

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class XmlPathExtractor : PathExtractor {
    override val supportedContentTypes = listOf(
        "application/xml",
        "text/xml",
        "application/soap+xml"
    )
    
    private val xpathFactory = XPathFactory.newInstance()
    private val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    
    override fun extract(content: ByteArray, path: String): Any? {
        return try {
            val document = documentBuilder.parse(ByteArrayInputStream(content))
            val xpath = xpathFactory.newXPath()
            val result = xpath.evaluate(path, document, XPathConstants.NODE) as? Node
            result?.textContent
        } catch (e: Exception) {
            null
        }
    }
    
    override fun extractAll(content: ByteArray, path: String): List<Any?> {
        return try {
            val document = documentBuilder.parse(ByteArrayInputStream(content))
            val xpath = xpathFactory.newXPath()
            val nodeList = xpath.evaluate(path, document, XPathConstants.NODESET) as NodeList
            (0 until nodeList.length).map { nodeList.item(it).textContent }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override fun canHandle(contentType: String): Boolean {
        return supportedContentTypes.any { contentType.contains(it, ignoreCase = true) }
    }
}
