package dev.slne.surf.social.core.common.playtime.rabbit.packet.response

import dev.slne.surf.social.api.playtime.session.PlaytimeStreak
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class PlaytimeStreakResponsePacket(
    val streak: PlaytimeStreak?
) : RabbitResponsePacket()
