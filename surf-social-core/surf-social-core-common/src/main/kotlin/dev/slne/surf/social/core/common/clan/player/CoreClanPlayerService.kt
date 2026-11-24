package dev.slne.surf.social.core.common.clan.player

import dev.slne.surf.social.api.clan.player.ClanPlayerService

interface CoreClanPlayerService : ClanPlayerService {
    suspend fun invalidateCaches()
    suspend fun changeAcceptsClanInvites(
        playerImpl: ClanPlayerImpl,
        acceptsClanInvites: Boolean
    ): Boolean

    companion object : CoreClanPlayerService by ClanPlayerService.INSTANCE as CoreClanPlayerService
}