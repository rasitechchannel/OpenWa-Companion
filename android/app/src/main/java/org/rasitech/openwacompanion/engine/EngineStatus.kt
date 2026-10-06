package org.rasitech.openwacompanion.engine

import org.json.JSONObject
import java.io.File

data class EngineStatus(
    val connection: String = "unknown",
    val node: String? = null,
    val qr: String? = null,
    val qrDataUrl: String? = null,
    val pairingCode: String? = null,
    val me: String? = null,
    val lastError: String? = null,
    val updatedAt: String? = null,
) {
    companion object {
        fun read(file: File): EngineStatus? {
            if (!file.exists()) return null
            return runCatching {
                val o = JSONObject(file.readText())
                val meObj = o.optJSONObject("me")
                EngineStatus(
                    connection = o.optString("connection", "unknown"),
                    node = o.nullableString("node"),
                    qr = o.nullableString("qr"),
                    qrDataUrl = o.nullableString("qrDataUrl"),
                    pairingCode = o.nullableString("pairingCode"),
                    me = meObj?.nullableString("id"),
                    lastError = o.nullableString("lastError"),
                    updatedAt = o.nullableString("updatedAt"),
                )
            }.getOrNull()
        }

        private fun JSONObject.nullableString(key: String): String? {
            if (!has(key) || isNull(key)) return null
            val value = optString(key)
            return value.takeIf { it.isNotEmpty() && it != "null" }
        }
    }
}
