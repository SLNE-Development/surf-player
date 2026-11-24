package dev.slne.surf.social.core.common.playtime.rabbit.packet.request

import dev.slne.surf.social.api.playtime.session.PlaytimeSession
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class SaveSessionRequestPacket(
    val session: PlaytimeSession
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()