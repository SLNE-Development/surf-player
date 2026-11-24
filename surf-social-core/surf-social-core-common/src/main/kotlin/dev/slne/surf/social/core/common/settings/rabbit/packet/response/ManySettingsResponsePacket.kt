package dev.slne.surf.social.core.common.settings.rabbit.packet.response

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.social.api.settings.setting.Setting
import kotlinx.serialization.Serializable

@Serializable
data class ManySettingsResponsePacket(
    val settings: List<Setting>
) : RabbitResponsePacket()
