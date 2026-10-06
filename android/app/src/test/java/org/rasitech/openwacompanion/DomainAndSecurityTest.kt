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
        val start = java.io.File(System.getProperty("user.dir")).absoluteFile
        val file = generateSequence(start) { current -> current.parentFile }
            .map { root -> java.io.File(root, "docs/BAILEYS_COVERAGE_MATRIX.md") }
            .firstOrNull { candidate -> candidate.isFile }
        assertTrue("coverage matrix missing from repo tree", file != null)
        val text = file!!.readText()
        assertTrue(text.contains("TODO_REMAINING: 0"))
        assertEquals(false, Regex("""\|\s*TODO\s*\|""").containsMatchIn(text))
    }
}
