package dev.slne.surf.player.microservice.chat.table

import dev.slne.surf.player.core.common.chat.aimoderation.ModerationCategory
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationAction
import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.json.json
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas
import kotlinx.serialization.json.Json

object ModerationsTable : AuditableLongIdTable("${PlayerSchemas.chat}.chat_moderations") {
    val messageUuid = nativeUuid("message_uuid").index()
    val action = enumerationByName<ModerationClassificationAction>("action", 50)
    val flaggedScores = json<Map<ModerationCategory, Double>>("flagged_scores", Json)
}