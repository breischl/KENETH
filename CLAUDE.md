# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew build    # Build all targets
./gradlew check    # Run all checks including tests and detekt
./gradlew clean    # Clean build outputs
./gradlew detekt   # Run detekt static analysis only

# Run tests for a specific module (all platforms)
./gradlew :core:allTests
./gradlew :transport:allTests

# Run tests for a specific platform
./gradlew :core:jvmTest
./gradlew :core:jsTest           # Runs Node.js and browser tests
./gradlew :core:jsNodeTest       # Node.js only
./gradlew :core:jsBrowserTest    # Headless Chrome only

# Run a single test class (JVM)
./gradlew :core:jvmTest --tests "dev.breischl.keneth.core.MyTestClass"
```

## Dev Setup

See [DEV_SETUP.md](dev-docs/DEV_SETUP.md) for prerequisites (JDK, Chrome for browser tests).

## Architecture

This is a Kotlin Multiplatform multi-module Gradle project with centralized build configuration.

### Module Structure

- **core/** - EnergyNet Protocol (EP) core library with message models, value types, CBOR serialization, and frame encoding
- **transport/** - Transport abstractions and TCP/TLS implementations for EP
- **server/** - EP server with session management, peer tracking, and energy parameter publishing
- **web/** - Browser-based demos (JS-only, does not use the shared convention plugin). See [web/README.md](web/README.md)
- **buildSrc/** - Convention plugins for shared build logic across modules

### Platform Targets

All library modules target three platforms via the `kotlin-multiplatform` convention plugin (the `web` module is JS-only):

- **JVM** — full implementation; tests run with JUnit Platform
- **JS (IR)** — Node.js and browser (Karma + headless Chrome) test runners
- **linuxArm64** — native binary target

Platform-specific source sets follow the standard KMP layout:

| Source set   | Used for                                       |
|--------------|------------------------------------------------|
| `commonMain` | Platform-agnostic code (most logic lives here) |
| `jvmMain`    | JVM-specific: TCP/TLS sockets                  |
| `jsMain`     | JS-specific stubs and platform actuals         |
| `nativeMain` | Native (linuxArm64) stubs and platform actuals |
| `jvmTest`    | JVM tests (all current tests live here)        |

### Build Configuration

- **JDK 24** (Temurin) via Gradle toolchains
- **Kotlin 2.3.0** with kotlinx ecosystem (coroutines, serialization, datetime)
- Multiplatform convention plugin at `buildSrc/src/main/kotlin/kotlin-multiplatform.gradle.kts` configures all targets,
  JUnit Platform, detekt, and compiler options for all modules
- JVM-only convention plugin at `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts` is available for JVM-only modules
- Version catalog at `gradle/libs.versions.toml` manages all dependency versions
- **detekt** 2.0.0-alpha.2 for static analysis, configured at `config/detekt/detekt.yml`
- Build cache and configuration cache enabled in `gradle.properties`

## Making Changes

When making code changes, use red/green TDD (tests first, ensure they fail, then make them pass). Particularly for
bugfixes. Tests should be named in such that the code under test, situation being tested, and expected result are all
visible in the test method name.

In production code, public classes and methods intended as part of the API surface should always documentation comments.
Non-API classes and methods should have comments unless they are trivial. Test classes and methods only require comments
for tricky or complex areas.

Prefer to put classes in separate files, except when they are very small, inner classes, companions, etc.

Keep the README.md and CLAUDE.md updated as appropriate. 

## Server Module Notes

### Key classes (post-0.2.0)

- `EpNode` is the sole entry point — manages sessions, peers, and energy parameter publishing
- `NodeListener` is the callback interface; `SessionSnapshot` is the immutable event payload
- `startPublishing(peerId, paramsProvider: () -> PublishingParams, tickRate)` takes a **callback**, not static params;
  the callback is invoked each tick so callers update captured state to change params mid-session

### Server test utilities (jvmTest)

Shared helpers live in `testutils`. Do not duplicate these in individual test files.

`ServerTestUtils.kt`:
- `testCbor` — shared CBOR codec
- `encodeMessage(message)`, `frameResultFor(message)` — encode messages into frames
- `channelTransportWithMessages(vararg messages)` — creates a `ChannelFakeFrameTransport` pre-loaded with messages

`ChannelFakeFrameTransport.kt`:
- `ChannelFakeFrameTransport.enqueueMessage(message)` extension — enqueue a message mid-test
- `ChannelFakeFrameTransport` — fake transport backed by a `Channel`; stays open until explicitly closed

`TestListeners.kt`:

- `TestScope.debugNodeListener(tag)` — `NodeListener` that prints every event with virtual time and tag; pass to
  `EpNode(nodeListener = ...)` when debugging
- `TestScope.debugTransportListener(tag)` — `TransportListener` that prints sends/receives; pass to
  `EpNode(transportListener = ...)`

### Virtual-time test timing pitfall

**`testScheduler.advanceUntilIdle()` advances the virtual clock**, not just "drain immediately-ready coroutines". It
runs every scheduled delay (including publishing tick loops, ping loops) until no tasks remain — which can be all the
way
to when the session timeout fires. Use `advanceTimeBy(N)` when you need to assert state at a specific point in time.
Only use `advanceUntilIdle()` when you explicitly want to run the system to its natural conclusion (e.g., asserting a
session eventually closes).

## Versioning

When starting on a new feature, bump the version in `gradle.properties`. Follow standard SemVer guidelines for the
expected size and type of the change. Development versions should end in `-SNAPSHOT` - the release process will handle
setting non-`SNAPSHOT` versions.