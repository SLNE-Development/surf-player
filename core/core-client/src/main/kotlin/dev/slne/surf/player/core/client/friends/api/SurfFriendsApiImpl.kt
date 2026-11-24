package dev.slne.surf.player.core.client.friends.api

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.api.friends.result.FriendRequestStateResult
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.client.friends.FriendsClient
import dev.slne.surf.player.core.client.friends.redis.event.*
import dev.slne.surf.player.core.client.friends.rpc.friendsRpcService
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestState
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.time.OffsetDateTime
import java.util.*

@AutoService(SurfFriendsApi::class)
class SurfFriendsApiImpl : SurfFriendsApi {

    override fun receivedFriendRequests(uuid: UUID): @UnmodifiableView ObjectSet<FriendRequest> {
        val snapshot = friendRequestSnapshot()
        val requests = ObjectOpenHashSet<FriendRequest>()

        for (index in snapshot.indices) {
            val request = snapshot[index]
            if (request.targetUuid == uuid) requests.add(request)
        }

        return requests.freeze()
    }

    override fun sentFriendRequests(uuid: UUID): @UnmodifiableView ObjectSet<FriendRequest> {
        val snapshot = friendRequestSnapshot()
        val requests = ObjectOpenHashSet<FriendRequest>()

        for (index in snapshot.indices) {
            val request = snapshot[index]
            if (request.senderUuid == uuid) requests.add(request)
        }

        return requests.freeze()
    }

    override fun friendships(uuid: UUID): @UnmodifiableView ObjectSet<Friendship> {
        val snapshot = friendshipSnapshot()
        val friendships = ObjectOpenHashSet<Friendship>()

        for (index in snapshot.indices) {
            val friendship = snapshot[index]
            if (friendship.playerUuid == uuid) friendships.add(friendship)
        }

        return friendships.freeze()
    }

    override fun onlineFriendUuids(uuid: UUID): @UnmodifiableView ObjectSet<UUID> {
        val snapshot = friendshipSnapshot()
        val online = ObjectOpenHashSet<UUID>()

        for (index in snapshot.indices) {
            val friendship = snapshot[index]
            if (friendship.playerUuid != uuid) continue

            val friend = SurfCoreApi.getPlayer(friendship.friendUuid) ?: continue
            online.add(friend.uuid)
        }

        return online.freeze()
    }

    override fun hasReceivedFriendRequest(uuid: UUID, sender: UUID): Boolean {
        val snapshot = friendRequestSnapshot()

        for (index in snapshot.indices) {
            val request = snapshot[index]
            if (request.targetUuid == uuid && request.senderUuid == sender) return true
        }

        return false
    }

    override fun hasSentFriendRequest(uuid: UUID, target: UUID): Boolean {
        val snapshot = friendRequestSnapshot()

        for (index in snapshot.indices) {
            val request = snapshot[index]
            if (request.senderUuid == uuid && request.targetUuid == target) return true
        }

        return false
    }

    override fun hasFriendship(uuid: UUID, friend: UUID): Boolean {
        return findFriendship(uuid, friend) != null
    }

    override fun findFriendship(uuid: UUID, friend: UUID): Friendship? {
        val snapshot = friendshipSnapshot()

        for (index in snapshot.indices) {
            val friendship = snapshot[index]
            if (friendship.playerUuid == uuid && friendship.friendUuid == friend) {
                return friendship
            }
        }

        return null
    }

    override suspend fun sendFriendRequest(uuid: UUID, target: UUID): FriendRequestCreateResult {
        val snapshot = friendRequestSnapshot()
        var alreadySent = false
        var alreadyReceived = false

        for (index in snapshot.indices) {
            val request = snapshot[index]

            if (request.senderUuid == uuid && request.targetUuid == target) {
                alreadySent = true
                break
            }

            if (request.senderUuid == target && request.targetUuid == uuid) {
                alreadyReceived = true
            }
        }

        if (alreadySent) {
            return FriendRequestCreateResult.AlreadySentFriendRequest(target)
        }

        if (alreadyReceived) {
            return FriendRequestCreateResult.AlreadyReceivedFriendRequest(target)
        }

        if (hasFriendship(uuid, target)) {
            return FriendRequestCreateResult.AlreadyFriends(target)
        }

        val result = friendsRpcService.createFriendRequest(senderUuid = uuid, targetUuid = target)

        if (result !is FriendRequestCreateResult.Success) {
            return result
        }

        FriendsClient.friendRequests.add(result.friendRequest)

        redisApi.publishEvent(
            FriendRequestSendRedisEvent(
                uuid, target, friendRequestNotificationsEnabled(target)
            )
        ).await()

        return result
    }

