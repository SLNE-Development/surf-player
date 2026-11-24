package dev.slne.surf.player.core.common.playtime.rabbit.packet.request

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.player.core.common.playtime.rabbit.packet.response.PlaytimeStreakResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class RecalculatePlaytimeStreakRequestPacket(
    val playerUuid: SerializableUUID
) : RabbitRequestPacket<PlaytimeStreakResponsePacket>()
