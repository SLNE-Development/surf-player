package dev.slne.surf.social.microservice.chat.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.social.microservice.SocialSchemas

object IgnoreListTable : AuditableLongIdTable("${SocialSchemas.chat}.chat_ignorelist_entries") {
    val userUuid = nativeUuid("user_id").index()
    val ignoredUuid = nativeUuid("target_uuid")

    init {
        uniqueIndex(userUuid, ignoredUuid)
    }
}