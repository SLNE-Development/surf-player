package dev.slne.surf.social.microservice.chat.handler

import dev.slne.surf.social.core.common.chat.rabbit.packet.request.ignore.FindIgnoreListEntriesRequestPacket
import dev.slne.surf.social.core.common.chat.rabbit.packet.request.ignore.UpdateIgnoreRequestPacket
import dev.slne.surf.social.core.common.chat.rabbit.packet.response.ignore.ManyIgnorelistEntryResponsePacket
import dev.slne.surf.social.microservice.chat.repository.ignore.IgnoreRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.coroutines.launch

object IgnoreListHandler {
    @RabbitHandler
    fun handleUpdateIgnoreRequest(packet: UpdateIgnoreRequestPacket) = packet.launch {
        val changed = if (packet.ignored) {
            IgnoreRepository.ignore(packet.playerUuid, packet.targetPlayerUuid)
        } else {
            IgnoreRepository.unignore(packet.playerUuid, packet.targetPlayerUuid)
        }

        packet.respond(PrimitiveResponse.BooleanResponsePacket(changed))
    }

    @RabbitHandler
    fun handleFindIgnoreListEntriesRequest(packet: FindIgnoreListEntriesRequestPacket) = packet.launch {
        packet.respond(
            ManyIgnorelistEntryResponsePacket(
                IgnoreRepository.findAllByUuid(packet.playerUuid)
            )
        )
    }
}