    override suspend fun revokeFriendRequest(uuid: UUID, target: UUID): FriendRequestRemoveResult {
        if (!hasSentFriendRequest(uuid, target)) {
            return FriendRequestRemoveResult.NotSentFriendRequest(target)
        }

        val result = friendsRpcService.revokeFriendRequest(senderUuid = uuid, targetUuid = target)

        if (result !is FriendRequestRemoveResult.Success) {
            return result
        }

        FriendsClient.removeFriendRequest(senderUuid = uuid, targetUuid = target)

        redisApi.publishEvent(
            FriendRequestRevokeRedisEvent(
                uuid, target, true
            )
        ).await()

        return result
    }

    override suspend fun acceptFriendRequest(uuid: UUID, sender: UUID): FriendRequestStateResult {
        if (!hasReceivedFriendRequest(uuid, sender)) {
            return FriendRequestStateResult.NoFriendRequest(sender)
        }

        val result = friendsRpcService.answerFriendRequest(
            senderUuid = sender,
            targetUuid = uuid,
            state = FriendRequestState.ACCEPT
        )

        if (result !is FriendRequestStateResult.Success) {
            return result
        }

        FriendsClient.removeFriendRequest(senderUuid = sender, targetUuid = uuid)

        val now = OffsetDateTime.now()

        FriendsClient.friendships.add(
            Friendship(
                playerUuid = uuid,
                friendUuid = sender,
                createdAt = now
            )
        )

        FriendsClient.friendships.add(
            Friendship(
                playerUuid = sender,
                friendUuid = uuid,
                createdAt = now
            )
        )

        redisApi.publishEvent(
            FriendRequestAcceptRedisEvent(
                uuid, sender
            )
        ).await()

        return result
    }

    override suspend fun declineFriendRequest(uuid: UUID, sender: UUID): FriendRequestStateResult {
        if (!hasReceivedFriendRequest(uuid, sender)) {
            return FriendRequestStateResult.NoFriendRequest(sender)
        }

        val result = friendsRpcService.answerFriendRequest(
            senderUuid = sender,
            targetUuid = uuid,
            state = FriendRequestState.DECLINE
        )

        if (result !is FriendRequestStateResult.Success) {
            return result
        }

        FriendsClient.removeFriendRequest(senderUuid = sender, targetUuid = uuid)

        redisApi.publishEvent(
            FriendRequestDenyRedisEvent(
                uuid, sender
            )
        ).await()

        return result
    }

    override suspend fun removeFriendship(uuid: UUID, friend: UUID): FriendshipRemoveResult {
        if (!hasFriendship(uuid, friend)) {
            return FriendshipRemoveResult.NotFriends(friend)
        }

        val result = friendsRpcService.removeFriendship(senderUuid = uuid, targetUuid = friend)

        if (result !is FriendshipRemoveResult.Success) {
            return result
        }

        redisApi.publishEvent(
            FriendRemoveRedisEvent(
                uuid, friend
            )
        ).await()

        FriendsClient.friendships.removeIf {
            (it.playerUuid == uuid && it.friendUuid == friend) ||
                    (it.playerUuid == friend && it.friendUuid == uuid)
        }

        return result
    }

    override fun friendNotificationsEnabled(uuid: UUID): Boolean =
        SurfSettingsApi.getSettingValue(uuid, SettingKeys.FRIEND_NOTIFICATIONS)

    override fun friendSoundsEnabled(uuid: UUID): Boolean =
        SurfSettingsApi.getSettingValue(uuid, SettingKeys.FRIEND_SOUNDS)

    override fun friendRequestNotificationsEnabled(uuid: UUID): Boolean =
        SurfSettingsApi.getSettingValue(uuid, SettingKeys.FRIEND_REQUEST_NOTIFICATIONS)

    private fun friendRequestSnapshot(): ObjectArrayList<FriendRequest> =
        FriendsClient.friendRequests.snapshot()

    private fun friendshipSnapshot(): ObjectArrayList<Friendship> =
        FriendsClient.friendships.snapshot()
}
