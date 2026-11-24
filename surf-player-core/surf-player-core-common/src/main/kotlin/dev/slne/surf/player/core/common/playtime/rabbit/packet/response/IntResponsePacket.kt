package dev.slne.surf.player.core.common.playtime.rabbit.packet.response

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class IntResponsePacket(
    val value: Int
) : RabbitResponsePacket()
