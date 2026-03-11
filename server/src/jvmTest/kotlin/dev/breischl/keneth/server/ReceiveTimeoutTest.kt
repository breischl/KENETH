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
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ReceiveTimeoutTest {
    private val serverIdentity = SessionParameters(identity = "test-node", type = "router")
    private val deviceIdentity = SessionParameters(identity = "test-device", type = "charger")
    private val peerId = deviceIdentity.identity

    /** Creates an [EpNode] connected to a fake transport with a completed handshake. */
    private suspend fun TestScope.createConnectedNode(
        activeReceiveTimeout: Duration = 200.milliseconds,
        idleReceiveTimeout: Duration = 5.seconds,
        withPeer: Boolean = false,
    ): Triple<EpNode, ChannelFakeFrameTransport, DeviceSession> {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val node = EpNode(
            identity = serverIdentity,
            activeReceiveTimeout = activeReceiveTimeout,
            idleReceiveTimeout = idleReceiveTimeout,
            nodeListener = debugNodeListener("node"),
            transportListener = debugTransportListener("node"),
            coroutineContext = dispatcher,
        )
        if (withPeer) {
            node.addPeer(PeerConfig.Inbound(peerId = peerId))
        }

        val fake = ChannelFakeFrameTransport()
        fake.enqueueMessage(deviceIdentity)
        val session = node.accept(MessageTransport(fake))

        advanceTimeBy(1)

        return Triple(node, fake, session)
    }

    // ── idle timeout ──────────────────────────────────────────────────────────

    @Test
    fun `session closed after idle timeout with no publishing`() = runTest {
        val (node, _, session) = createConnectedNode()
        assertEquals(SessionState.ACTIVE, session.state)

        // Advance past idle timeout — no further messages
        advanceTimeBy(5001)
        testScheduler.advanceUntilIdle()

        assertEquals(SessionState.CLOSED, session.state)
        node.close()
    }

    @Test
    fun `session not closed before idle timeout`() = runTest {
        val (node, fake, session) = createConnectedNode()
        assertEquals(SessionState.ACTIVE, session.state)

        // Just before the idle timeout — timeout fires at 5001ms
        advanceTimeBy(4999)

        assertEquals(SessionState.ACTIVE, session.state)
        fake.close()
        node.close()
    }

    @Test
    fun `incoming message resets idle timeout`() = runTest {
        val (node, fake, session) = createConnectedNode()

        // Advance 4 seconds, then send a Ping — this resets the clock
        advanceTimeBy(4000)
        fake.enqueueMessage(Ping)
        advanceTimeBy(1) // process the Ping so the watchdog resets

        // Advance another 4 seconds — still within 5s of the Ping
        advanceTimeBy(4000)

        assertEquals(SessionState.ACTIVE, session.state)
        fake.close()
        node.close()
    }

    // ── active timeout (remote publishing) ───────────────────────────────────

    @Test
    fun `session closed after active timeout when remote is publishing`() = runTest {
        val (node, fake, session) = createConnectedNode()

        // Remote starts publishing — sends one supply message
        fake.enqueueMessage(SupplyParameters())
        advanceTimeBy(1)
        assertEquals(SessionState.ACTIVE, session.state)

        // Advance past active timeout — remote goes silent
        advanceTimeBy(201)
        testScheduler.advanceUntilIdle()

        assertEquals(SessionState.CLOSED, session.state)
        node.close()
    }

    @Test
    fun `active timeout is tighter than idle timeout when remote publishes`() = runTest {
        val (node, fake, session) = createConnectedNode()

        // Remote sends a supply message — triggers active timeout mode
        fake.enqueueMessage(SupplyParameters())
        advanceTimeBy(1)

        // 300ms passes — active timeout fires, not the idle timeout
        advanceTimeBy(300)
        testScheduler.advanceUntilIdle()

        assertEquals(SessionState.CLOSED, session.state)
        node.close()
    }

    // ── active timeout (local publishing) ────────────────────────────────────

    @Test
    fun `local publishing triggers tight receive timeout`() = runTest {
        val (node, _, session) = createConnectedNode(withPeer = true)

        // Local node starts publishing (sending to remote), but remote sends nothing.
        // Per EP spec, both sides should be publishing during an energy transfer,
        // so local publishing activates the tight timeout.
        node.startPublishing(peerId, { PublishingParams(supply = SupplyParameters()) })
        advanceTimeBy(1) // run first publishing tick

        // Advance past the active timeout — remote didn't respond with energy params
        advanceTimeBy(201)
        testScheduler.advanceUntilIdle()

        assertEquals(SessionState.CLOSED, session.state)
        node.close()
    }

    // ── timeout reset ────────────────────────────────────────────────────────

    @Test
    fun `active timeout resets to idle when remote sends Ping`() = runTest {
        val (node, fake, session) = createConnectedNode()

        // Enqueue both messages before advancing time. The collect coroutine drains all
        // buffered channel items in one pass, so the conflated heartbeat channel delivers
        // only the final state (remotePublishingActive=false from Ping) to the watchdog —
        // the 200ms timer is never started.
        fake.enqueueMessage(SupplyParameters())
        fake.enqueueMessage(Ping)
        advanceTimeBy(1)

        // 300ms passes — should NOT timeout, back to 5s idle timeout
        advanceTimeBy(300)

        assertEquals(SessionState.ACTIVE, session.state)
        fake.close()
        node.close()
    }
}
