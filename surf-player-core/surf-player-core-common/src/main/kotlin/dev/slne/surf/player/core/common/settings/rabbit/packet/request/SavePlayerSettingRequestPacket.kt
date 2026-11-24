package dev.slne.surf.social.core.common.settings.rabbit.packet.request

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.social.api.settings.setting.PlayerSetting
import kotlinx.serialization.Serializable

@Serializable
data class SavePlayerSettingRequestPacket(
    val playerUuid: SerializableUUID,
    val playerSetting: PlayerSetting
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
