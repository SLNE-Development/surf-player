package dev.slne.surf.player.core.common.clan.invite

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInviteView
import dev.slne.surf.player.core.common.clan.clan.CoreClanService

abstract class AbstractClanInviteView : ClanInviteView {
    abstract val clanID: ULong

    override suspend fun getClan(): Clan? {
        return CoreClanService.Companion.findClanByID(clanID)
    }

    override suspend fun getClanOrThrow(): Clan {
        return getClan() ?: throw NoSuchElementException("Clan with ID $clanID does not exist")
    }
}