package dev.slne.surf.social.microservice.clan.handler.invite

import dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByInvited.FindPendingInvitesByInvitedRequestPacket
import dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByInvited.FindPendingInvitesByInvitedResponsePacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanInviteRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindPendingInvitesByInvitedHandler {

    @RabbitHandler
    fun handleFindPendingInvitesByInvited(request: FindPendingInvitesByInvitedRequestPacket) {
        val (invited) = request

        request.launch {
            val invites = ClanInviteRepository.getPendingInvitesByPlayer(invited)
            request.respond(FindPendingInvitesByInvitedResponsePacket(invites))
        }
    }
}

