package dev.slne.surf.social.microservice.friends.rabbit.handler

import dev.slne.surf.social.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.social.core.common.friends.packets.friendship.FetchFriendshipsRequestPacket
import dev.slne.surf.social.core.common.friends.packets.friendship.FetchFriendshipsResponsePacket
import dev.slne.surf.social.core.common.friends.packets.friendship.RemoveFriendshipRequestPacket
import dev.slne.surf.social.core.common.friends.packets.friendship.RemoveFriendshipResponsePacket
import dev.slne.surf.social.microservice.friends.db.repository.FriendshipsRepository
import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import kotlinx.coroutines.launch

object FriendshipHandler {
    @RabbitHandler
    fun handleFetchFriendshipsRequest(
        request: FetchFriendshipsRequestPacket
    ) = request.launch {
        request.respond(FetchFriendshipsResponsePacket(FriendshipsRepository.fetchFriendships()))
    }

    @RabbitHandler
    fun handleRemoveFriendshipRequest(
        request: RemoveFriendshipRequestPacket
    ) = request.launch {
        FriendshipsRepository.deleteFriendship(request.senderUuid, request.targetUuid)

        request.respond(RemoveFriendshipResponsePacket(FriendshipRemoveResult.Success))
    }
}