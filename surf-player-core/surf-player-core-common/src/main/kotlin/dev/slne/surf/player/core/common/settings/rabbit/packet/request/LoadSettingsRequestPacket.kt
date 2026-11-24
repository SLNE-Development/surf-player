package dev.slne.surf.player.core.common.settings.rabbit.packet.request

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.player.core.common.settings.rabbit.packet.response.ManySettingsResponsePacket
import kotlinx.serialization.Serializable

@Serializable
class LoadSettingsRequestPacket : RabbitRequestPacket<ManySettingsResponsePacket>()