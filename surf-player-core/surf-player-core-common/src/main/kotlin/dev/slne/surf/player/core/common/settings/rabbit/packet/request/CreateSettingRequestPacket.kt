package dev.slne.surf.player.core.common.settings.rabbit.packet.request

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.player.core.common.settings.rabbit.packet.response.SingleSettingResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class CreateSettingRequestPacket(
    val name: String,
    val defaultValue: String
) :
    RabbitRequestPacket<SingleSettingResponsePacket>()
