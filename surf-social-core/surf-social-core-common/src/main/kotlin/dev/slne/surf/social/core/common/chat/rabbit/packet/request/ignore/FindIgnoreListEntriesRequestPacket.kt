package dev.slne.surf.social.core.common.chat.rabbit.packet.request.ignore

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.social.core.common.chat.rabbit.packet.response.ignore.ManyIgnorelistEntryResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class FindIgnoreListEntriesRequestPacket(
    val playerUuid: SerializableUUID
) : RabbitRequestPacket<ManyIgnorelistEntryResponsePacket>()
