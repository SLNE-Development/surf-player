package dev.slne.surf.player.microservice.friends.rpc

import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.api.friends.result.FriendRequestStateResult
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestState
import dev.slne.surf.player.core.common.friends.rpc.FriendsRpcService
import dev.slne.surf.player.microservice.chat.repository.ignore.IgnoreRepository
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldownKind
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldowns
import dev.slne.surf.player.microservice.friends.db.repository.FriendRequestRepository
import dev.slne.surf.player.microservice.friends.db.repository.FriendshipsRepository
import java.time.OffsetDateTime
import java.util.*

object FriendsRpcServiceImpl : FriendsRpcService {
    override suspend fun fetchFriendRequests(): List<FriendRequest> =
        FriendRequestRepository.fetchFriendRequests()

    override suspend fun createFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestCreateResult {
        if (IgnoreRepository.isEitherIgnoring(senderUuid, targetUuid)) {
            return FriendRequestCreateResult.Blocked(targetUuid)
        }

        val remainingCooldown = FriendRequestCooldowns.remaining(senderUuid, targetUuid)
        if (remainingCooldown != null) {
            return FriendRequestCreateResult.OnCooldown(targetUuid, remainingCooldown)
        }

        val friendRequest = FriendRequest(
            senderUuid = senderUuid,
            targetUuid = targetUuid,
            createdAt = OffsetDateTime.now(),
        )

        FriendRequestRepository.createFriendRequest(friendRequest)
        return FriendRequestCreateResult.Success(friendRequest)
    }

    override suspend fun revokeFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestRemoveResult {
        if (FriendRequestRepository.deleteFriendRequest(senderUuid, targetUuid)) {
            FriendRequestCooldowns.record(senderUuid, targetUuid, FriendRequestCooldownKind.WITHDRAW)
        }

        return FriendRequestRemoveResult.Success
    }

    override suspend fun answerFriendRequest(
        senderUuid: UUID,
        targetUuid: UUID,
        state: FriendRequestState
    ): FriendRequestStateResult {
        val existed = FriendRequestRepository.deleteFriendRequest(senderUuid, targetUuid)

        when (state) {
            FriendRequestState.ACCEPT ->
                FriendshipsRepository.createFriendship(senderUuid, targetUuid, OffsetDateTime.now())

            FriendRequestState.DECLINE -> if (existed) {
                FriendRequestCooldowns.record(senderUuid, targetUuid, FriendRequestCooldownKind.DECLINE)
            }
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
