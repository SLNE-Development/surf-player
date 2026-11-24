package dev.slne.surf.player.core.common.playtime.rabbit.packet.response

import dev.slne.surf.player.api.playtime.session.PlaytimeSession
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class ManySessionsResponsePacket(
    val sessions: Set<PlaytimeSession>
) : RabbitResponsePacket()
