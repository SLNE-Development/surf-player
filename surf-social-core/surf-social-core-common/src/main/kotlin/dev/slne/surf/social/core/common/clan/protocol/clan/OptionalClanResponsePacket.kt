package dev.slne.surf.social.core.common.clan.protocol.clan

import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class OptionalClanResponsePacket(val clan: ClanImpl?) : RabbitResponsePacket()