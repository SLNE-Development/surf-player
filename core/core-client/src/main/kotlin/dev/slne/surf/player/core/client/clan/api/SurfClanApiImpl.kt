package dev.slne.surf.player.core.client.clan.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanCreateBuilder
import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.api.clan.clan.ClanValidationResult
import dev.slne.surf.player.api.clan.clan.listener.ClanListener
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.player.core.client.clan.services.ClientClanInviteService
import dev.slne.surf.player.core.client.clan.services.ClientClanMemberService
import dev.slne.surf.player.core.client.clan.services.ClientClanService
import dev.slne.surf.player.core.common.clan.CoreClanApi
import dev.slne.surf.player.core.common.clan.clan.AbstractClanView
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import java.util.*

@AutoService(SurfClanApi::class)
class SurfClanApiImpl : CoreClanApi {
    override fun registerListener(listener: ClanListener) =
        ClientClanService.registerListener(listener)

    override fun unregisterListener(listener: ClanListener) =
        ClientClanService.unregisterListener(listener)

    override fun registerListener(listener: ClanMemberListener) =
        ClientClanMemberService.registerListener(listener)

    override fun unregisterListener(listener: ClanMemberListener) =
        ClientClanMemberService.unregisterListener(listener)

    override suspend fun findClanByPlayer(uuid: UUID): Clan? =
        ClientClanService.findClanByPlayer(uuid)

    override suspend fun findClanByUuid(clanUuid: UUID): Clan? =
        ClientClanService.findClanByUuid(clanUuid)

    override suspend fun findClanByTag(tag: String): Clan? =
        ClientClanService.findClanByTag(tag)

    override fun validateClanNameAndTag(name: String, tag: String): ClanValidationResult =
        ClientClanService.validateClanNameAndTag(name, tag)

    override suspend fun createClan(
        owner: UUID,
        name: String,
        tag: String,
        additional: ClanCreateBuilder.() -> Unit
    ): ClanCreationResult {
        val builder = ClanCreateBuilder(name, tag, owner)
        builder.additional()

        return ClientClanService.createClan(builder.copy())
    }

    override suspend fun findClanMember(uuid: UUID): ClanMember? =
        ClientClanMemberService.findMemberByUuid(uuid)

    override suspend fun findClanMemberByName(name: String): ClanMember? =
        ClientClanMemberService.findMemberByName(name)

    override suspend fun findClanInvites(uuid: UUID): List<ClanInvite> =
        ClientClanInviteService.getPendingInvitesByPlayer(uuid)

    override suspend fun findClanInvite(uuid: UUID, clanName: String): ClanInvite? =
        ClientClanInviteService.getPendingInviteByPlayerAndClanName(uuid, clanName)

    override suspend fun findClanByID(id: ULong): ClanImpl? =
        ClientClanService.findClanByID(id)

    override suspend fun updateDescription(clan: ClanImpl, description: String?): Boolean =
        ClientClanService.updateDescription(clan, description)

    override suspend fun updateDiscordInvite(clan: ClanImpl, discordInvite: String?): Boolean =
        ClientClanService.updateDiscordInvite(clan, discordInvite)

    override suspend fun updateTagColor(clan: ClanImpl, update: ClanTagColorUpdate): Boolean =
        ClientClanService.updateTagColor(clan, update)

    override suspend fun fetchPendingInvites(clan: AbstractClanView): Set<ClanInviteImpl> =
        ClientClanService.fetchPendingInvites(clan)

    override suspend fun invitePlayer(
        clan: ClanImpl,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult = ClientClanService.invitePlayer(clan, invitee, invitedBy)

    override suspend fun revokeInvite(clan: ClanImpl, playerUuid: UUID): Boolean =
        ClientClanService.revokeInvite(clan, playerUuid)

    override suspend fun addMember(
        clan: ClanImpl,
        playerUuid: UUID,
        role: ClanMemberRole,
        addedBy: UUID?
    ): ClanMemberAddResult = ClientClanService.addMember(clan, playerUuid, role, addedBy)

    override suspend fun removeMember(clan: ClanImpl, playerUuid: UUID): Boolean =
        ClientClanService.removeMember(clan, playerUuid)

    override suspend fun delete(clan: ClanImpl): Boolean = ClientClanService.delete(clan)

    override suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult =
        ClientClanInviteService.acceptInvite(invite)

    override suspend fun revokeInvite(invite: ClanInviteImpl): Boolean =
        ClientClanInviteService.revokeInvite(invite)

    override suspend fun changeRole(member: ClanMemberImpl, role: ClanMemberRole): Boolean =
        ClientClanMemberService.changeRole(member, role)
}
