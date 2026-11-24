package dev.slne.surf.social.core.common.friends.packets.friendrequest

import dev.slne.surf.social.api.friends.model.FriendRequest
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class FetchFriendRequestsResponsePacket(
    val friendRequests: List<FriendRequest>
) : RabbitResponsePacket()
