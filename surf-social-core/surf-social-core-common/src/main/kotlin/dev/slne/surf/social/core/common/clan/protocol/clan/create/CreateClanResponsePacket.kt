package dev.slne.surf.social.core.common.clan.protocol.clan.create

import dev.slne.surf.social.api.clan.clan.ClanCreationResult
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class CreateClanResponsePacket(val result: ClanCreationResult) : RabbitResponsePacket()
