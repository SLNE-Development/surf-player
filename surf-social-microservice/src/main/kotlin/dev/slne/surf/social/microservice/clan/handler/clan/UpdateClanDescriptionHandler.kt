package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.updateDescription.UpdateClanDescriptionRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.coroutines.launch

object UpdateClanDescriptionHandler {

    @RabbitHandler
    fun handleUpdateClanDescription(request: UpdateClanDescriptionRequestPacket) {
        request.launch {
            val updated = ClanRepository.updateDescription(request.clanID, request.description)
            request.respond(PrimitiveResponse.BooleanResponsePacket(updated))
        }
    }
}