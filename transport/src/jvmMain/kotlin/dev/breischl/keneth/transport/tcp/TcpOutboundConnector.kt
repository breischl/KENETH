package dev.breischl.keneth.transport.tcp

import dev.breischl.keneth.transport.MessageTransport
import dev.breischl.keneth.transport.OutboundConnector
import dev.breischl.keneth.transport.TransportListener

/** The default EnergyNet Protocol TCP port. */
const val EP_DEFAULT_PORT = 56540

/**
 * A [OutboundConnector] that establishes outbound TCP connections.
 *
 * @param host Hostname or IP address of the remote peer.
 * @param port TCP port of the remote peer. Defaults to [EP_DEFAULT_PORT].
 */
class TcpOutboundConnector(val host: String, val port: Int = EP_DEFAULT_PORT) : OutboundConnector {
    override suspend fun connect(listener: TransportListener?): MessageTransport =
        MessageTransport(RawTcpClientTransport(host, port, listener))
}
