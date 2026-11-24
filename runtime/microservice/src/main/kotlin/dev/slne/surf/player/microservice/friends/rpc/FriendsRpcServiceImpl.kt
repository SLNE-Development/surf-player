package dev.slne.surf.player.microservice.friends.rpc

import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.api.friends.model.Friendship
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.api.friends.result.FriendshipRemoveResult
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestAnswer
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestPrivileges
import dev.slne.surf.player.core.common.friends.rpc.FriendRequestState
import dev.slne.surf.player.core.common.friends.rpc.FriendsRpcService
import dev.slne.surf.player.microservice.chat.repository.ignore.IgnoreRepository
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldownKind
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldowns
import dev.slne.surf.player.microservice.friends.db.repository.FriendRequestCooldownRepository
import dev.slne.surf.player.microservice.friends.db.repository.FriendRequestRepository
import dev.slne.surf.player.microservice.friends.db.repository.FriendshipsRepository
import dev.slne.surf.player.microservice.settings.repository.PlayerSettingsRepository
import java.time.OffsetDateTime
import java.util.*

object FriendsRpcServiceImpl : FriendsRpcService {
    override suspend fun findAllFriendRequests(): List<FriendRequest> =
        FriendRequestRepository.fetchFriendRequests()

    override suspend fun createFriendRequest(
        senderUuid: UUID,
        targetUuid: UUID,
        privileges: FriendRequestPrivileges
    ): FriendRequestCreateResult {
        FriendRequestGate.refusal(
            targetUuid,
            privileges,
            senderIgnoresTarget = { IgnoreRepository.isIgnoring(senderUuid, targetUuid) },
            targetIgnoresSender = { IgnoreRepository.isIgnoring(targetUuid, senderUuid) },
            targetAcceptsRequests = { acceptsFriendRequests(targetUuid) },
            remainingCooldown = { FriendRequestCooldowns.remaining(senderUuid, targetUuid) },
        )?.let { return it }

        val friendRequest = FriendRequest(
            senderUuid = senderUuid,
            targetUuid = targetUuid,
            createdAt = OffsetDateTime.now(),
        )

        FriendRequestRepository.createFriendRequest(friendRequest)
        return FriendRequestCreateResult.Success(friendRequest)
    }

    override suspend fun deleteFriendRequestCooldowns(senderUuid: UUID, targetUuid: UUID?): Int =
        FriendRequestCooldownRepository.deleteCooldowns(senderUuid, targetUuid)

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
    ): FriendRequestAnswer {
        val existed = FriendRequestRepository.deleteFriendRequest(senderUuid, targetUuid)

        return when (state) {
            FriendRequestState.ACCEPT -> FriendRequestAnswer.Accepted(
                FriendshipsRepository.createFriendship(senderUuid, targetUuid, OffsetDateTime.now())
            )

            FriendRequestState.DECLINE -> {
                if (existed) {
                    FriendRequestCooldowns.record(senderUuid, targetUuid, FriendRequestCooldownKind.DECLINE)
                }
                FriendRequestAnswer.Declined
            }
        }
    }

    /** Whether [targetUuid] accepts friend requests (setting [SettingKeys.FRIEND_REQUESTS]). */
    private suspend fun acceptsFriendRequests(targetUuid: UUID): Boolean {
        val key = SettingKeys.FRIEND_REQUESTS
        val value = PlayerSettingsRepository.findSettingValueOrDefault(targetUuid, key.name)
            ?: return key.defaultValue

        return runCatching { key.deserialize(value) }.getOrDefault(key.defaultValue)
    }

    override suspend fun findAllFriendships(): List<Friendship> =
        FriendshipsRepository.fetchFriendships()

    override suspend fun deleteFriendship(senderUuid: UUID, targetUuid: UUID): FriendshipRemoveResult {
        FriendshipsRepository.deleteFriendship(senderUuid, targetUuid)
        return FriendshipRemoveResult.Success
    }
}
