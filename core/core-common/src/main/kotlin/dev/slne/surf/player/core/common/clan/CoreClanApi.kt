package dev.slne.surf.player.core.common.clan

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.core.common.clan.clan.AbstractClanView
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import java.util.*

/**
 * The operations the clan domain objects ([ClanImpl], [ClanInviteImpl], [ClanMemberImpl]) need
 * from the [SurfClanApi] implementation of the client.
 */
interface CoreClanApi : SurfClanApi {
    suspend fun findClanByID(id: ULong): ClanImpl?

    suspend fun updateDescription(clan: ClanImpl, description: String?): Boolean
    suspend fun updateDiscordInvite(clan: ClanImpl, discordInvite: String?): Boolean
    suspend fun updateTagColor(clan: ClanImpl, update: ClanTagColorUpdate): Boolean

    suspend fun fetchPendingInvites(clan: AbstractClanView): Set<ClanInviteImpl>
    suspend fun invitePlayer(clan: ClanImpl, invitee: UUID, invitedBy: UUID): ClanInviteResult
    suspend fun revokeInvite(clan: ClanImpl, playerUuid: UUID): Boolean

    suspend fun addMember(
        clan: ClanImpl,
        playerUuid: UUID,
        role: ClanMemberRole,
        addedBy: UUID?
    ): ClanMemberAddResult

    suspend fun removeMember(clan: ClanImpl, playerUuid: UUID): Boolean

    suspend fun delete(clan: ClanImpl): Boolean

    suspend fun acceptInvite(invite: ClanInviteImpl): ClanInviteAcceptResult
    suspend fun revokeInvite(invite: ClanInviteImpl): Boolean

    suspend fun changeRole(member: ClanMemberImpl, role: ClanMemberRole): Boolean

    companion object : CoreClanApi by SurfClanApi.INSTANCE as CoreClanApi
}
