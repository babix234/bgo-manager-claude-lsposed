package com.mgomanager.app.domain.util

import org.w3c.dom.Element
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

data class ExtractedIds(
    val userId: String,
    val gaid: String = "nicht vorhanden",
    val deviceToken: String = "nicht vorhanden",
    val appSetId: String = "nicht vorhanden",
    val ssaid: String = "nicht vorhanden"
)

@Singleton
class IdExtractor @Inject constructor() {

    /**
     * Extract IDs from playerprefs XML file
     */
    fun extractIdsFromPlayerPrefs(xmlFile: File): Result<ExtractedIds> {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(xmlFile)

            val userId = extractStringValue(doc, "Scopely.Attribution.UserId")
            val gaid = extractStringValue(doc, "GoogleAdId") ?: "nicht vorhanden"
            val deviceToken = extractStringValue(doc, "LastOpenedDeviceToken") ?: "nicht vorhanden"
            val appSetId = extractStringValue(doc, "AppSetId") ?: "nicht vorhanden"

            if (userId == null) {
                Result.failure(Exception("User ID nicht gefunden (MANDATORY)"))
            } else {
                Result.success(ExtractedIds(userId, gaid, deviceToken, appSetId))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract SSAID from settings_ssaid.xml using regex
     * Format: com.scopely.monopolygo/<16-char-hex-ssaid>
     */
    fun extractSsaid(xmlFile: File): String {
        return try {
            val content = xmlFile.readText()
            val regex = Regex("""com\.scopely\.monopolygo/([a-f0-9]{16})""")
            val match = regex.find(content)
            match?.groupValues?.get(1) ?: "nicht vorhanden"
        } catch (e: Exception) {
            "nicht vorhanden"
        }
    }

    private fun extractStringValue(doc: org.w3c.dom.Document, name: String): String? {
        val nodeList = doc.getElementsByTagName("string")
        for (i in 0 until nodeList.length) {
            val element = nodeList.item(i) as Element
            if (element.getAttribute("name") == name) {
                return element.textContent
            }
        }
        return null
    }
}
