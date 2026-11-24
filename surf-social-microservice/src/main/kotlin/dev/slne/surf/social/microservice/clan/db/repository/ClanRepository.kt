package dev.slne.surf.social.microservice.clan.db.repository

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanCreationResult
import dev.slne.surf.social.api.clan.clan.ClanTagColor
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.common.clan.clan.ClanTagRules
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.social.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.social.microservice.clan.db.table.ClansTable
import dev.slne.surf.social.microservice.clan.db.table.SurfPlayersTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import java.time.OffsetDateTime
import java.util.*

object ClanRepository {
    private val log = logger()

    suspend fun findClanByPlayer(playerUuid: UUID): ClanImpl? {
        val clanIDSubQuery = ClanMembersTable
            .select(ClanMembersTable.clanId)
            .where { ClanMembersTable.uuid eq playerUuid }

        return findClanWithMembers { ClansTable.id inSubQuery clanIDSubQuery }
    }

    suspend fun findClanByUuid(clanUuid: UUID): ClanImpl? =
        findClanWithMembers { ClansTable.uuid eq clanUuid }

    suspend fun findClanByTag(tag: String): ClanImpl? =
        findClanWithMembers { ClansTable.tag.upperCase() eq ClanTagRules.normalize(tag) }

    suspend fun findClanByID(id: ULong): ClanImpl? =
        findClanWithMembers { ClansTable.id eq id }

    suspend fun fetchAllClansWithoutMembersSortByMemberCount(): Collection<ClanImpl> =
        suspendTransaction {
            val cutoff = OffsetDateTime.now().minusSeconds(Clan.INACTIVE_AFTER.inWholeSeconds)
            val lastActiveAt = Coalesce(SurfPlayersTable.lastSeen, ClanMembersTable.createdAt)
            val activeMemberCount = Sum(
                Case()
                    .When(lastActiveAt greaterEq cutoff, intLiteral(1))
                    .Else(intLiteral(0)),
                IntegerColumnType()
            )

            joinClansWithMembers()
                .select(ClansTable.columns + activeMemberCount)
                .groupBy(ClansTable.id)
                .orderBy(activeMemberCount, SortOrder.DESC)
                .map { row -> clanFromRow(row, emptySet()) }
                .toList()
        }

    suspend fun updateDescription(clanID: ULong, description: String?): Boolean =
        suspendTransaction {
            ClansTable.update({ ClansTable.id eq clanID }) {
                it[ClansTable.description] = description
            } > 0
        }

    suspend fun updateDiscordInvite(clanID: ULong, discordInvite: String?): Boolean =
        suspendTransaction {
            ClansTable.update({ ClansTable.id eq clanID }) {
                it[ClansTable.discordInvite] = discordInvite
            } > 0
        }

    suspend fun updateTagColor(
        clanID: ULong,
        tagForegroundColor: TextColor?,
        tagBackgroundColor: TextColor?,
        tagShadowColor: ShadowColor?
    ): Boolean = suspendTransaction {
        ClansTable.update({ ClansTable.id eq clanID }) {
            it[ClansTable.tagForegroundColor] = tagForegroundColor
            it[ClansTable.tagBackgroundColor] = tagBackgroundColor
            it[ClansTable.tagShadowColor] = tagShadowColor
        } > 0
    }

    suspend fun create(
        name: String,
        tag: String,
        owner: UUID,
        tagForegroundColor: TextColor?,
        tagBackgroundColor: TextColor?,
        tagShadowColor: ShadowColor?,
        description: String?,
        discordInvite: String?
    ): ClanCreationResult = suspendTransaction {
        if (ClanMemberRepository.isInAnyClan(owner)) {
            return@suspendTransaction ClanCreationResult.OwnerIsAlreadyInClan
        }

        // Name and tag are checked up front because the caller needs to know which of the two
        // collided, and ON CONFLICT DO NOTHING cannot tell them apart.
        if (isNameTaken(name)) {
            return@suspendTransaction ClanCreationResult.ClanNameAlreadyExists
        }

        // Normalized once and used for both the check and the insert: checking the canonical form
        // but storing the raw one would keep creating the very mixed-case rows that made clans
        // unreachable by tag in the first place.
        val normalizedTag = ClanTagRules.normalize(tag)
        if (isTagTaken(normalizedTag)) {
            return@suspendTransaction ClanCreationResult.ClanTagAlreadyExists
        }

        // The checks above leave a window for two concurrent creations, so still insert with
        // ON CONFLICT DO NOTHING. Catching the violation instead is not an option: PostgreSQL aborts
        // the whole transaction on a failed statement, and the following COMMIT would then fail with
        // PostgresqlRollbackException. Losing the name/tag distinction in that rare race is the
        // acceptable trade.
        val clanRow = ClansTable.insertReturning(ignoreErrors = true) { smt ->
            smt[this.name] = name
            smt[this.tag] = normalizedTag
            smt[this.createdBy] = owner
            tagForegroundColor?.let { smt[this.tagForegroundColor] = it }
            tagBackgroundColor?.let { smt[this.tagBackgroundColor] = it }
            tagShadowColor?.let { smt[this.tagShadowColor] = it }
            description?.let { smt[this.description] = it }
            discordInvite?.let { smt[this.discordInvite] = it }
        }.singleOrNull() ?: run {
            log.atWarning()
                .log("Clan %s (%s) lost a concurrent creation race for its name or tag", name, tag)
            return@suspendTransaction ClanCreationResult.ClanAlreadyExists
        }

        val ownerRow = ClanMembersTable.insertReturning {
            it[this.uuid] = owner
            it[this.addedBy] = owner
            it[this.role] = ClanMemberRole.OWNER
            it[this.clanId] = clanRow[ClansTable.id]
        }.single()

        val ownerMember = ClanMemberRepository.memberFromRow(ownerRow)
        ClanCreationResult.Success(clanFromRow(clanRow, setOf(ownerMember)))
    }

