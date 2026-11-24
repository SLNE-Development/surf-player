package dev.slne.surf.social.core.common.clan.protocol.clan.findAllWithoutMembersSortByMemberCount

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class FindAllClansWithoutMembersSortByMemberCountRequestPacket :
    RabbitRequestPacket<FindAllClansWithoutMembersSortByMemberCountResponsePacket>()