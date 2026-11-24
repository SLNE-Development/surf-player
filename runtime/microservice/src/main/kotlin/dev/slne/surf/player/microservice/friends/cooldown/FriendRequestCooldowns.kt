package dev.slne.surf.player.microservice.friends.cooldown

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.player.microservice.friends.config.FriendsConfig
import dev.slne.surf.player.microservice.friends.db.repository.FriendRequestCooldownRepository
import java.time.OffsetDateTime
import java.util.*
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/** Stored friend request cooldowns, applying [FriendRequestCooldownCalculator] with [FriendsConfig]. */
object FriendRequestCooldowns {
    /** How long [senderUuid] still has to wait before sending [receiverUuid] a request, or `null`. */
    suspend fun remaining(senderUuid: UUID, receiverUuid: UUID): Duration? =
        FriendRequestCooldownRepository.findCooldown(senderUuid, receiverUuid)
            ?.remainingAt(OffsetDateTime.now())

    /** Starts or escalates the cooldown of [senderUuid] → [receiverUuid] after a [kind] event. */
    suspend fun record(senderUuid: UUID, receiverUuid: UUID, kind: FriendRequestCooldownKind) = suspendTransaction {
        val previous = FriendRequestCooldownRepository.findCooldown(senderUuid, receiverUuid)
        val updated = FriendRequestCooldownCalculator.record(
            previous,
            kind,
            OffsetDateTime.now(),
            FriendsConfig.cooldownSettings
        )

        FriendRequestCooldownRepository.saveCooldown(senderUuid, receiverUuid, updated)
    }

    /**
     * Deletes the cooldowns that are over and older than the escalation window, so they can no
     * longer escalate a later one either.
     */
    suspend fun deleteStale() {
        val window = FriendsConfig.cooldownSettings.escalationWindow
        FriendRequestCooldownRepository.deleteExpiredBefore(OffsetDateTime.now().minus(window.toJavaDuration()))
    }
}
