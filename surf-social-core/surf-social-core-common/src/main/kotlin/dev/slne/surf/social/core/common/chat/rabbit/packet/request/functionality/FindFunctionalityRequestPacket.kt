package dev.slne.surf.social.core.common.chat.rabbit.packet.request.functionality

import dev.slne.surf.social.core.common.chat.rabbit.packet.response.functionality.FunctionalitiesResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class FindFunctionalityRequestPacket(
    val serverName: String
) : RabbitRequestPacket<FunctionalitiesResponsePacket>()
