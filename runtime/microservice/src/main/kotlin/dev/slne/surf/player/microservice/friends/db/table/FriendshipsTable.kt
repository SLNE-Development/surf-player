package dev.slne.surf.player.microservice.friends.db.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.columns.time.offsetDateTime
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas

object FriendshipsTable : LongIdTable("${PlayerSchemas.friends}.friendships") {
    val playerUuid = nativeUuid("player_uuid")
    val friendUuid = nativeUuid("friend_uuid")
    val createdAt = offsetDateTime("created_at")

    init {
        uniqueIndex(playerUuid, friendUuid)
    }
}