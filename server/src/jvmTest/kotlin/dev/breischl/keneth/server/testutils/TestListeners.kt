package dev.breischl.keneth.server.testutils

import dev.breischl.keneth.core.messages.Message
import dev.breischl.keneth.core.messages.SoftDisconnect
import dev.breischl.keneth.server.NodeListener
import dev.breischl.keneth.server.SessionSnapshot
import dev.breischl.keneth.transport.CborSnapshot
import dev.breischl.keneth.transport.ReceivedMessage
import dev.breischl.keneth.transport.TransportListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration

/**
 * A [dev.breischl.keneth.server.NodeListener] that prints every session and peer event to stdout, prefixed with
 * [tag] and the current virtual time from [kotlinx.coroutines.test.TestScope.testScheduler].
 *
 * Useful for debugging timing-sensitive tests. Pass it to [dev.breischl.keneth.server.EpNode] via:
 * ```
 * EpNode(..., nodeListener = debugNodeListener("my-node"))
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun TestScope.debugNodeListener(tag: String) = object : NodeListener {
    private fun t() = testScheduler.currentTime
    override fun onSessionCreated(session: SessionSnapshot) =
        println("[$tag t=${t()}] sessionCreated ${session.sessionId}")

    override fun onSessionActive(session: SessionSnapshot) =
        println("[$tag t=${t()}] sessionActive remoteIdentity=${session.remoteIdentity}")

    override fun onSessionHandshakeFailed(session: SessionSnapshot, reason: String) =
        println("[$tag t=${t()}] handshakeFailed reason=$reason")

    override fun onSessionDisconnecting(session: SessionSnapshot, softDisconnect: SoftDisconnect?) =
        println("[$tag t=${t()}] disconnecting softDisconnect=$softDisconnect")

    override fun onSessionTimeout(session: SessionSnapshot, timeoutDuration: Duration) =
        println("[$tag t=${t()}] TIMEOUT after=$timeoutDuration")

    override fun onSessionClosed(session: SessionSnapshot) =
        println("[$tag t=${t()}] sessionClosed")

    override fun onSessionError(session: SessionSnapshot, error: Throwable) =
        println("[$tag t=${t()}] ERROR $error")

    override fun onMessageReceived(session: SessionSnapshot, message: Message) =
        println("[$tag t=${t()}] msgReceived ${message::class.simpleName}")

    override fun onMessageSent(session: SessionSnapshot, message: Message) =
        println("[$tag t=${t()}] msgSent ${message::class.simpleName}")

    override fun onPeerConnected(session: SessionSnapshot) =
        println("[$tag t=${t()}] peerConnected peerId=${session.peerId}")

    override fun onPeerDisconnected(session: SessionSnapshot) =
        println("[$tag t=${t()}] peerDisconnected peerId=${session.peerId}")
}

/**
 * A [dev.breischl.keneth.transport.TransportListener] that prints every send and receive to stdout, prefixed with
 * [tag] and the current virtual time.
 *
 * Useful alongside [debugNodeListener] to see exactly which messages flow through the transport:
 * ```
 * EpNode(..., transportListener = debugTransportListener("my-node"))
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun TestScope.debugTransportListener(tag: String) = object : TransportListener {
    private fun t() = testScheduler.currentTime
    override fun onMessageSending(message: Message, payloadCbor: CborSnapshot) =
        println("[$tag t=${t()}] SEND ${message::class.simpleName}")

    override fun onMessageReceived(received: ReceivedMessage, payloadCbor: CborSnapshot?) =
        println("[$tag t=${t()}] RECV ${received.message?.let { it::class.simpleName } ?: "FAILED"}")
}