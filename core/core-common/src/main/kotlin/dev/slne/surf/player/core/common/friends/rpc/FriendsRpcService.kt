package dev.slne.surf.player.core.common.friends.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
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
    /** Every pending friend request. */
    suspend fun findAllFriendRequests(): List<FriendRequest>

    /**
     * Stores a request from [senderUuid] to [targetUuid], unless one of both ignores the other
     * ([FriendRequestCreateResult.Blocked]), [targetUuid] turned off the setting `friend:requests`
     * ([FriendRequestCreateResult.RequestsDisabled]) or [senderUuid] is on cooldown for
     * [targetUuid] ([FriendRequestCreateResult.OnCooldown]), checked in this order.
     */
    suspend fun createFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestCreateResult

    /** Removes the request from [senderUuid] to [targetUuid] and puts [senderUuid] on the withdraw cooldown. */
    suspend fun revokeFriendRequest(senderUuid: UUID, targetUuid: UUID): FriendRequestRemoveResult

    /**
     * Removes the request from [senderUuid] to [targetUuid]; [FriendRequestState.ACCEPT] also creates
     * the friendship and returns both stored directions ([FriendRequestAnswer.Accepted]),
     * [FriendRequestState.DECLINE] puts [senderUuid] on the decline cooldown.
     */
    suspend fun answerFriendRequest(
        senderUuid: UUID,
        targetUuid: UUID,
        state: FriendRequestState
    ): FriendRequestAnswer

    /** Every friendship, stored once per direction. */
    suspend fun findAllFriendships(): List<Friendship>

    /** Removes the friendship between [senderUuid] and [targetUuid] in both directions. */
    suspend fun deleteFriendship(senderUuid: UUID, targetUuid: UUID): FriendshipRemoveResult
}
