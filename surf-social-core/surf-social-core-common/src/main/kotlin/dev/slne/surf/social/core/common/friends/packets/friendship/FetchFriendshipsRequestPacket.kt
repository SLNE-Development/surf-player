package dev.slne.surf.social.core.common.friends.packets.friendship

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class FetchFriendshipsRequestPacket : RabbitRequestPacket<FetchFriendshipsResponsePacket>()
