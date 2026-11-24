package dev.slne.surf.player.core.common.playtime.rabbit.packet.request

import dev.slne.surf.player.core.common.playtime.rabbit.packet.response.IntResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class RecalculateAllPlaytimeStreaksRequestPacket : RabbitRequestPacket<IntResponsePacket>()
