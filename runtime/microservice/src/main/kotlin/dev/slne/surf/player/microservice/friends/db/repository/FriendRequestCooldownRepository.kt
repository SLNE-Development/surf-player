package dev.slne.surf.player.microservice.friends.db.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.less
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.upsert
import dev.slne.surf.player.microservice.friends.cooldown.CooldownCounter
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldown
import dev.slne.surf.player.microservice.friends.db.table.FriendRequestCooldownsTable
import kotlinx.coroutines.flow.firstOrNull
import java.time.OffsetDateTime
import java.util.*

object FriendRequestCooldownRepository {
    /** The cooldown of the direction [senderUuid] → [receiverUuid], expired or not. */
    suspend fun findCooldown(
        senderUuid: UUID,
        receiverUuid: UUID
    ): FriendRequestCooldown? = suspendTransaction {
        FriendRequestCooldownsTable.selectAll()
            .where {
                (FriendRequestCooldownsTable.senderUuid eq senderUuid) and
                        (FriendRequestCooldownsTable.receiverUuid eq receiverUuid)
            }
            .firstOrNull()
            ?.toCooldown()
    }

    suspend fun saveCooldown(
        senderUuid: UUID,
        receiverUuid: UUID,
        cooldown: FriendRequestCooldown
    ) = suspendTransaction {
        FriendRequestCooldownsTable.upsert(
            FriendRequestCooldownsTable.senderUuid,
            FriendRequestCooldownsTable.receiverUuid
        ) {
            it[this.senderUuid] = senderUuid
            it[this.receiverUuid] = receiverUuid
            it[declineCount] = cooldown.declines.count
            it[lastDeclineAt] = cooldown.declines.lastAt
            it[withdrawCount] = cooldown.withdrawals.count
            it[lastWithdrawAt] = cooldown.withdrawals.lastAt
            it[expiresAt] = cooldown.expiresAt
        }

        Unit
    }

    /** Deletes the cooldowns that expired before [before]. */
    suspend fun deleteExpiredBefore(before: OffsetDateTime) = suspendTransaction {
        FriendRequestCooldownsTable.deleteWhere { FriendRequestCooldownsTable.expiresAt less before }

        Unit
    }

    /**
     * Deletes the cooldowns of [senderUuid], only the one towards [receiverUuid] if given; returns
     * how many were deleted.
     */
    suspend fun deleteCooldowns(senderUuid: UUID, receiverUuid: UUID?): Int = suspendTransaction {
        FriendRequestCooldownsTable.deleteWhere {
            val ofSender = FriendRequestCooldownsTable.senderUuid eq senderUuid

            if (receiverUuid == null) ofSender else ofSender and (FriendRequestCooldownsTable.receiverUuid eq receiverUuid)
        }
    }

    private fun ResultRow.toCooldown() = FriendRequestCooldown(
        declines = CooldownCounter(
            count = this[FriendRequestCooldownsTable.declineCount],
            lastAt = this[FriendRequestCooldownsTable.lastDeclineAt]
        ),
        withdrawals = CooldownCounter(
            count = this[FriendRequestCooldownsTable.withdrawCount],
            lastAt = this[FriendRequestCooldownsTable.lastWithdrawAt]
        ),
        expiresAt = this[FriendRequestCooldownsTable.expiresAt]
    )
}