    suspend fun delete(clanID: ULong): Boolean = suspendTransaction {
        ClansTable.deleteWhere { ClansTable.id eq clanID } > 0
    }

    suspend fun suggestTagsByPrefix(prefix: String, limit: Int): List<String> {
        if (prefix.isBlank()) return emptyList()

        val normalizedPrefix = ClanTagRules.normalize(prefix)

        // The prefix is raw command input while a tag only ever holds letters and digits, so anything
        // else cannot prefix one. Rejecting it here also keeps LIKE metacharacters out of the
        // pattern below, where a '%' would match every row and defeat both the prefix semantics and
        // the index.
        if (!normalizedPrefix.all { it.isLetterOrDigit() }) return emptyList()

        val tagUpperCase = ClansTable.tag.upperCase()

        return suspendTransaction {
            ClansTable
                .select(ClansTable.tag)
                .where { tagUpperCase like "$normalizedPrefix%" }
                .orderBy(tagUpperCase)
                .limit(limit)
                // Callers match suggestions against a normalized input, so a legacy mixed-case tag
                // has to be reported uppercase or it never survives their prefix filter.
                .map { ClanTagRules.normalize(it[ClansTable.tag]) }
                .toList()
        }
    }

    private fun joinClansWithMembers() = ClansTable
        .leftJoin(ClanMembersTable, { ClansTable.id }, { ClanMembersTable.clanId })
        .leftJoin(SurfPlayersTable, { ClanMembersTable.uuid }, { SurfPlayersTable.uuid })

    private suspend fun findClanWithMembers(where: () -> Op<Boolean>): ClanImpl? = suspendTransaction {
        val rows = joinClansWithMembers()
            .selectAll()
            .where(where)
            .toList()

        clanWithMembersFromRows(rows)
    }

    private suspend fun isNameTaken(name: String): Boolean = ClansTable
        .select(ClansTable.id)
        .where { ClansTable.name eq name }
        .limit(1)
        .singleOrNull() != null

    private suspend fun isTagTaken(normalizedTag: String): Boolean = ClansTable
        .select(ClansTable.id)
        .where { ClansTable.tag.upperCase() eq normalizedTag }
        .limit(1)
        .singleOrNull() != null

    /** Maps the rows of [joinClansWithMembers] for one clan, one row per member, to that clan. */
    private fun clanWithMembersFromRows(rows: List<ResultRow>): ClanImpl? {
        val clanRow = rows.firstOrNull() ?: return null
        val members = rows.mapNotNullTo(mutableObjectSetOf(rows.size)) { row ->
            if (row.getOrNull(ClanMembersTable.id) == null) return@mapNotNullTo null
            ClanMemberRepository.memberFromRow(row)
        }

        return clanFromRow(clanRow, members)
    }

    private fun clanFromRow(row: ResultRow, members: Set<ClanMemberImpl>): ClanImpl = ClanImpl(
        clanID = row[ClansTable.id].value,
        uuid = row[ClansTable.uuid],
        name = row[ClansTable.name],
        tag = row[ClansTable.tag],
        createdByUuid = row[ClansTable.createdBy],
        description = row[ClansTable.description],
        discordInvite = row[ClansTable.discordInvite],
        clanTagColor = ClanTagColor.clanTagColorOrNull(
            foreground = row[ClansTable.tagForegroundColor],
            background = row[ClansTable.tagBackgroundColor],
            shadow = row[ClansTable.tagShadowColor]
        ),
        members = members,
        updatedAt = row[ClansTable.updatedAt],
        createdAt = row[ClansTable.createdAt]
    )
}
