package dev.slne.surf.social.core.common.chat.rabbit.packet.response.ignore

import dev.slne.surf.social.api.chat.entry.IgnoreListEntry
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class ManyIgnorelistEntryResponsePacket(
    val entries: List<IgnoreListEntry>
) : RabbitResponsePacket()