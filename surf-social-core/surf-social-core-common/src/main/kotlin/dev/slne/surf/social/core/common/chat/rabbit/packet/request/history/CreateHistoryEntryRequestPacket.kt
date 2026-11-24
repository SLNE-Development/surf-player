package dev.slne.surf.social.core.common.chat.rabbit.packet.request.history

import dev.slne.surf.social.api.chat.entry.HistoryEntry
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class CreateHistoryEntryRequestPacket(
    val historyEntry: HistoryEntry
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
