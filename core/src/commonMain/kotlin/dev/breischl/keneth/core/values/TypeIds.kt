package dev.breischl.keneth.core.values

/**
 * CBOR type identifier constants for EnergyNet Protocol value types.
 *
 * These are the map keys used when encoding EP values as CBOR maps.
 * Defined in EnergyNet Protocol section 3.1.
 */
internal object TypeIds {
    /** Text string (UTF-8). */
    const val TEXT: Int = 0x00

    /** Boolean flag. */
    const val FLAG: Int = 0x01

    /** Monetary amount. */
    const val AMOUNT: Int = 0x02

    /** UTC timestamp (epoch milliseconds). */
    const val TIMESTAMP: Int = 0x03

    /** Raw binary data. */
    const val BINARY: Int = 0x04

    /** ISO 4217 currency code. */
    const val CURRENCY: Int = 0x05

    /** Duration (milliseconds). */
    const val DURATION: Int = 0x06

    /** Electrical voltage (volts). */
    const val VOLTAGE: Int = 0x10

    /** Electrical current (amperes). */
    const val CURRENT: Int = 0x11

    /** Electrical power (watts). */
    const val POWER: Int = 0x12

    /** Electrical energy (watt-hours). */
    const val ENERGY: Int = 0x13

    /** Percentage (0.0–100.0). */
    const val PERCENTAGE: Int = 0x14

    /** Electrical resistance (ohms). */
    const val RESISTANCE: Int = 0x15

    /** Min/max numeric bounds. */
    const val BOUNDS: Int = 0x20

    /** Price forecast schedule. */
    const val PRICE_FORECAST: Int = 0x30

    /** Energy source mix. */
    const val SOURCE_MIX: Int = 0x40

    /** Energy mix breakdown. */
    const val ENERGY_MIX: Int = 0x41

    /** Isolation measurement state. */
    const val ISOLATION_STATE: Int = 0x50
}
