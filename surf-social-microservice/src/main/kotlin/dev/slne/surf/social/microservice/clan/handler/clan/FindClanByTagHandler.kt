package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.OptionalClanResponsePacket
import dev.slne.surf.social.core.common.clan.protocol.clan.findByTag.FindClanByTagRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindClanByTagHandler {

    @RabbitHandler
    fun handleFindClanByTag(request: FindClanByTagRequestPacket) {
        request.launch {
            val clan = ClanRepository.findClanByTag(request.tag)
            request.respond(OptionalClanResponsePacket(clan))
        }
    }
}