package dev.breischl.keneth.server

/**
 * Result of attempting to start energy parameter publishing via [EpNode.startPublishing].
 *
 * Use a `when` expression to handle all cases:
 * ```kotlin
 * when (val result = node.startPublishing("charger-1", params)) {
 *     is StartPublishingResult.Success -> println("Publishing started: ${result.publisher.peerId}")
 *     is StartPublishingResult.PeerNotFound -> println("Unknown peer: ${result.peerId}")
 *     is StartPublishingResult.PeerNotConnected -> println("Peer not connected: ${result.peerId}")
 *     is StartPublishingResult.PublishingAlreadyActive -> println("Already publishing")
 * }
 * ```
 */
sealed class StartPublishingResult {
    /** Publishing started successfully. */
    data class Success(val publisher: EnergyPublisher) : StartPublishingResult()

    /** No peer with this ID is configured. */
    data class PeerNotFound(val peerId: String) : StartPublishingResult()

    /** The peer exists but has not completed the EP handshake. */
    data class PeerNotConnected(val peerId: String) : StartPublishingResult()

    /** Publishing is already active for this peer. */
    data class PublishingAlreadyActive(val peerId: String) : StartPublishingResult()
}
