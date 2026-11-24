package dev.slne.surf.social.api.clan.invite

import dev.slne.surf.social.api.clan.util.InternalClanApi
import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val instance = requiredService<ClanInviteService>()

@InternalClanApi
interface ClanInviteService {
    suspend fun getPendingInvitesByPlayer(invited: UUID): List<ClanInvite>
    suspend fun getPendingInviteByPlayerAndClanName(invited: UUID, clanName: String): ClanInvite?

    companion object : ClanInviteService by instance {
        val INSTANCE get() = instance
    }
}