package dev.slne.surf.player.core.common.playtime.rabbit.packet.request

import dev.slne.surf.player.api.playtime.session.PlaytimeFilter
import dev.slne.surf.player.core.common.playtime.rabbit.packet.response.PlaytimePerPlayerResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class LoadPlaytimePerPlayerRequestPacket(
    val filter: PlaytimeFilter
) : RabbitRequestPacket<PlaytimePerPlayerResponsePacket>()
