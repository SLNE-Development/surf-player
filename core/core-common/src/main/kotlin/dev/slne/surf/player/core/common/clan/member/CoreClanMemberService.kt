package dev.slne.surf.player.core.common.clan.member

import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.api.clan.member.ClanMemberService
import java.util.*

interface CoreClanMemberService : ClanMemberService {
    suspend fun addMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult

    suspend fun removeMember(clanID: ULong, player: UUID): Boolean
    suspend fun changeRole(member: ClanMemberImpl, role: ClanMemberRole): Boolean

    companion object : CoreClanMemberService by ClanMemberService.INSTANCE as CoreClanMemberService
}