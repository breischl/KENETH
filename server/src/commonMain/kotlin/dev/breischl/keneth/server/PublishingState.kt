package dev.breischl.keneth.server

/**
 * Lifecycle state of an [EnergyPublisher].
 */
enum class PublishingState {
    /** The publisher is actively sending parameters at the configured tick rate. */
    ACTIVE,

    /** Publishing has been stopped (manually or due to peer disconnect/error). */
    STOPPED,
}
