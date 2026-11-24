package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.findAllWithoutMembersSortByMemberCount.FindAllClansWithoutMembersSortByMemberCountRequestPacket
import dev.slne.surf.social.core.common.clan.protocol.clan.findAllWithoutMembersSortByMemberCount.FindAllClansWithoutMembersSortByMemberCountResponsePacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindAllClansWithoutMembersSortByMemberCountHandler {

    @RabbitHandler
    fun handleFindAllClansWithoutMembersSortByMemberCount(request: FindAllClansWithoutMembersSortByMemberCountRequestPacket) {
        request.launch {
            val fetched = ClanRepository.fetchAllClansWithoutMembersSortByMemberCount()
            request.respond(FindAllClansWithoutMembersSortByMemberCountResponsePacket(fetched))
        }
    }
}