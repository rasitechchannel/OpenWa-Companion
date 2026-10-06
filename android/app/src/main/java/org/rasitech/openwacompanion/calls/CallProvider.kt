package org.rasitech.openwacompanion.calls

/**
 * Abstraction for future open-source media call engines.
 * Default implementation never fakes a live voice/video session.
 */
interface CallProvider {
    val supportsLiveMedia: Boolean
    suspend fun accept(callId: String): CallActionResult
    suspend fun reject(callId: String, fromJid: String): CallActionResult
}

sealed class CallActionResult {
    data object SignalingOnly : CallActionResult()
    data object UnsupportedLiveMedia : CallActionResult()
    data class Failed(val reason: String) : CallActionResult()
}

class UnsupportedLiveCallProvider(
    private val rejectSignaling: suspend (callId: String, fromJid: String) -> Unit,
) : CallProvider {
    override val supportsLiveMedia: Boolean = false

    override suspend fun accept(callId: String): CallActionResult =
        CallActionResult.UnsupportedLiveMedia

    override suspend fun reject(callId: String, fromJid: String): CallActionResult =
        runCatching {
            rejectSignaling(callId, fromJid)
            CallActionResult.SignalingOnly
        }.getOrElse { CallActionResult.Failed(it.message ?: "reject failed") }
}
