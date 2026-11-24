package dev.slne.surf.social.core.common.clan.protocol.member.findByUuid

import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class FindClanMemberByUuidResponsePacket(
    val member: ClanMemberImpl?
) : RabbitResponsePacket()