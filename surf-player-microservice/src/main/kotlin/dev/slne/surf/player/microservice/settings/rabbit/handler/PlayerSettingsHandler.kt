package dev.slne.surf.player.microservice.settings.rabbit.handler

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.player.core.common.settings.rabbit.packet.request.LoadPlayerSettingsRequestPacket
import dev.slne.surf.player.core.common.settings.rabbit.packet.request.SavePlayerSettingRequestPacket
import dev.slne.surf.player.core.common.settings.rabbit.packet.response.ManyPlayerSettingsResponsePacket
import dev.slne.surf.player.microservice.settings.repository.playerSettingsRepository
import kotlinx.coroutines.launch

object PlayerSettingsHandler {
    @RabbitHandler
    fun handleLoadPlayerSettingsRequestPacket(packet: LoadPlayerSettingsRequestPacket) =
        packet.launch {
            packet.respond(
                ManyPlayerSettingsResponsePacket(
                    playerSettingsRepository.loadSettingsByPlayerUuid(packet.playerUuid)
                )
            )
        }

    @RabbitHandler
    fun handleSavePlayerSettingRequestPacket(packet: SavePlayerSettingRequestPacket) =
        packet.launch {
            playerSettingsRepository.savePlayerSetting(
                packet.playerUuid,
                packet.playerSetting
            )
            packet.respond(
                PrimitiveResponse.BooleanResponsePacket(
                    true
                )
            )
        }
}