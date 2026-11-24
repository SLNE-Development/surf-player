package dev.slne.surf.player.microservice.chat.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas

object IgnoreListTable : AuditableLongIdTable("${PlayerSchemas.chat}.chat_ignorelist_entries") {
    val userUuid = nativeUuid("user_id").index()
    val ignoredUuid = nativeUuid("target_uuid")

    init {
        uniqueIndex(userUuid, ignoredUuid)
    }
}