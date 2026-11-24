package dev.slne.surf.social.core.common.friends.packets.friendship

import dev.slne.surf.social.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class RemoveFriendshipResponsePacket(
    val result: FriendshipRemoveResult
) : RabbitResponsePacket()
