package dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByClanID

import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class FindPendingInvitesByClanIDResponsePacket(val invites: Set<ClanInviteImpl>) : RabbitResponsePacket()