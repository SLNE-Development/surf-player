package dev.slne.surf.social.microservice.clan.db.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.leftJoin
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.social.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.social.microservice.clan.db.table.SurfPlayersTable
import kotlinx.coroutines.flow.singleOrNull
import java.util.*

object ClanMemberRepository {
    suspend fun createMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult = suspendTransaction {
        // ON CONFLICT DO NOTHING rather than catching the constraint violation: PostgreSQL aborts the
        // whole transaction on a failed statement, so catching one and returning normally makes the
        // following COMMIT fail with PostgresqlRollbackException. No row back means the unique index
        // on uuid already had one, so the player is in a clan already.
        val row = ClanMembersTable.insertReturning(ignoreErrors = true) {
            it[this.uuid] = player
            it[this.addedBy] = invitedBy
            it[this.role] = role
            it[this.clanId] = clanID
        }.singleOrNull() ?: return@suspendTransaction ClanMemberAddResult.AlreadyMember

        ClanMemberAddResult.Success(memberFromRow(row))
    }

    suspend fun deleteMember(clanID: ULong, player: UUID): Boolean = suspendTransaction {
        ClanMembersTable.deleteWhere {
            ClanMembersTable.clanId eq clanID and (ClanMembersTable.uuid eq player)
        } > 0
    }

    suspend fun changeRole(memberID: ULong, role: ClanMemberRole): Boolean = suspendTransaction {
        ClanMembersTable.update({ ClanMembersTable.id eq memberID }) {
            it[this.role] = role
        } > 0
    }

    suspend fun findByUuid(uuid: UUID): ClanMemberImpl? = suspendTransaction {
        ClanMembersTable
            .leftJoin(SurfPlayersTable, { ClanMembersTable.uuid }, { SurfPlayersTable.uuid })
            .selectAll()
            .where { ClanMembersTable.uuid eq uuid }
            .limit(1)
            .singleOrNull()
            ?.let(::memberFromRow)
    }

    /** Whether [player] is a member of any clan. Must be called inside a transaction. */
    suspend fun isInAnyClan(player: UUID): Boolean = ClanMembersTable
        .select(ClanMembersTable.id)
        .where { ClanMembersTable.uuid eq player }
        .limit(1)
        .singleOrNull() != null

    /**
     * Maps a [ClanMembersTable] row, optionally joined with [SurfPlayersTable] for the member's
     * last activity, to a [ClanMemberImpl].
     */
    fun memberFromRow(row: ResultRow): ClanMemberImpl {
        val createdAt = row[ClanMembersTable.createdAt]
        val lastSeen = row.getOrNull(SurfPlayersTable.lastSeen)

        return ClanMemberImpl(
            ID = row[ClanMembersTable.id].value,
            uuid = row[ClanMembersTable.uuid],
            role = row[ClanMembersTable.role],
            addedBy = row[ClanMembersTable.addedBy],
            createdAt = createdAt,
            updatedAt = row[ClanMembersTable.updatedAt],
            lastActiveAt = lastSeen ?: createdAt
        )
    }
}
