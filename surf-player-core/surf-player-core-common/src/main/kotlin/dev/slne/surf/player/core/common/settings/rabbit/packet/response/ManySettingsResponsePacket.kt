package dev.slne.surf.player.core.common.settings.rabbit.packet.response

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.player.api.settings.setting.Setting
import kotlinx.serialization.Serializable

@Serializable
data class ManySettingsResponsePacket(
    val settings: List<Setting>
) : RabbitResponsePacket()
