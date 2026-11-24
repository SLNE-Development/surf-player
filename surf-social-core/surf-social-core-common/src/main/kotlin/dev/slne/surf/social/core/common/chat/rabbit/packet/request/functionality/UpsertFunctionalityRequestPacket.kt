package dev.slne.surf.social.core.common.chat.rabbit.packet.request.functionality

import dev.slne.surf.social.api.chat.functionality.Functionalities
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class UpsertFunctionalityRequestPacket(
    val serverName: String,
    val functionalities: Functionalities
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
