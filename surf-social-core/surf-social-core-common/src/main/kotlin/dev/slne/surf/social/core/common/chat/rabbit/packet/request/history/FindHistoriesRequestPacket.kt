package dev.slne.surf.social.core.common.chat.rabbit.packet.request.history

import dev.slne.surf.social.api.chat.entry.HistoryFilter
import dev.slne.surf.social.core.common.chat.rabbit.packet.response.history.ManyHistoriesResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class FindHistoriesRequestPacket(
    val filter: HistoryFilter
) : RabbitRequestPacket<ManyHistoriesResponsePacket>()
