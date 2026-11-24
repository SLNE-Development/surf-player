package dev.slne.surf.social.core.client.tab.hook

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanView
import dev.slne.surf.social.api.clan.clan.listener.ClanCreatedListener
import dev.slne.surf.social.api.clan.clan.listener.ClanDeletedListener
import dev.slne.surf.social.api.clan.clan.listener.ClanUpdateMemberListener
import dev.slne.surf.social.api.clan.clan.listener.ClanUpdatedListener
import dev.slne.surf.social.core.client.tab.service.TablistService
import net.kyori.adventure.text.Component
import java.util.*

object ClanHook {
    suspend fun getClanTag(playerUuid: UUID): Component? =
        Clan.byPlayer(playerUuid)?.renderClanTag(10)

    fun createListeners() {
        Clan.registerListener(ClanCreatedListener(::formatMembers))
        Clan.registerListener(ClanUpdatedListener(::formatMembers))
        Clan.registerListener(ClanDeletedListener(::formatMembers))
        Clan.registerListener(ClanUpdateMemberListener { clan, _, _ -> formatMembers(clan) })
    }

    private fun formatMembers(clan: ClanView) {
        for (member in clan.members) {
            TablistService.updateEntry(member.uuid)
        }
    }
}
