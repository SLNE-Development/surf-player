package dev.slne.surf.social.microservice.friends.db.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Op
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.or
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.social.api.friends.model.FriendRequest
import dev.slne.surf.social.microservice.friends.db.table.FriendRequestsTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object FriendRequestRepository {
    suspend fun fetchFriendRequests(): List<FriendRequest> = suspendTransaction {
        FriendRequestsTable.selectAll().map {
            FriendRequest(
                senderUuid = it[FriendRequestsTable.senderUuid],
                targetUuid = it[FriendRequestsTable.targetUuid],
                createdAt = it[FriendRequestsTable.createdAt]
            )
        }.toList()
    }

    suspend fun createFriendRequest(
        friendRequest: FriendRequest
    ) = suspendTransaction {
        FriendRequestsTable.insert {
            it[this.senderUuid] = friendRequest.senderUuid
            it[this.targetUuid] = friendRequest.targetUuid
            it[this.createdAt] = friendRequest.createdAt
        }

        Unit
    }

    /** @return whether there was a request from [senderUuid] to [targetUuid] */
    suspend fun deleteFriendRequest(
        senderUuid: UUID,
        targetUuid: UUID
    ): Boolean = suspendTransaction {
        FriendRequestsTable.deleteWhere { isRequest(senderUuid, targetUuid) } > 0
    }

    /** Deletes the requests between [playerA] and [playerB] in both directions. */
    suspend fun deleteFriendRequestsBetween(
        playerA: UUID,
        playerB: UUID
    ) = suspendTransaction {
        FriendRequestsTable.deleteWhere { isRequest(playerA, playerB) or isRequest(playerB, playerA) }

        Unit
    }

    private fun isRequest(senderUuid: UUID, targetUuid: UUID): Op<Boolean> =
        (FriendRequestsTable.senderUuid eq senderUuid) and (FriendRequestsTable.targetUuid eq targetUuid)
}
