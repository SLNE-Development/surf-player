package dev.slne.surf.social.microservice.clan.handler.invite

import dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByInvitedAndClanName.FindPendingInviteByInvitedPlayerAndClanNameRequestPacket
import dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByInvitedAndClanName.FindPendingInviteByInvitedPlayerAndClanNameResponsePacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanInviteRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindPendingInviteByInvitedPlayerAndClanNameHandler {

    @RabbitHandler
    fun handleFindPendingInviteByInvitedPlayerAndClanName(request: FindPendingInviteByInvitedPlayerAndClanNameRequestPacket) {
        val (invited, clanName) = request

        request.launch {
            val invite = ClanInviteRepository.getPendingInviteByPlayerAndClanName(
                invited = invited,
                clanName = clanName
            )

            request.respond(FindPendingInviteByInvitedPlayerAndClanNameResponsePacket(invite))
        }
    }
}

