package dev.slne.surf.social.core.common.chat.rabbit.packet.request.functionality

import dev.slne.surf.social.core.common.chat.rabbit.packet.response.functionality.ManyFunctionalitiesResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class FindAllFunctionalitiesRequestPacket : RabbitRequestPacket<ManyFunctionalitiesResponsePacket>()
