package dev.slne.surf.social.core.common.clan.protocol.player.findByUuid

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FindClanPlayerByUuidRequestPacket(
    val uuid: @Contextual UUID
) : RabbitRequestPacket<FindClanPlayerByUuidResponsePacket>()