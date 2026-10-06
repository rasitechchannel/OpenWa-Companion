package org.rasitech.openwacompanion.domain.event

import org.rasitech.openwacompanion.domain.model.CapabilityStatus

data class DomainEvent(
    val eventId: String,
    val seq: Long,
    val accountId: String,
    val eventType: String,
    val rawType: String?,
    val classification: CapabilityStatus,
    val schemaVersion: Int,
    val timestamp: Long,
    val payloadJson: String,
)
