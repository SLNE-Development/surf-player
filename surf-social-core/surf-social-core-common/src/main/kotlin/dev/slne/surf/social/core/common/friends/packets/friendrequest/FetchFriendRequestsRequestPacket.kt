package dev.slne.surf.social.core.common.friends.packets.friendrequest

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class FetchFriendRequestsRequestPacket : RabbitRequestPacket<FetchFriendRequestsResponsePacket>()
