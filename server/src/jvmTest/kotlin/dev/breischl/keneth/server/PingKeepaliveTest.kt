package dev.breischl.keneth.server

import dev.breischl.keneth.core.messages.Ping
import dev.breischl.keneth.core.messages.SessionParameters
import dev.breischl.keneth.core.messages.SupplyParameters
import dev.breischl.keneth.server.testutils.ChannelFakeFrameTransport
import dev.breischl.keneth.server.testutils.debugNodeListener
import dev.breischl.keneth.server.testutils.debugTransportListener
import dev.breischl.keneth.server.testutils.enqueueMessage
import dev.breischl.keneth.transport.MessageTransport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class PingKeepaliveTest {
    private val serverIdentity = SessionParameters(identity = "test-node", type = "router")
    private val deviceIdentity = SessionParameters(identity = "test-device", type = "charger")
    private val peerId = deviceIdentity.identity

    /**
     * Creates an [EpNode] connected to a fake transport with a completed handshake.
     *
     * Uses `activeReceiveTimeout = 200ms` (the EP default), which gives a derived ping rate
     * of 100ms. `idleReceiveTimeout` is set high to prevent idle timeouts during tests.
     */
    private suspend fun TestScope.createConnectedNode(
        withPeer: Boolean = false,
    ): Pair<EpNode, ChannelFakeFrameTransport> {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val node = EpNode(
            identity = serverIdentity,
            activeReceiveTimeout = 200.milliseconds,
            idleReceiveTimeout = 5.seconds,
            nodeListener = debugNodeListener("node"),
            transportListener = debugTransportListener("node"),
            coroutineContext = dispatcher,
        )
        if (withPeer) {
            node.addPeer(PeerConfig.Inbound(peerId = peerId))
        }

        val fake = ChannelFakeFrameTransport()
        fake.enqueueMessage(deviceIdentity)
        node.accept(MessageTransport(fake))

        // Let handshake complete
        advanceTimeBy(1)

        return node to fake
    }

    /** Returns sent frames that are [Ping] messages. */
    private fun ChannelFakeFrameTransport.pingFrames() =
        sentFrames.filter { it.messageTypeId == Ping.typeId }

    @Test
    fun `ping messages sent when session is idle`() = runTest {
        val (node, fake) = createConnectedNode()

        advanceTimeBy(350)

        assertTrue(fake.pingFrames().size >= 3, "Expected at least 3 pings, got ${fake.pingFrames().size}")

        fake.close()
        node.close()
    }

    @Test
    fun `no ping sent during active publishing`() = runTest {
        val (node, fake) = createConnectedNode(withPeer = true)

        node.startPublishing(peerId, { PublishingParams(supply = SupplyParameters()) })
        fake.sentFrames.clear()

        // Enqueue incoming SupplyParameters to keep the active timeout alive,
        // since the remote is expected to be publishing too during an active transfer.
        fake.enqueueMessage(SupplyParameters())
        advanceTimeBy(100)
        fake.enqueueMessage(SupplyParameters())
        advanceTimeBy(100)

        assertTrue(fake.sentFrames.isNotEmpty(), "Should have sent some frames - test seems to be broken")
        assertTrue(fake.pingFrames().isEmpty(), "Expected no pings during active publishing")
    }

    @Test
    fun `ping resumes after publishing stops`() = runTest {
        val (node, fake) = createConnectedNode(withPeer = true)

        node.startPublishing(peerId, { PublishingParams(supply = SupplyParameters()) })

        // Keep session alive during publishing by sending incoming messages
        fake.enqueueMessage(SupplyParameters())
        advanceTimeBy(100)
        fake.enqueueMessage(SupplyParameters())
        advanceTimeBy(100)

        node.stopPublishing(peerId)

        // Remote also stops publishing (sends Ping), which clears remotePublishingActive
        // and switches the watchdog back to the idle timeout.
        fake.enqueueMessage(Ping)
        advanceTimeBy(1)

        fake.sentFrames.clear()
        advanceTimeBy(350)

        assertTrue(
            fake.pingFrames().size >= 3,
            "Expected at least 3 pings after publishing stopped, got ${fake.pingFrames().size}"
        )

        fake.close()
        node.close()
    }
}
