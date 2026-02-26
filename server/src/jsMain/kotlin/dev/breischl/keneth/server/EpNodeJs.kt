package dev.breischl.keneth.server

import dev.breischl.keneth.transport.MessageTransport
import dev.breischl.keneth.transport.TransportListener

internal actual fun EpNode.startPlatformSpecific() { /* no-op: no TCP on JS */
}

internal actual fun defaultOutboundFactory(
    listener: TransportListener?
): (suspend (String, Int) -> MessageTransport)? = null
