package dev.slne.surf.player.core.common.clan.player

import dev.slne.surf.player.api.clan.player.ClanPlayerService

interface CoreClanPlayerService : ClanPlayerService {
    suspend fun invalidateCaches()
    suspend fun changeAcceptsClanInvites(
        playerImpl: ClanPlayerImpl,
        acceptsClanInvites: Boolean
    ): Boolean

    companion object : CoreClanPlayerService by ClanPlayerService.INSTANCE as CoreClanPlayerService
}