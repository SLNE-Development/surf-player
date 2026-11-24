package dev.slne.surf.social.microservice.clan.handler.member

import dev.slne.surf.social.core.common.clan.protocol.member.changeRole.ChangeClanMemberRoleRequestPacket
import dev.slne.surf.social.microservice.clan.db.repository.ClanMemberRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.coroutines.launch

object ChangeClanMemberRoleHandler {

    @RabbitHandler
    fun handleChangeClanMemberRole(request: ChangeClanMemberRoleRequestPacket) {
        val (memberID, role) = request

        request.launch {
            val changed = ClanMemberRepository.changeRole(memberID, role)
            request.respond(PrimitiveResponse.BooleanResponsePacket(changed))
        }
    }
}

