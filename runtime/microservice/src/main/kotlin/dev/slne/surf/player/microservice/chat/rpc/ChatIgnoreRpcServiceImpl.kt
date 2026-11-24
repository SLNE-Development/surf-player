package dev.slne.surf.player.microservice.chat.rpc

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import dev.slne.surf.player.core.common.chat.rpc.ChatIgnoreRpcService
import dev.slne.surf.player.microservice.chat.repository.ignore.IgnoreRepository
import dev.slne.surf.player.microservice.friends.db.repository.FriendRequestRepository
import java.util.*

object ChatIgnoreRpcServiceImpl : ChatIgnoreRpcService {
    override suspend fun createIgnore(playerUuid: UUID, targetUuid: UUID): Boolean = suspendTransaction {
        val ignored = IgnoreRepository.ignore(playerUuid, targetUuid)

        if (ignored) {
            FriendRequestRepository.deleteFriendRequestsBetween(playerUuid, targetUuid)
        }

        ignored
    }

    override suspend fun deleteIgnore(playerUuid: UUID, targetUuid: UUID): Boolean =
        IgnoreRepository.unignore(playerUuid, targetUuid)

    override suspend fun findIgnoreList(playerUuid: UUID): List<IgnoreListEntry> =
        IgnoreRepository.findAllByUuid(playerUuid)
}
