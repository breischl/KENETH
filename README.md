# KENETh: Kotlin EnergyNET Protocol Library

[![CI](https://github.com/breischl/KENETh/actions/workflows/ci.yml/badge.svg)](https://github.com/breischl/KENETh/actions/workflows/ci.yml)

## Overview

A Kotlin Multiplatform library for implementing the [EnergyNet Protocol (EP)](https://github.com/energyetf/energynet) —
an open protocol for energy transfer coordination between devices such as EVs, chargers, and energy routers.

KENETh provides:

- **CBOR serialization** of all EP message types and value types
- **Frame encoding/decoding** for the EP wire format
- **TCP and TLS transport** for sending and receiving EP messages
- **Server-side session management** with EP handshake enforcement, peer tracking, and energy parameter publishing

## Modules

### [keneth-core](core/README.md)

Core EP library: typed value classes (`Voltage`, `Current`, `Power`, etc.), message models, CBOR serialization, and
frame encoding/decoding. Fully cross-platform.

### [keneth-transport](transport/README.md)

Transport layer: `MessageTransport` and `FrameTransport` interfaces with TCP and TLS implementations. Handles frame
encoding/decoding and message parsing automatically. TCP/TLS socket implementations are JVM-only.

### [keneth-server](server/README.md)

EP server: `EpNode` manages peer connections, EP session handshakes, and energy parameter publishing. Supports named
peers with inbound matching by identity and configurable publish rates. Server logic is cross-platform; TCP accept loop
is JVM-only.

## Platform Support

All modules target JVM, JS (IR), and linuxArm64 via Kotlin Multiplatform. Platform-agnostic code lives in `commonMain`;
platform-specific implementations (TCP/TLS sockets, TCP accept loop) are in `jvmMain`.

## Development

See [DEV_SETUP.md](DEV_SETUP.md) for prerequisites, build instructions, and useful references.
