package dev.slne.surf.social.core.common.friends.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.social.api.friends.model.FriendRequest
import dev.slne.surf.social.api.friends.model.Friendship
import dev.slne.surf.social.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.social.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.social.api.friends.result.FriendRequestStateResult
import dev.slne.surf.social.api.friends.result.FriendshipRemoveResult
import java.util.*

/**
 * Friend requests and friendships stored by the microservice.
 *
 * The clients check the preconditions (already friends, request already sent, ...) against their
 * synchronized state before calling. Only the checks that need data the clients do not have
 * (ignores, cooldowns) are done here.
 */
@RpcService
interface FriendsRpcService {
    suspend fun fetchFriendRequests(): List<FriendRequest>

    /**
     * Stores a request from [senderUuid] to [targetUuid], unless one of both ignores the other
     * ([FriendRequestCreateResult.Blocked]) or [senderUuid] is on cooldown for [targetUuid]
     * ([FriendRequestCreateResult.OnCooldown]).
     */
    suspend fun createFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestCreateResult

    /** Removes the request from [senderUuid] to [targetUuid] and puts [senderUuid] on the withdraw cooldown. */
    suspend fun revokeFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestRemoveResult

    /**
     * Removes the request from [senderUuid] to [targetUuid]; [FriendRequestState.ACCEPT] also creates
     * the friendship, [FriendRequestState.DECLINE] puts [senderUuid] on the decline cooldown.
     */
    suspend fun answerFriendRequest(
        senderUuid: UUID,
        targetUuid: UUID,
        state: FriendRequestState
    ): FriendRequestStateResult

    /** Every friendship, stored once per direction. */
    suspend fun fetchFriendships(): List<Friendship>

    /** Removes the friendship between [senderUuid] and [targetUuid] in both directions. */
    suspend fun removeFriendship(senderUuid: UUID, targetUuid: UUID): FriendshipRemoveResult
}
