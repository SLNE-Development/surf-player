package dev.slne.surf.player.core.common.playtime.rpc

import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.rabbitmq.api.InternalRabbitMQ
import dev.slne.surf.rabbitmq.api.RabbitMQApi
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * Round trips of the [PlaytimeRpcService] payloads that rely on contextual serializers.
 */
@OptIn(InternalRabbitMQ::class, ExperimentalSerializationApi::class)
class PlaytimeRpcSerializationTest {
    private val cbor = RabbitMQApi.createCbor(EmptySerializersModule())

    private fun <T> roundTrip(serializer: KSerializer<T>, value: T): T =
        cbor.decodeFromByteArray(serializer, cbor.encodeToByteArray(serializer, value))

    @Test
    fun `filter with restrictions survives a round trip`() {
        val filter = PlaytimeFilter(
            from = LocalDateTime.parse("2026-11-02T00:00:00"),
            to = LocalDateTime.parse("2026-11-03T00:00:00"),
            servers = setOf("Survival Events"),
            categories = emptySet(),
            players = setOf(UUID.randomUUID(), UUID.randomUUID())
        )

        assertEquals(filter, roundTrip(PlaytimeFilter.serializer(), filter))
    }

    @Test
    fun `filter without restrictions survives a round trip`() {
        val filter = PlaytimeFilter(
            from = LocalDateTime.parse("2026-11-02T00:00:00"),
            to = LocalDateTime.parse("2026-11-03T00:00:00")
        )

        assertEquals(filter, roundTrip(PlaytimeFilter.serializer(), filter))
    }

    @Test
    fun `playtime per player built from a fastutil map survives a round trip`() {
        val playtime = Object2LongOpenHashMap<UUID>().apply {
            put(UUID.randomUUID(), 3600)
            put(UUID.randomUUID(), 61)
        }

        val decoded = roundTrip(cbor.serializersModule.serializer<Map<UUID, Long>>(), playtime)

        assertEquals(HashMap(playtime), HashMap(decoded))
    }
}
