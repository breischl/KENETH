package dev.breischl.keneth.server

import dev.breischl.keneth.transport.FrameTransport
import dev.breischl.keneth.transport.InMemoryFrameTransport
import dev.breischl.keneth.transport.MessageTransport
import dev.breischl.keneth.transport.OutboundConnector
import dev.breischl.keneth.transport.TransportListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * An in-memory [InboundConnector] and [OutboundConnector] combined into a single rendezvous object,
 * for connecting two in-process [EpNode]s without network I/O.
 *
 * Pass the same instance as the acceptor for one node and the connector for another:
 *
 * ```kotlin
 * val connector = InMemoryBidirectionalConnector()
 *
 * val nodeA = EpNode(identity = identityA, acceptor = connector)
 * val nodeB = EpNode(identity = identityB)
 *
 * nodeA.addPeer(PeerConfig.Inbound("node-b"))
 * nodeB.addPeer(PeerConfig.Outbound("node-a", connector = connector))
 *
 * nodeA.start()
 * nodeB.start()
 * ```
 *
 * When the outbound node calls [connect] (via [EpNode.addPeer]), an [InMemoryFrameTransport]
 * pair is created and the inbound side is queued internally. When the accepting node calls
 * [start], a coroutine drains the queue and passes each transport to [EpNode.accept].
 *
 * @param coroutineContext Additional coroutine context (e.g., a test dispatcher).
 */
class InMemoryBidirectionalConnector(
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
) : InboundConnector, OutboundConnector {

    private val pendingConnections = Channel<FrameTransport>(Channel.UNLIMITED)
    private val scope = CoroutineScope(SupervisorJob() + coroutineContext)

    /**
     * Called by the outbound node when it establishes a connection.
     *
     * Creates an [InMemoryFrameTransport] pair, queues the remote side for the accepting node's
     * [EpNode.accept], and returns the local side for the outbound node's session.
     */
    override suspend fun connect(listener: TransportListener?): MessageTransport {
        val (local, remote) = InMemoryFrameTransport.createPair()
        local.listener = listener
        pendingConnections.send(remote)
        return MessageTransport(local, listener = listener)
    }

    /**
     * Starts the accept loop. Drains queued connections and passes each to [EpNode.accept].
     */
    override fun start(node: EpNode) {
        scope.launch {
            for (frameTransport in pendingConnections) {
                node.accept(MessageTransport(frameTransport, listener = node.transportListener))
            }
        }
    }

    /** Closes the pending connection channel and cancels the accept loop. */
    override fun close() {
        pendingConnections.close()
        scope.cancel()
    }
}
