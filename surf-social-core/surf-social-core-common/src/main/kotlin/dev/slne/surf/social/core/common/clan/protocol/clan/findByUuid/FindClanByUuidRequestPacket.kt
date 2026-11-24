package dev.slne.surf.social.core.common.clan.protocol.clan.findByUuid

import dev.slne.surf.social.core.common.clan.protocol.clan.OptionalClanResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FindClanByUuidRequestPacket(
    val clanUuid: @Contextual UUID
) : RabbitRequestPacket<OptionalClanResponsePacket>()