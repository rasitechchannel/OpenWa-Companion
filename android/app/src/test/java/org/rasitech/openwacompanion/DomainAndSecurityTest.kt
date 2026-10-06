package org.rasitech.openwacompanion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.rasitech.openwacompanion.domain.model.CapabilityStatus
import org.rasitech.openwacompanion.security.LogRedactor

class DomainAndSecurityTest {
    @Test
    fun capabilityStatusContainsRequiredValues() {
        val names = CapabilityStatus.entries.map { it.name }.toSet()
        assertTrue(names.contains("IMPLEMENTED_UI"))
        assertTrue(names.contains("CAPTURED_ONLY"))
        assertTrue(names.contains("PENDING_PHYSICAL_DEVICE_TEST"))
        assertTrue(names.contains("UNSUPPORTED_UPSTREAM_ACTION"))
    }

    @Test
    fun logRedactorMasksAuthKeys() {
        val raw = """{"noiseKey":"abc","signedIdentityKey":"xyz","ok":true}"""
        val redacted = LogRedactor.redact(raw)
        assertTrue(redacted.contains("[REDACTED]"))
        assertTrue(!redacted.contains("\"abc\""))
    }

    @Test
    fun coverageMatrixHasNoTodo() {
        val matrix = java.io.File("../../../../../../docs/BAILEYS_COVERAGE_MATRIX.md")
        // Unit tests run from module; also check packaged absolute fallback.
        val file = listOf(
            java.io.File("E:/OpenWA/docs/BAILEYS_COVERAGE_MATRIX.md"),
            java.io.File("../docs/BAILEYS_COVERAGE_MATRIX.md"),
            matrix,
        ).firstOrNull { it.exists() }
        assertTrue("coverage matrix missing", file != null && file!!.exists())
        val text = file!!.readText()
        assertTrue(text.contains("TODO_REMAINING: 0"))
        assertEquals(false, Regex("""\|\s*TODO\s*\|""").containsMatchIn(text))
    }
}
