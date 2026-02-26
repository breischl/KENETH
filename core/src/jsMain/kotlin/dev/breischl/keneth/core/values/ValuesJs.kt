package dev.breischl.keneth.core.values

import kotlinx.serialization.KSerializer
import kotlin.time.Instant

actual value class Amount(actual val value: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x02
        actual fun serializer(): KSerializer<Amount> = AmountSerializer
    }
}

actual value class Binary(actual val bytes: ByteArray) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x04
        actual fun serializer(): KSerializer<Binary> = BinarySerializer
    }
}

actual value class Currency(actual val code: String) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x05
        actual fun serializer(): KSerializer<Currency> = CurrencySerializer
    }
}

actual value class Current(actual val amperes: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x11
        actual fun serializer(): KSerializer<Current> = CurrentSerializer
    }
}

actual value class Duration(actual val millis: Long) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x06
        actual fun serializer(): KSerializer<Duration> = DurationSerializer
    }
}

actual value class Energy(actual val wattHours: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x13
        actual fun serializer(): KSerializer<Energy> = EnergySerializer
    }
}

actual value class Flag(actual val value: Boolean) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x01
        actual fun serializer(): KSerializer<Flag> = FlagSerializer
    }
}

actual value class Percentage(actual val percent: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x14
        actual fun serializer(): KSerializer<Percentage> = PercentageSerializer
    }
}

actual value class Power(actual val watts: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x12
        actual fun serializer(): KSerializer<Power> = PowerSerializer
    }
}

actual value class Resistance(actual val ohms: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x15
        actual fun serializer(): KSerializer<Resistance> = ResistanceSerializer
    }
}

actual value class Text(actual val value: String) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x00
        actual fun serializer(): KSerializer<Text> = TextSerializer
    }
}

actual value class Timestamp(actual val instant: Instant) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x03
        actual fun serializer(): KSerializer<Timestamp> = TimestampSerializer
    }
}

actual value class Voltage(actual val volts: Double) {
    actual companion object {
        actual const val TYPE_ID: Int = 0x10
        actual fun serializer(): KSerializer<Voltage> = VoltageSerializer
    }
}
