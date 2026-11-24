package dev.slne.surf.social.core.common.chat.rabbit.packet.response.history

import dev.slne.surf.social.api.chat.entry.HistoryEntry
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class ManyHistoriesResponsePacket(
    val histories: List<HistoryEntry>
) : RabbitResponsePacket()