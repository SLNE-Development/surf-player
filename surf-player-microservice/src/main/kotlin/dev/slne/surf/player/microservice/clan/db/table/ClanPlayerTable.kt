package dev.slne.surf.social.microservice.clan.db.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import dev.slne.surf.social.microservice.SocialSchemas

object ClanPlayerTable : ULongIdTable("${SocialSchemas.clan}.clan_players") {
    val uuid = nativeUuid("uuid").uniqueIndex()
    val acceptsClanInvites = bool("accepts_clan_invites").default(true)
}