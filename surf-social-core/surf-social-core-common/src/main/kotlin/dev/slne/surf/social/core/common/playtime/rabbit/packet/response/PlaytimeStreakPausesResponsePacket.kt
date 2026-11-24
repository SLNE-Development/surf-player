package dev.slne.surf.social.core.common.playtime.rabbit.packet.response

import dev.slne.surf.social.api.playtime.session.PlaytimeStreakPause
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class PlaytimeStreakPausesResponsePacket(
    val pauses: List<PlaytimeStreakPause>
) : RabbitResponsePacket()
