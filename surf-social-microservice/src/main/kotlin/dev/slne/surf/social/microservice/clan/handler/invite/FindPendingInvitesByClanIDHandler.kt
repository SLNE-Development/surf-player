package dev.slne.surf.social.microservice.clan.handler.invite

import dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByClanID.FindPendingInvitesByClanIDRequestPacket
import dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByClanID.FindPendingInvitesByClanIDResponsePacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanInviteRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindPendingInvitesByClanIDHandler {

    @RabbitHandler
    fun handleFindPendingInvitesByClanID(request: FindPendingInvitesByClanIDRequestPacket) {
        val (clanID) = request

        request.launch {
            val invites = ClanInviteRepository.fetchPendingInvites(clanID)
            request.respond(FindPendingInvitesByClanIDResponsePacket(invites))
        }
    }
}

