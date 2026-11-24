package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.findTagsByPrefixLimited.FindClanTagsByPrefixLimitedRequestPacket
import dev.slne.surf.social.core.common.clan.protocol.clan.findTagsByPrefixLimited.FindClanTagsByPrefixLimitedResponsePacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindClanTagsByPrefixLimitedHandler {

    @RabbitHandler
    fun handleFindClanTagsByPrefixLimited(request: FindClanTagsByPrefixLimitedRequestPacket) {
        request.launch {
            val clanTags = ClanRepository.suggestTagsByPrefix(request.prefix, request.limit)
            request.respond(FindClanTagsByPrefixLimitedResponsePacket(clanTags))
        }
    }
}