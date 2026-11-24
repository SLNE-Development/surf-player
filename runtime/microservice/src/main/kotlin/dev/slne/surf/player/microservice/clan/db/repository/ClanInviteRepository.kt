package dev.slne.surf.player.microservice.clan.db.repository

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.inSubQuery
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanInviteAcceptance
import dev.slne.surf.player.microservice.clan.db.table.ClanInvitesTable
import dev.slne.surf.player.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.player.microservice.clan.db.table.ClansTable
import kotlinx.coroutines.flow.*
import java.util.*

object ClanInviteRepository {
    suspend fun fetchPendingInvites(clanID: ULong): Set<ClanInviteImpl> = suspendTransaction {
        ClanInvitesTable
            .selectAll()
            .where { ClanInvitesTable.clanId eq clanID }
            .map(::inviteFromRow)
            .toSet()
    }

    suspend fun getPendingInviteByPlayerAndClanName(
        invited: UUID,
        clanName: String
    ): ClanInviteImpl? = suspendTransaction {
        val clanIDSubQuery = ClansTable
            .select(ClansTable.id)
            .where { ClansTable.name eq clanName }

        ClanInvitesTable
            .selectAll()
            .where { ClanInvitesTable.invited eq invited and (ClanInvitesTable.clanId inSubQuery clanIDSubQuery) }
            .singleOrNull()
            ?.let(::inviteFromRow)
    }

    suspend fun getPendingInvitesByPlayer(invited: UUID): List<ClanInviteImpl> = suspendTransaction {
        ClanInvitesTable
            .selectAll()
            .where { ClanInvitesTable.invited eq invited }
            .map(::inviteFromRow)
            .toList()
    }

    suspend fun createInvite(
        clanID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult = suspendTransaction {
        // Whether the invitee accepts invites (setting `clan:invites`) is checked by the client.
        if (ClanMemberRepository.isInAnyClan(invitee)) return@suspendTransaction ClanInviteResult.AlreadyInClan

        // ON CONFLICT DO NOTHING rather than catching the constraint violation: PostgreSQL aborts the
        // whole transaction on a failed statement, so catching one and returning normally makes the
        // following COMMIT fail with PostgresqlRollbackException. No row back means the unique index
        // on (invited, clan_id) already had one.
        val row = ClanInvitesTable.insertReturning(ignoreErrors = true) {
            it[this.invited] = invitee
            it[this.invitedBy] = invitedBy
            it[this.clanId] = clanID
        }.singleOrNull() ?: return@suspendTransaction ClanInviteResult.AlreadyInvited

        ClanInviteResult.Success(inviteFromRow(row))
    }

    suspend fun deleteInvite(clanID: ULong, invitee: UUID): Boolean = suspendTransaction {
        ClanInvitesTable.deleteWhere {
            ClanInvitesTable.clanId eq clanID and (ClanInvitesTable.invited eq invitee)
        } > 0
    }

    suspend fun acceptInvite(
        inviteID: ULong,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteAcceptance = suspendTransaction {
        if (ClanMemberRepository.isInAnyClan(invitee)) {
            return@suspendTransaction ClanInviteAcceptance.AlreadyInClan
        }

        val deletedRow = ClanInvitesTable.deleteReturning {
            ClanInvitesTable.id eq inviteID and (ClanInvitesTable.invited eq invitee)
        }.singleOrNull() ?: return@suspendTransaction ClanInviteAcceptance.InviteNotFound

        val clanId = deletedRow[ClanInvitesTable.clanId]
        ClanMembersTable.insert {
            it[uuid] = invitee
            it[addedBy] = invitedBy
            it[this.clanId] = clanId
        }

        val clan = ClanRepository.findClanByID(clanId.value) ?: error("Clan ${clanId.value} vanished while accepting an invite")
        ClanInviteAcceptance.Accepted(clan)
    }

    private fun inviteFromRow(row: ResultRow): ClanInviteImpl = ClanInviteImpl(
        id = row[ClanInvitesTable.id].value,
        clanID = row[ClanInvitesTable.clanId].value,
        invited = row[ClanInvitesTable.invited],
        invitedBy = row[ClanInvitesTable.invitedBy],
        createdAt = row[ClanInvitesTable.createdAt],
        updatedAt = row[ClanInvitesTable.updatedAt]
    )
}
