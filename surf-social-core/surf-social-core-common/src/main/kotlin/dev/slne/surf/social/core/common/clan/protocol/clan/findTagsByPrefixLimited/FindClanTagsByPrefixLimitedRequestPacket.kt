package dev.slne.surf.social.core.common.clan.protocol.clan.findTagsByPrefixLimited

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class FindClanTagsByPrefixLimitedRequestPacket(
    val prefix: String,
    val limit: Int
) : RabbitRequestPacket<FindClanTagsByPrefixLimitedResponsePacket>()