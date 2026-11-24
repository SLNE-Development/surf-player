package dev.slne.surf.social.core.common.clan.protocol.member.create

import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class CreateClanMemberResponsePacket(val result: ClanMemberAddResult) : RabbitResponsePacket()