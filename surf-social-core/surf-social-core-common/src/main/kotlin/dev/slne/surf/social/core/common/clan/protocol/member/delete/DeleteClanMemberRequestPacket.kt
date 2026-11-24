package dev.slne.surf.social.core.common.clan.protocol.member.delete

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class DeleteClanMemberRequestPacket(
    val clanID: ULong,
    val playerUuid: @Contextual UUID
) : RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
