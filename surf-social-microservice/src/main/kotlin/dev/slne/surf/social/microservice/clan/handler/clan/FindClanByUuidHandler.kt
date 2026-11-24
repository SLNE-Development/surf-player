package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.OptionalClanResponsePacket
import dev.slne.surf.social.core.common.clan.protocol.clan.findByUuid.FindClanByUuidRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindClanByUuidHandler {

    @RabbitHandler
    fun handleFindClanByUuid(request: FindClanByUuidRequestPacket) {
        request.launch {
            val clan = ClanRepository.findClanByUuid(request.clanUuid)
            request.respond(OptionalClanResponsePacket(clan))
        }
    }
}