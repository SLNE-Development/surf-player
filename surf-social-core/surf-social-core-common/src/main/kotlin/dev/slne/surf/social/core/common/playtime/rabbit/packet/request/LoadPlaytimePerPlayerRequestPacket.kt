package dev.slne.surf.social.core.common.playtime.rabbit.packet.request

import dev.slne.surf.social.api.playtime.session.PlaytimeFilter
import dev.slne.surf.social.core.common.playtime.rabbit.packet.response.PlaytimePerPlayerResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class LoadPlaytimePerPlayerRequestPacket(
    val filter: PlaytimeFilter
) : RabbitRequestPacket<PlaytimePerPlayerResponsePacket>()
