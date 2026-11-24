package dev.slne.surf.social.microservice.clan.services

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.api.clan.member.ClanMemberService
import dev.slne.surf.social.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.social.core.common.clan.member.CoreClanMemberService
import java.util.*

@AutoService(ClanMemberService::class)
class ClanMemberServiceImpl : CoreClanMemberService {

    override suspend fun findMemberByName(name: String): ClanMember? {
        throw NotImplementedError()
    }

    override fun registerListener(listener: ClanMemberListener) {
        throw NotImplementedError()
    }

    override fun unregisterListener(listener: ClanMemberListener) {
        throw NotImplementedError()
    }

    override suspend fun findMemberByUuid(uuid: UUID): ClanMember? {
        throw NotImplementedError()
    }

    override suspend fun addMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult {
        throw NotImplementedError()
    }

    override suspend fun removeMember(clanID: ULong, player: UUID): Boolean {
        throw NotImplementedError()
    }

    override suspend fun changeRole(member: ClanMemberImpl, role: ClanMemberRole): Boolean {
        throw NotImplementedError()
    }
}