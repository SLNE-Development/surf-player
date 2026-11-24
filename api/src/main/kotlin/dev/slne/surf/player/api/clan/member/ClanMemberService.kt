package dev.slne.surf.player.api.clan.member

import dev.slne.surf.player.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.player.api.clan.util.InternalClanApi
import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val instance = requiredService<ClanMemberService>()

@InternalClanApi
interface ClanMemberService {
    fun registerListener(listener: ClanMemberListener)
    fun unregisterListener(listener: ClanMemberListener)

    suspend fun findMemberByUuid(uuid: UUID): ClanMember?
    suspend fun findMemberByName(name: String): ClanMember?

    companion object : ClanMemberService by instance {
        val INSTANCE get() = instance
    }
}