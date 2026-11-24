package dev.slne.surf.social.api.clan.member

import dev.slne.surf.social.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.social.api.clan.util.InternalClanApi
import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val service = requiredService<ClanMemberService>()

@InternalClanApi
interface ClanMemberService {
    fun registerListener(listener: ClanMemberListener)
    fun unregisterListener(listener: ClanMemberListener)

    suspend fun findMemberByUuid(uuid: UUID): ClanMember?
    suspend fun findMemberByName(name: String): ClanMember?

    companion object : ClanMemberService by service {
        val INSTANCE get() = service
    }
}