package dev.slne.surf.social.core.common.clan.protocol.invite.findPendingByClanID

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class FindPendingInvitesByClanIDRequestPacket(val clanID: ULong) :
    RabbitRequestPacket<FindPendingInvitesByClanIDResponsePacket>()