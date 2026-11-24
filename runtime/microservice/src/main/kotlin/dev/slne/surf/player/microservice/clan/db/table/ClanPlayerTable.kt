package dev.slne.surf.player.microservice.clan.db.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas

/**
 * No longer read or written: whether a player accepts clan invites is the setting `clan:invites`.
 * The table is only kept so existing databases need no migration; it can be dropped together with
 * the legacy UUID column migration. [acceptsClanInvites] is copied into the setting once by
 * [dev.slne.surf.player.microservice.clan.db.migration.migrateClanInviteOptOuts] and may only be
 * dropped after that ran (see docs/deployment-checklist.md).
 */
object ClanPlayerTable : ULongIdTable("${PlayerSchemas.clan}.clan_players") {
    val uuid = nativeUuid("uuid").uniqueIndex()
    val acceptsClanInvites = bool("accepts_clan_invites").default(true)
}
