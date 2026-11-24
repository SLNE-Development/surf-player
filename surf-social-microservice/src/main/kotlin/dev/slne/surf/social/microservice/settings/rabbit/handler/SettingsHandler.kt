package dev.slne.surf.social.microservice.settings.rabbit.handler

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.social.core.common.settings.rabbit.packet.request.CreateSettingRequestPacket
import dev.slne.surf.social.core.common.settings.rabbit.packet.request.DeleteSettingRequestPacket
import dev.slne.surf.social.core.common.settings.rabbit.packet.request.LoadSettingsRequestPacket
import dev.slne.surf.social.core.common.settings.rabbit.packet.response.ManySettingsResponsePacket
import dev.slne.surf.social.core.common.settings.rabbit.packet.response.SingleSettingResponsePacket
import dev.slne.surf.social.microservice.settings.repository.settingRepository
import kotlinx.coroutines.launch

object SettingsHandler {
    @RabbitHandler
    fun handleCreateSettingPacket(packet: CreateSettingRequestPacket) = packet.launch {
        packet.respond(
            SingleSettingResponsePacket(
                settingRepository.createSetting(
                    packet.name,
                    packet.defaultValue
                )
            )
        )
    }

    @RabbitHandler
    fun handleDeleteSettingPacket(packet: DeleteSettingRequestPacket) = packet.launch {
        packet.respond(
            PrimitiveResponse.BooleanResponsePacket(
                settingRepository.deleteSetting(
                    packet.name
                ) > 0
            )
        )
    }

    @RabbitHandler
    fun handleLoadSettingsRequestPacket(packet: LoadSettingsRequestPacket) = packet.launch {
        packet.respond(
            ManySettingsResponsePacket(
                settingRepository.loadSettings()
            )
        )
    }
}