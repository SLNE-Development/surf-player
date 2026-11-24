package dev.slne.surf.player.microservice.clan.db.table

import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.player.microservice.PlayerSchemas

object ClanMembersTable : AuditableLongIdTable("${PlayerSchemas.clan}.clan_members") {
    val uuid = nativeUuid("uuid").uniqueIndex()
    val addedBy = nativeUuid("added_by").nullable().default(null)
    val role = enumerationByName<ClanMemberRole>("role", 16).default(ClanMemberRole.MEMBER)
    val clanId = reference(
        "clan_id",
        ClansTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).index()
}