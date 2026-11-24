package dev.slne.surf.player.microservice.clan.db.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import dev.slne.surf.player.core.common.clan.player.ClanPlayerImpl
import dev.slne.surf.player.microservice.clan.db.table.ClanPlayerTable
import kotlinx.coroutines.flow.singleOrNull
import java.util.*

object ClanPlayerRepository {
    suspend fun findOrCreateByUuid(uuid: UUID): ClanPlayerImpl = suspendTransaction {
        selectByUuid(uuid)?.let { return@suspendTransaction it }

        // ON CONFLICT DO NOTHING, because another transaction may have inserted the same player
        // between the select above and this insert - two servers touching the same player at once is
        // ordinary. Letting the unique index reject the insert would abort this whole transaction in
        // PostgreSQL; an empty result instead means the other side won, so read its row.
        ClanPlayerTable.insertReturning(ignoreErrors = true) { it[this.uuid] = uuid }
            .singleOrNull()
            ?.let(::playerFromRow)
            ?: selectByUuid(uuid)
            ?: error("Clan player $uuid could neither be inserted nor read back")
    }

    suspend fun changeAcceptsClanInvites(
        uuid: UUID,
        acceptsClanInvites: Boolean
    ): Unit = suspendTransaction {
        val player = findOrCreateByUuid(uuid)

        ClanPlayerTable.update({ ClanPlayerTable.id eq player.ID }) {
            it[this.acceptsClanInvites] = acceptsClanInvites
        }
    }

    private suspend fun selectByUuid(uuid: UUID): ClanPlayerImpl? = ClanPlayerTable.selectAll()
        .where { ClanPlayerTable.uuid eq uuid }
        .singleOrNull()
        ?.let(::playerFromRow)

    private fun playerFromRow(row: ResultRow): ClanPlayerImpl = ClanPlayerImpl(
        ID = row[ClanPlayerTable.id].value,
        uuid = row[ClanPlayerTable.uuid],
        acceptsClanInvites = row[ClanPlayerTable.acceptsClanInvites]
    )
}
