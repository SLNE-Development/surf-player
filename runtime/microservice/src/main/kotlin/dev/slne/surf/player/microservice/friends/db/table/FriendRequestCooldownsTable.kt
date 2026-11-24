package dev.slne.surf.player.microservice.friends.db.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas

/**
 * One row per direction (sender → receiver) that was ever declined or withdrawn. Until
 * [expiresAt] the sender may not send the receiver another friend request.
 */
object FriendRequestCooldownsTable : LongIdTable("${PlayerSchemas.friends}.friend_request_cooldowns") {
    val senderUuid = nativeUuid("sender_uuid")
    val receiverUuid = nativeUuid("receiver_uuid")
    val declineCount = integer("decline_count").default(0)
    val lastDeclineAt = offsetDateTime("last_decline_at").nullable()
    val withdrawCount = integer("withdraw_count").default(0)
    val lastWithdrawAt = offsetDateTime("last_withdraw_at").nullable()
    val expiresAt = offsetDateTime("expires_at").index()

    init {
        uniqueIndex(senderUuid, receiverUuid)
    }
}
