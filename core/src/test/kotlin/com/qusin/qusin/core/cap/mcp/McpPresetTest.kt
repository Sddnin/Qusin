package com.qusin.qusin.core.cap.mcp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class McpPresetTest {
    @Test fun resolvesPreRegisteredOAuthClientWithoutExposingSecretInStructure() {
        val catalog = McpCatalogParser.parse(
            """{
              "formatVersion": 1,
              "presets": [{
                "id": "canva", "name": "Canva", "summary": {"en": "Design"},
                "category": "design", "transport": "streamable-http", "auth": "oauth",
                "endpoint": "https://api.canva.com/connect/v1/mcp",
                "officialSource": "https://www.canva.dev/docs/apps/mcp/access/",
                "fields": [
                  {"id":"client_id","label":{"en":"Client ID"},"secret":false,"required":true,"target":"oauth-client-id"},
                  {"id":"client_secret","label":{"en":"Client secret"},"secret":true,"required":true,"target":"oauth-client-secret"}
                ]
              }]
            }""".trimIndent(),
        )
        val preset = catalog.presets.single()
        val resolver = McpPresetResolver()
        val fields = mapOf("client_id" to "public-id", "client_secret" to "private-secret")
        val structure = resolver.resolveStructure(preset, fields - "client_secret")
        assertEquals("public-id", structure.oauthClientId)
        assertNull(structure.oauthClientSecret)
        val resolved = resolver.resolve(preset, fields)
        assertEquals("public-id", resolved.oauthClientId)
        assertEquals("private-secret", resolved.oauthClientSecret)
    }
}
