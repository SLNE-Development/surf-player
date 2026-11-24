package dev.slne.surf.social.microservice.clan.handler.clan

import dev.slne.surf.social.core.common.clan.protocol.clan.updateTagColor.UpdateClanTagColorRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.coroutines.launch

object UpdateClanTagColorHandler {

    @RabbitHandler
    fun handleUpdateClanTagColor(request: UpdateClanTagColorRequestPacket) {
        val (clanID, tagForegroundColor, tagBackgroundColor, tagShadowColor) = request

        request.launch {
            val updated = ClanRepository.updateTagColor(
                clanID = clanID,
                tagForegroundColor = tagForegroundColor,
                tagBackgroundColor = tagBackgroundColor,
                tagShadowColor = tagShadowColor
            )

            request.respond(PrimitiveResponse.BooleanResponsePacket(updated))
        }
    }
}