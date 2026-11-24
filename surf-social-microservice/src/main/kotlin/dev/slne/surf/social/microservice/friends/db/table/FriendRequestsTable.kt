package dev.slne.surf.social.microservice.friends.db.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import dev.slne.surf.social.microservice.SocialSchemas

object FriendRequestsTable : LongIdTable("${SocialSchemas.friends}.friend_requests") {
    val senderUuid = nativeUuid("sender_uuid")
    val targetUuid = nativeUuid("target_uuid")
    val createdAt = offsetDateTime("created_at")

    init {
        uniqueIndex(senderUuid, targetUuid)
    }
}