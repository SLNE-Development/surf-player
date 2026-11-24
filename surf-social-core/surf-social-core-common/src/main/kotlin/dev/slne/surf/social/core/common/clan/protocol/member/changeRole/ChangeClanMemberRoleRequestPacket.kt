package dev.slne.surf.social.core.common.clan.protocol.member.changeRole

import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class ChangeClanMemberRoleRequestPacket(
    val memberID: ULong,
    val role: ClanMemberRole
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()