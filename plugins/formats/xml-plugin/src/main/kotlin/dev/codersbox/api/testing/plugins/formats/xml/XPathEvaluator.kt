package dev.codersbox.api.testing.plugins.formats.xml

import org.dom4j.Document
import org.dom4j.Node

class XPathEvaluator(private val document: Document) {
    
    fun selectSingleNode(xpath: String): Node? {
        return document.selectSingleNode(xpath)
    }

    fun selectNodes(xpath: String): List<Node> {
        @Suppress("UNCHECKED_CAST")
        return document.selectNodes(xpath) as List<Node>
    }

    fun getString(xpath: String): String? {
        return selectSingleNode(xpath)?.text
    }

    fun getInt(xpath: String): Int? {
        return getString(xpath)?.toIntOrNull()
    }

    fun getDouble(xpath: String): Double? {
        return getString(xpath)?.toDoubleOrNull()
    }

    fun getBoolean(xpath: String): Boolean? {
        return getString(xpath)?.toBooleanStrictOrNull()
    }

    fun exists(xpath: String): Boolean {
        return selectSingleNode(xpath) != null
    }

    fun count(xpath: String): Int {
        return selectNodes(xpath).size
    }
}
