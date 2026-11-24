package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.OptionalClanResponsePacket
import dev.slne.surf.social.core.common.clan.protocol.clan.findByID.FindClanByClanIDRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindClanByClanIDHandler {

    @RabbitHandler
    fun handleFindClanByClanID(request: FindClanByClanIDRequestPacket) {
        request.launch {
            val clan = ClanRepository.findClanByID(request.clanID)
            request.respond(OptionalClanResponsePacket(clan))
        }
    }
}