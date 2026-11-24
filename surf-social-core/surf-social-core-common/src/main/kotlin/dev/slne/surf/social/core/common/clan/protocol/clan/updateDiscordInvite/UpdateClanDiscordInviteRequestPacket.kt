package dev.slne.surf.social.core.common.clan.protocol.clan.updateDiscordInvite

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class UpdateClanDiscordInviteRequestPacket(
    val clanID: ULong,
    val discordInvite: String?
): RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()
