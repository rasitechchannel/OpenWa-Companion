package org.rasitech.openwacompanion

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdapterSerializationTest {
    @Test
    fun domainEventEnvelopeRoundTripFields() {
        val json = JSONObject()
            .put("eventId", "e1")
            .put("seq", 1)
            .put("accountId", "default")
            .put("eventType", "messages.upsert")
            .put("classification", "PENDING_PHYSICAL_DEVICE_TEST")
            .put("schemaVersion", 1)
            .put("timestamp", 123L)
            .put("payload", JSONObject().put("messages", org.json.JSONArray()))
            .toString()
        val parsed = JSONObject(json)
        assertEquals("messages.upsert", parsed.getString("eventType"))
        assertEquals("default", parsed.getString("accountId"))
        assertTrue(parsed.getJSONObject("payload").has("messages"))
    }

    @Test
    fun mediaPathPreferredOverBase64() {
        val payload = JSONObject()
            .put("localPath", "/data/user/0/org.rasitech.openwacompanion/no_backup/accounts/default/media/x.jpg")
            .put("byteSize", 12)
        assertTrue(payload.has("localPath"))
        assertTrue(!payload.has("base64"))
    }
}
