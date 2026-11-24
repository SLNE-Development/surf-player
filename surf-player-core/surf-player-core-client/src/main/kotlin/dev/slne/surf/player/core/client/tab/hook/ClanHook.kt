package dev.slne.surf.social.core.client.tab.hook

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanView
import dev.slne.surf.social.core.client.clan.onEveryClanChange
import dev.slne.surf.social.core.client.tab.service.TablistService
import net.kyori.adventure.text.Component
import java.util.*

object ClanHook {
    suspend fun getClanTag(playerUuid: UUID): Component? =
        Clan.byPlayer(playerUuid)?.renderClanTag(10)

    fun createListeners() {
        onEveryClanChange(::updateMembers)
    }

    private fun updateMembers(clan: ClanView) {
        for (member in clan.members) {
            TablistService.updateEntry(member.uuid)
        }
    }
}
