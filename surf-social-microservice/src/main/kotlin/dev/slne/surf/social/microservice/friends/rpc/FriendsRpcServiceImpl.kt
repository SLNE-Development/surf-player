package dev.slne.surf.social.microservice.friends.rpc

import dev.slne.surf.social.api.friends.model.FriendRequest
import dev.slne.surf.social.api.friends.model.Friendship
import dev.slne.surf.social.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.social.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.social.api.friends.result.FriendRequestStateResult
import dev.slne.surf.social.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.social.core.common.friends.rpc.FriendRequestState
import dev.slne.surf.social.core.common.friends.rpc.FriendsRpcService
import dev.slne.surf.social.microservice.friends.db.repository.FriendRequestRepository
import dev.slne.surf.social.microservice.friends.db.repository.FriendshipsRepository
import java.time.OffsetDateTime
import java.util.*

object FriendsRpcServiceImpl : FriendsRpcService {
    override suspend fun fetchFriendRequests(): List<FriendRequest> =
        FriendRequestRepository.fetchFriendRequests()

    override suspend fun createFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestCreateResult {
        val friendRequest = FriendRequest(
            senderUuid = senderUuid,
            targetUuid = targetUuid,
            createdAt = OffsetDateTime.now(),
        )

        FriendRequestRepository.createFriendRequest(friendRequest)
        return FriendRequestCreateResult.Success(friendRequest)
    }

    override suspend fun revokeFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestRemoveResult {
        FriendRequestRepository.deleteFriendRequest(senderUuid, targetUuid)
        return FriendRequestRemoveResult.Success
    }

    override suspend fun answerFriendRequest(
        senderUuid: UUID,
        targetUuid: UUID,
        state: FriendRequestState
    ): FriendRequestStateResult {
        FriendRequestRepository.deleteFriendRequest(senderUuid, targetUuid)

        if (state == FriendRequestState.ACCEPT) {
            FriendshipsRepository.createFriendship(senderUuid, targetUuid, OffsetDateTime.now())
        }

        return FriendRequestStateResult.Success
    }

    override suspend fun fetchFriendships(): List<Friendship> =
        FriendshipsRepository.fetchFriendships()

    override suspend fun removeFriendship(senderUuid: UUID, targetUuid: UUID): FriendshipRemoveResult {
        FriendshipsRepository.deleteFriendship(senderUuid, targetUuid)
        return FriendshipRemoveResult.Success
    }
}
