package dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByInvitedAndClanName

import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class FindPendingInviteByInvitedPlayerAndClanNameResponsePacket(
    val invite: ClanInviteImpl?
) : RabbitResponsePacket()