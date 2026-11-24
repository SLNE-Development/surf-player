package dev.slne.surf.social.core.common.chat.rabbit.packet.response.functionality

import dev.slne.surf.social.api.chat.functionality.Functionalities
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class ManyFunctionalitiesResponsePacket(
    val functionalities: Map<String, Functionalities>
) : RabbitResponsePacket()
