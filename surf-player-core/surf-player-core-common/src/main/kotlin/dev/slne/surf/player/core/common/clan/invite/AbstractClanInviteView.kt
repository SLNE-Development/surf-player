package dev.slne.surf.social.core.common.clan.invite

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.invite.ClanInviteView
import dev.slne.surf.social.core.common.clan.clan.CoreClanService

abstract class AbstractClanInviteView : ClanInviteView {
    abstract val clanID: ULong

    override suspend fun getClan(): Clan? {
        return CoreClanService.Companion.findClanByID(clanID)
    }

    override suspend fun getClanOrThrow(): Clan {
        return getClan() ?: throw NoSuchElementException("Clan with ID $clanID does not exist")
    }
}