package dev.slne.surf.social.microservice.clan.handler.player

import dev.slne.surf.social.core.common.clan.protocol.player.updateAccepsClanInvites.UpdateClanPlayerAcceptsInvitesRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanPlayerRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.coroutines.launch

object UpdateClanPlayerAcceptsInvitesHandler {

    @RabbitHandler
    fun handleUpdateClanPlayerAcceptsInvites(request: UpdateClanPlayerAcceptsInvitesRequestPacket) {
        val (playerID, acceptsClanInvites) = request

        request.launch {
            val updated = ClanPlayerRepository.changeAcceptsClanInvites(playerID, acceptsClanInvites)
            request.respond(PrimitiveResponse.BooleanResponsePacket(updated))
        }
    }
}

