# Developer Setup

## Build

This project uses [Gradle](https://gradle.org/).

* Run `./gradlew build` to build all targets.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.
* Run `./gradlew :core:jvmTest` (or `:transport:jvmTest`, `:server:jvmTest`) for JVM tests on a specific module.
* Run `./gradlew :core:allTests` to run tests across all platforms for a module.

See [CLAUDE.md](CLAUDE.md) for full build command reference.

## Useful References

- [EnergyNet Protocol spec](https://github.com/energyetf/energynet)
- [CBOR spec](https://cbor.io/)
- [cbor.me](https://cbor.me/) is a handy CBOR debugging tool

## JDK 25

Required for compilation and tests. Managed automatically via Gradle toolchains — Gradle will
use an existing JDK 25 installation if one is available, or you can install
[Temurin 25](https://adoptium.net/temurin/releases/?version=25) manually.

## Google Chrome

Required for JavaScript browser tests (`jsBrowserTest`, `jsTest`, `allTests`). Node.js-only
tests (`jsNodeTest`) do not need it.

Install the appropriate package for your platform:

| Platform      | Package                                                          |
|---------------|------------------------------------------------------------------|
| Debian/Ubuntu | `sudo apt-get install google-chrome-stable`                      |
| Fedora/RHEL   | `sudo dnf install google-chrome-stable`                          |
| macOS         | Install [Google Chrome](https://www.google.com/chrome/) normally |
| Windows       | Install [Google Chrome](https://www.google.com/chrome/) normally |

If Chrome is installed in a non-standard location, set the `CHROME_BIN` environment variable to
its path before running Gradle:

```bash
export CHROME_BIN=/path/to/google-chrome
./gradlew check
```
