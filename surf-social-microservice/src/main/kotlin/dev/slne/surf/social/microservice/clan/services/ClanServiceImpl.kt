package dev.slne.surf.social.microservice.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.clan.clan.*
import dev.slne.surf.social.api.clan.clan.listener.ClanListener
import dev.slne.surf.social.api.clan.invite.ClanInviteResult
import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.core.common.clan.clan.AbstractClanView
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.common.clan.clan.CoreClanService
import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import java.util.*

@AutoService(ClanService::class)
class ClanServiceImpl : CoreClanService {

    override fun init() = Unit

    override suspend fun invalidateCaches() {
        throw NotImplementedError()
    }

    override fun registerListener(listener: ClanListener) {
        throw NotImplementedError()
    }

    override fun unregisterListener(listener: ClanListener) {
        throw NotImplementedError()
    }

    override suspend fun findClanByPlayer(playerUuid: UUID): Clan? {
        throw NotImplementedError()
    }

    override suspend fun findClanByUuid(clanUuid: UUID): Clan? {
        throw NotImplementedError()
    }

    override suspend fun findClanByTag(tag: String): Clan? {
        throw NotImplementedError()
    }

    override suspend fun findClanByID(id: ULong): ClanImpl? {
        throw NotImplementedError()
    }

    override suspend fun fetchAllClansWithoutMembersSortByMemberCount(): Collection<ClanImpl> {
        throw NotImplementedError()
    }

    override fun validateClanNameAndTag(name: String, tag: String): ClanValidationResult {
        throw NotImplementedError()
    }

    override suspend fun createClan(properties: ClanCreateBuilder): ClanCreationResult {
        throw NotImplementedError()
    }

    override suspend fun updateDescription(clan: ClanImpl, description: String?): Boolean {
        throw NotImplementedError()
    }

    override suspend fun updateDiscordInvite(clan: ClanImpl, discordInvite: String?): Boolean {
        throw NotImplementedError()
    }

    override suspend fun updateTagColor(clan: ClanImpl, update: ClanTagColor.Update): Boolean {
        throw NotImplementedError()
    }

    override suspend fun fetchPendingInvites(clan: AbstractClanView): Set<ClanInviteImpl> {
        throw NotImplementedError()
    }

    override suspend fun invitePlayer(
        clan: ClanImpl,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        throw NotImplementedError()
    }

    override suspend fun revokeInvite(clan: ClanImpl, playerUuid: UUID): Boolean {
        throw NotImplementedError()
    }

    override suspend fun addMember(
        clan: ClanImpl,
        playerUuid: UUID,
        role: ClanMemberRole,
        addedBy: UUID?
    ): ClanMemberAddResult {
        throw NotImplementedError()
    }

    override suspend fun removeMember(clan: ClanImpl, playerUuid: UUID): Boolean {
        throw NotImplementedError()
    }

    override suspend fun delete(clan: ClanImpl): Boolean {
        throw NotImplementedError()
    }

    override suspend fun computeTagSuggestions(input: String, limit: Int): Collection<String> {
        throw NotImplementedError()
    }
}