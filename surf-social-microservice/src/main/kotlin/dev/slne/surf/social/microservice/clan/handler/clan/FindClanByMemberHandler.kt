package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.OptionalClanResponsePacket
import dev.slne.surf.social.core.common.clan.protocol.clan.findByMember.FindClanByMemberRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindClanByMemberHandler {

    @RabbitHandler
    fun handleFindClanByMember(request: FindClanByMemberRequestPacket) {
        request.launch {
            val clan = ClanRepository.findClanByPlayer(request.memberUuid)
            request.respond(OptionalClanResponsePacket(clan))
        }
    }
}