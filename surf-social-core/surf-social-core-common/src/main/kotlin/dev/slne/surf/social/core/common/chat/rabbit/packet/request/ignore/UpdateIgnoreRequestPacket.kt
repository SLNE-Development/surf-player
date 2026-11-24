package dev.slne.surf.social.core.common.chat.rabbit.packet.request.ignore

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class UpdateIgnoreRequestPacket(
    val playerUuid: SerializableUUID,
    val targetPlayerUuid: SerializableUUID,
    val ignored: Boolean
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
