package dev.slne.surf.social.core.common.clan.protocol.clan.findByTag

import dev.slne.surf.social.core.common.clan.protocol.clan.OptionalClanResponsePacket
import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class FindClanByTagRequestPacket(val tag: String) : RabbitRequestPacket<OptionalClanResponsePacket>()