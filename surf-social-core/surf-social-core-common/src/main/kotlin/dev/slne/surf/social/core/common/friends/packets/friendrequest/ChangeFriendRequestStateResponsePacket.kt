package dev.slne.surf.social.core.common.friends.packets.friendrequest

import dev.slne.surf.social.api.friends.result.FriendRequestStateResult
import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class ChangeFriendRequestStateResponsePacket(
    val result: FriendRequestStateResult
) : RabbitResponsePacket()
