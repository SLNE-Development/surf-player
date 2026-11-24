package dev.slne.surf.social.core.common.clan.protocol.player.findByUuid

import dev.slne.surf.social.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class FindClanPlayerByUuidResponsePacket(
    val player: ClanPlayerImpl
): RabbitResponsePacket() {
}