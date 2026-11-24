package dev.slne.surf.social.microservice.clan.handler.player

import dev.slne.surf.social.core.common.clan.protocol.player.findByUuid.FindClanPlayerByUuidRequestPacket
import dev.slne.surf.social.core.common.clan.protocol.player.findByUuid.FindClanPlayerByUuidResponsePacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanPlayerRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FindClanPlayerByUuidHandler {

    @RabbitHandler
    fun handleFindClanPlayerByUuid(request: FindClanPlayerByUuidRequestPacket) {
        val (uuid) = request

        request.launch {
            val player = ClanPlayerRepository.findOrCreateByUuid(uuid)
            request.respond(FindClanPlayerByUuidResponsePacket(player))
        }
    }
}

