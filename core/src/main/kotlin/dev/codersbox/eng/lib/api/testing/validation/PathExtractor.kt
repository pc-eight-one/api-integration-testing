package dev.codersbox.eng.lib.api.testing.validation

interface PathExtractor {
    val supportedContentTypes: List<String>
    fun extract(content: ByteArray, path: String): Any?
    fun extractAll(content: ByteArray, path: String): List<Any?>
    fun canHandle(contentType: String): Boolean
}

object PathExtractorRegistry {
    private val extractors = mutableListOf<PathExtractor>()
    
    init {
        register(JsonPathExtractor())
        register(XmlPathExtractor())
        register(CsvPathExtractor())
        register(YamlPathExtractor())
    }
    
    fun register(extractor: PathExtractor) {
        extractors.add(0, extractor)
    }
    
    fun getExtractor(contentType: String): PathExtractor? {
        return extractors.firstOrNull { it.canHandle(contentType) }
    }
    
    fun extract(content: ByteArray, path: String, contentType: String): Any? {
        val extractor = getExtractor(contentType) 
            ?: throw IllegalArgumentException("No extractor found for content type: $contentType")
        return extractor.extract(content, path)
    }
    
    fun extractAll(content: ByteArray, path: String, contentType: String): List<Any?> {
        val extractor = getExtractor(contentType)
            ?: throw IllegalArgumentException("No extractor found for content type: $contentType")
        return extractor.extractAll(content, path)
    }
}
