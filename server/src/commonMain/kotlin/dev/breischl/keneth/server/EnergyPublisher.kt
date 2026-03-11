package dev.breischl.keneth.server

import kotlinx.coroutines.Job

/**
 * Represents an active or stopped energy parameter publisher for a peer.
 *
 * Created via [EpNode.startPublishing] and managed via [EpNode.stopPublishing].
 * This class provides a read-only view of the publishing state.
 *
 * @property peerId The peer this publisher is associated with.
 */
class EnergyPublisher internal constructor(
    val peerId: String,
    @kotlin.concurrent.Volatile internal var _state: PublishingState = PublishingState.ACTIVE,
    internal var job: Job? = null,
) {
    /** Current state of this publisher. */
    val state: PublishingState get() = _state
}
