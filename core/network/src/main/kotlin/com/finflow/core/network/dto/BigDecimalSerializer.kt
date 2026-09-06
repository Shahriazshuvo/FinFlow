package com.finflow.core.network.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import java.math.BigDecimal

/**
 * Postgres `numeric(12,2)` must never round-trip through a `Double`. PostgREST may emit a
 * numeric as a bare JSON number or as a string depending on configuration, so this reads
 * whichever arrives via its raw text and writes an unquoted literal so Postgres receives a
 * number, not a string.
 */
internal object BigDecimalSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("java.math.BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return BigDecimal(decoder.decodeString())
        return BigDecimal(jsonDecoder.decodeJsonElement().let { it as JsonPrimitive }.content)
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder == null) {
            encoder.encodeString(value.toPlainString())
        } else {
            jsonEncoder.encodeJsonElement(JsonUnquotedLiteral(value.toPlainString()))
        }
    }
}
