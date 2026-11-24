package dev.slne.surf.social.microservice.friends.db.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import dev.slne.surf.social.microservice.SocialSchemas

object FriendshipsTable : LongIdTable("${SocialSchemas.friends}.friendships") {
    val playerUuid = nativeUuid("player_uuid")
    val friendUuid = nativeUuid("friend_uuid")
    val createdAt = offsetDateTime("created_at")

    init {
        uniqueIndex(playerUuid, friendUuid)
    }
}