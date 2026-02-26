package dev.breischl.keneth.core.frames

import dev.breischl.keneth.core.diagnostics.DiagnosticContext
import dev.breischl.keneth.core.parsing.ParseResult
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.SequenceInputStream

/**
 * Reads and decodes a single frame from an input stream.
 *
 * Uses CBOR structure awareness to determine frame boundaries in the byte stream.
 * Each call reads exactly one complete frame. Returns null on clean EOF (no more frames
 * available), or a [ParseResult] for success or failure.
 *
 * @param inputStream The stream to read from.
 * @param maxBytes Maximum number of bytes to read for a single frame.
 * @return A ParseResult on success or failure, or null on clean EOF.
 */
fun FrameCodec.decodeFromStream(
    inputStream: InputStream,
    maxBytes: Long = InputStreamByteReader.DEFAULT_MAX_BYTES,
): ParseResult<Frame>? {
    // Read first byte — EOF here means no more frames (clean end of stream)
    val first = inputStream.read()
    if (first == -1) return null

    // Read second byte — if EOF here the frame is incomplete
    val second = inputStream.read()
    if (second == -1) return streamError("READ_ERROR", "Unexpected end of stream after first byte")

    // Prepend the two already-read bytes so decodeFromReader sees the full frame
    val prepend = ByteArrayInputStream(byteArrayOf(first.toByte(), second.toByte()))
    val reader = InputStreamByteReader(SequenceInputStream(prepend, inputStream), maxBytes)
    return decodeFromReader(reader, DiagnosticContext.get())
}
