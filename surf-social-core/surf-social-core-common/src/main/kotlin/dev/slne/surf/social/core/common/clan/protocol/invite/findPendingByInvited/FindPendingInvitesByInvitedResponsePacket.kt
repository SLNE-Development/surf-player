package dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByInvited

import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class FindPendingInvitesByInvitedResponsePacket(val invites: List<ClanInviteImpl>) : RabbitResponsePacket()