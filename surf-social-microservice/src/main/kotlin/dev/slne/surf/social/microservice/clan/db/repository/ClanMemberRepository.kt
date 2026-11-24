package dev.slne.surf.social.microservice.clan.db.repository

import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import java.util.*

private val instance = requiredService<ClanMemberRepository>()

interface ClanMemberRepository {
    suspend fun createMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult

    suspend fun deleteMember(clanID: ULong, player: UUID): Boolean

    suspend fun changeRole(memberID: ULong, role: ClanMemberRole): Boolean

    suspend fun findByUuid(uuid: UUID): ClanMemberImpl?

    companion object : ClanMemberRepository by instance {
        val INSTANCE get() = instance
    }
}